package com.library.core.service;

import com.library.core.domain.book.Book;
import com.library.core.repository.book.BookRepository;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookService {

    private final BookRepository bookRepository;
    private final ExecutorService executorService;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public Book addBook(Book book) {
        validateBook(book);
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    public List<Book> searchBooksByAuthor(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Асинхронные методы
    public CompletableFuture<Book> addBookAsync(Book book) {
        return CompletableFuture.supplyAsync(() -> addBook(book), executorService);
    }

    public CompletableFuture<List<Book>> searchBooksAsync(String searchTerm) {
        return CompletableFuture.supplyAsync(() -> {
            List<Book> byTitle = bookRepository.findByTitleContaining(searchTerm);
            List<Book> byAuthor = bookRepository.findByAuthorName(searchTerm);

            // Объединяем результаты
            byTitle.addAll(byAuthor);
            return byTitle.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }, executorService);
    }

    // Валидация
    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }

        if (book.getIsbn() != null && !isValidIsbn(book.getIsbn())) {
            throw new IllegalArgumentException(
                    "Invalid ISBN format. Should be 10 or 13 digits (with or without hyphens)"
            );
        }

        if (book.getPublicationYear() != null &&
                book.getPublicationYear().getValue() > Year.now().getValue()) {
            throw new IllegalArgumentException("Publication year cannot be in the future");
        }
    }

    private boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return true; // null или пустая строка допустимы
        }

        // Удаляем все дефисы и пробелы
        String cleaned = isbn.replaceAll("[\\s-]+", "");

        // Проверяем, что остались только цифры
        if (!cleaned.matches("\\d+")) {
            return false;
        }

        // Проверяем длину
        int length = cleaned.length();
        return length == 10 || length == 13;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}