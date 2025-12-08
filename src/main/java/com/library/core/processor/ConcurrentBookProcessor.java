package com.library.core.processor;

import com.library.core.domain.book.Book;
import com.library.core.service.BookService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ConcurrentBookProcessor {

    private final BookService bookService;
    private final ExecutorService executorService;
    private final CompletionService<Book> completionService;

    public ConcurrentBookProcessor(BookService bookService) {
        this.bookService = bookService;
        this.executorService = Executors.newFixedThreadPool(10);
        this.completionService = new ExecutorCompletionService<>(executorService);
    }

    public List<Book> processBooksConcurrently(List<Book> books) {
        // Отправляем все книги на асинхронную обработку
        books.forEach(book ->
                completionService.submit(() -> bookService.addBook(book))
        );

        // Собираем результаты
        List<Book> processedBooks = new CopyOnWriteArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            try {
                Future<Book> future = completionService.take();
                Book result = future.get();
                processedBooks.add(result);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Error processing books concurrently", e);
            }
        }

        return processedBooks;
    }

    public Map<Long, CompletableFuture<Book>> processWithCompletableFutures(List<Book> books) {
        Map<Long, CompletableFuture<Book>> futures = new ConcurrentHashMap<>();

        books.forEach(book -> {
            CompletableFuture<Book> future = CompletableFuture
                    .supplyAsync(() -> bookService.addBook(book), executorService)
                    .exceptionally(ex -> {
                        System.err.println("Error processing book: " + ex.getMessage());
                        return null;
                    });

            futures.put(System.currentTimeMillis(), future);
        });

        return futures;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}