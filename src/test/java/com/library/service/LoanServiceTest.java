package com.library.core.service;

import com.library.core.domain.book.Book;
import com.library.core.domain.book.BookCopy;
import com.library.core.domain.loan.BorrowRecord;
import com.library.core.domain.user.Reader;
import com.library.core.repository.book.BookCopyRepository;
import com.library.core.repository.book.impl.InMemoryBookCopyRepository;
import com.library.core.repository.loan.BorrowRecordRepository;
import com.library.core.repository.loan.impl.InMemoryBorrowRecordRepository;
import com.library.core.repository.user.ReaderRepository;
import com.library.core.repository.user.impl.InMemoryReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {

    private LoanService loanService;
    private ReaderRepository readerRepository;
    private BookCopyRepository bookCopyRepository;
    private BorrowRecordRepository borrowRecordRepository;

    @BeforeEach
    void setUp() {
        readerRepository = new InMemoryReaderRepository();
        bookCopyRepository = new InMemoryBookCopyRepository();
        borrowRecordRepository = new InMemoryBorrowRecordRepository();
        loanService = new LoanService(borrowRecordRepository, bookCopyRepository, readerRepository);
    }

    @Test
    void whenBorrowBookWithValidData_thenRecordIsCreated() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        readerRepository.save(reader);

        Book book = new Book("Test Book", "123");
        BookCopy copy = new BookCopy("INV-001", book);
        copy.setStatus(BookCopy.CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        // When
        BorrowRecord record = loanService.borrowBook(reader.getId(), copy.getId());

        // Then
        assertNotNull(record);
        assertEquals(reader.getId(), record.getReader().getId());
        assertEquals(copy.getId(), record.getBookCopy().getId());
        assertNotNull(record.getBorrowDate());
        assertNotNull(record.getDueDate());
        assertNull(record.getReturnDate());

        // Проверяем, что статус книги изменился
        BookCopy updatedCopy = bookCopyRepository.findById(copy.getId()).orElseThrow();
        assertEquals(BookCopy.CopyStatus.BORROWED, updatedCopy.getStatus());
    }

    @Test
    void whenReturnBook_thenRecordIsUpdated() {
        // Given - создаем запись о выдаче
        Reader reader = new Reader("12345", "John", "Doe");
        readerRepository.save(reader);

        Book book = new Book("Test Book", "123");
        BookCopy copy = new BookCopy("INV-001", book);
        copy.setStatus(BookCopy.CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        BorrowRecord borrowRecord = loanService.borrowBook(reader.getId(), copy.getId());

        // When
        BorrowRecord returnedRecord = loanService.returnBook(borrowRecord.getId());

        // Then
        assertNotNull(returnedRecord.getReturnDate());
        assertEquals(LocalDate.now(), returnedRecord.getReturnDate());

        BookCopy updatedCopy = bookCopyRepository.findById(copy.getId()).orElseThrow();
        assertEquals(BookCopy.CopyStatus.AVAILABLE, updatedCopy.getStatus());
    }

    @Test
    void whenCalculateFines_thenCorrectAmount() {
        // Given - создаем просроченную запись
        Reader reader = new Reader("12345", "John", "Doe");
        readerRepository.save(reader);

        BorrowRecord record = new BorrowRecord();
        record.setReader(reader);
        record.setBorrowDate(LocalDate.now().minusDays(40)); // 40 дней назад
        record.setDueDate(LocalDate.now().minusDays(10)); // просрочено на 10 дней
        borrowRecordRepository.save(record);

        // When
        double fines = loanService.calculateTotalFinesForReader(reader.getId());

        // Then - 10 дней * 10 рублей = 100 рублей
        assertEquals(100.0, fines, 0.01);
    }
}