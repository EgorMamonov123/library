package com.library.core.service;

import com.library.core.domain.book.BookCopy;
import com.library.core.domain.loan.BorrowRecord;
import com.library.core.domain.user.Reader;
import com.library.core.repository.loan.BorrowRecordRepository;
import com.library.core.repository.book.BookCopyRepository;
import com.library.core.repository.user.ReaderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoanService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReaderRepository readerRepository;
    private final ExecutorService executorService;

    // Константы
    private static final int MAX_BORROW_DAYS = 30;
    private static final int MAX_BOOKS_PER_READER = 5;

    public LoanService(BorrowRecordRepository borrowRecordRepository,
                       BookCopyRepository bookCopyRepository,
                       ReaderRepository readerRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.readerRepository = readerRepository;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    // Выдача книги
    public BorrowRecord borrowBook(Long readerId, Long bookCopyId) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        Optional<BookCopy> bookCopyOpt = bookCopyRepository.findById(bookCopyId);

        if (!readerOpt.isPresent()) {
            throw new IllegalArgumentException("Reader not found with id: " + readerId);
        }

        if (!bookCopyOpt.isPresent()) {
            throw new IllegalArgumentException("BookCopy not found with id: " + bookCopyId);
        }

        Reader reader = readerOpt.get();
        BookCopy bookCopy = bookCopyOpt.get();

        // Проверки
        if (!reader.canBorrowBooks()) {
            throw new IllegalStateException("Reader cannot borrow more books");
        }

        if (bookCopy.getStatus() != BookCopy.CopyStatus.AVAILABLE) {
            throw new IllegalStateException("Book copy is not available. Status: " + bookCopy.getStatus());
        }

        // Создание записи о выдаче
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(MAX_BORROW_DAYS);

        BorrowRecord record = new BorrowRecord(reader, bookCopy, borrowDate, dueDate);

        // Обновление статуса книги
        bookCopy.setStatus(BookCopy.CopyStatus.BORROWED);
        bookCopyRepository.save(bookCopy);

        return borrowRecordRepository.save(record);
    }

    // Возврат книги
    public BorrowRecord returnBook(Long borrowRecordId) {
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(borrowRecordId);

        if (!recordOpt.isPresent()) {
            throw new IllegalArgumentException("Borrow record not found with id: " + borrowRecordId);
        }

        BorrowRecord record = recordOpt.get();

        if (record.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned");
        }

        // Обновление записи
        record.setReturnDate(LocalDate.now());
        record.setFineAmount(record.calculateFine());

        // Обновление статуса книги
        BookCopy bookCopy = record.getBookCopy();
        bookCopy.setStatus(BookCopy.CopyStatus.AVAILABLE);
        bookCopyRepository.save(bookCopy);

        return borrowRecordRepository.save(record);
    }

    // Поиск просроченных книг
    public List<BorrowRecord> findOverdueRecords() {
        return borrowRecordRepository.findOverdueRecords(LocalDate.now());
    }

    // Расчет штрафов для читателя
    public double calculateTotalFinesForReader(Long readerId) {
        List<BorrowRecord> records = borrowRecordRepository.findByReaderId(readerId);
        return records.stream()
                .filter(record -> record.getReturnDate() == null && LocalDate.now().isAfter(record.getDueDate()))
                .mapToDouble(BorrowRecord::calculateFine)
                .sum();
    }

    // Асинхронная обработка возвратов
    public CompletableFuture<BorrowRecord> returnBookAsync(Long borrowRecordId) {
        return CompletableFuture.supplyAsync(
                () -> returnBook(borrowRecordId),
                executorService
        );
    }

    // Пакетная обработка возвратов
    public CompletableFuture<Void> processBatchReturns(List<Long> recordIds) {
        return CompletableFuture.runAsync(() -> {
            recordIds.forEach(id -> {
                try {
                    returnBook(id);
                    System.out.println("Successfully returned book for record: " + id);
                } catch (Exception e) {
                    System.err.println("Error returning book for record " + id + ": " + e.getMessage());
                }
            });
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}