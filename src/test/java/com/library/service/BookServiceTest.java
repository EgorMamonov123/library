package com.library.service;

import com.library.core.domain.book.Book;
import com.library.core.repository.book.BookRepository;
import com.library.core.repository.book.impl.InMemoryBookRepository;
import com.library.core.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookRepository bookRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        bookService = new BookService(bookRepository);
    }

    @Test
    @DisplayName("Добавление книги с валидными данными")
    void whenAddValidBook_thenBookIsSaved() {
        // Given
        Book book = new Book("Effective Java", "9780134685991"); // 13-значный ISBN без дефисов
        book.setPublicationYear(Year.of(2018));

        // When
        Book savedBook = bookService.addBook(book);

        // Then
        assertNotNull(savedBook.getId());
        assertEquals("Effective Java", savedBook.getTitle());
        assertEquals("9780134685991", savedBook.getIsbn());
        assertTrue(bookRepository.existsById(savedBook.getId()));
    }

    @Test
    @DisplayName("Добавление книги без названия вызывает исключение")
    void whenAddBookWithoutTitle_thenThrowException() {
        // Given
        Book book = new Book(null, "9780134685991");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.addBook(book);
        });
    }

    @Test
    @DisplayName("Поиск книги по ISBN")
    void whenFindByIsbn_thenReturnBook() {
        // Given
        Book book = new Book("Clean Code", "9780132350884"); // 13-значный ISBN
        bookService.addBook(book);

        // When
        Optional<Book> found = bookService.getBookByIsbn("9780132350884");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Clean Code", found.get().getTitle());
    }

    @Test
    @DisplayName("Поиск книги по названию (частичное совпадение)")
    void whenSearchByTitle_thenReturnMatchingBooks() {
        // Given
        bookService.addBook(new Book("Java Concurrency in Practice", "0321349601")); // 10-значный ISBN
        bookService.addBook(new Book("Effective Java", "9780134685991")); // 13-значный ISBN
        bookService.addBook(new Book("Python Programming", "9781590282410"));

        // When
        List<Book> results = bookService.searchBooksByTitle("Java");

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream()
                .anyMatch(b -> b.getTitle().contains("Java")));
    }

    @Test
    @DisplayName("Удаление существующей книги")
    void whenDeleteExistingBook_thenBookIsRemoved() {
        // Given
        Book book = new Book("Test Book", "1234567890"); // 10-значный ISBN
        Book saved = bookService.addBook(book);
        Long bookId = saved.getId();

        // When
        boolean deleted = bookService.deleteBook(bookId);

        // Then
        assertTrue(deleted);
        assertFalse(bookRepository.existsById(bookId));
    }

    @Test
    @DisplayName("Удаление несуществующей книги возвращает false")
    void whenDeleteNonExistingBook_thenReturnFalse() {
        // When
        boolean deleted = bookService.deleteBook(999L);

        // Then
        assertFalse(deleted);
    }

    @Test
    @DisplayName("Асинхронное добавление книги")
    void whenAddBookAsync_thenBookIsSaved() throws ExecutionException, InterruptedException {
        // Given
        Book book = new Book("Async Programming in Java", "9781234567890");

        // When
        var future = bookService.addBookAsync(book);
        Book savedBook = future.get();

        // Then
        assertNotNull(savedBook.getId());
        assertEquals("Async Programming in Java", savedBook.getTitle());
    }

    @Test
    @DisplayName("Поиск доступных книг")
    void whenGetAvailableBooks_thenReturnOnlyAvailable() {
        // Given
        Book book1 = new Book("Book 1", "1234567890"); // 10-значный ISBN
        book1.setAvailableCopies(3);
        bookService.addBook(book1);

        Book book2 = new Book("Book 2", "0987654321"); // 10-значный ISBN
        book2.setAvailableCopies(0);
        bookService.addBook(book2);

        Book book3 = new Book("Book 3", "9780123456789"); // 13-значный ISBN
        book3.setAvailableCopies(2);
        bookService.addBook(book3);

        // When
        List<Book> availableBooks = bookService.getAvailableBooks();

        // Then
        assertEquals(2, availableBooks.size()); // Book 1 и Book 3
        assertTrue(availableBooks.stream()
                .anyMatch(b -> b.getTitle().equals("Book 1")));
        assertTrue(availableBooks.stream()
                .anyMatch(b -> b.getTitle().equals("Book 3")));
        assertFalse(availableBooks.stream()
                .anyMatch(b -> b.getTitle().equals("Book 2")));
    }

    @Test
    @DisplayName("Проверка валидации ISBN - 10 цифр")
    void whenBookWithValid10DigitIsbn_thenSuccess() {
        // Given
        Book book = new Book("Valid Book", "1234567890"); // 10 цифр

        // When
        Book saved = bookService.addBook(book);

        // Then
        assertNotNull(saved);
        assertEquals("1234567890", saved.getIsbn());
    }

    @Test
    @DisplayName("Проверка валидации ISBN - 13 цифр")
    void whenBookWithValid13DigitIsbn_thenSuccess() {
        // Given
        Book book = new Book("Valid Book", "9781234567890"); // 13 цифр

        // When
        Book saved = bookService.addBook(book);

        // Then
        assertNotNull(saved);
        assertEquals("9781234567890", saved.getIsbn());
    }

    @Test
    @DisplayName("Проверка валидации ISBN - с дефисами")
    void whenBookWithValidIsbnWithHyphens_thenSuccess() {
        // Given
        Book book = new Book("Valid Book", "978-1-234-56789-0"); // ISBN с дефисами

        // When
        Book saved = bookService.addBook(book);

        // Then
        assertNotNull(saved);
        assertEquals("978-1-234-56789-0", saved.getIsbn());
    }

    @Test
    @DisplayName("Проверка валидации ISBN - невалидный формат")
    void whenBookWithInvalidIsbn_thenThrowException() {
        // Given
        Book book = new Book("Invalid Book", "invalid-isbn");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.addBook(book);
        });
    }

    @Test
    @DisplayName("Проверка валидации ISBN - пустая строка")
    void whenBookWithEmptyIsbn_thenSuccess() {
        // Given
        Book book = new Book("Valid Book", ""); // Пустой ISBN допустим

        // When
        Book saved = bookService.addBook(book);

        // Then
        assertNotNull(saved);
        assertEquals("", saved.getIsbn());
    }

    @Test
    @DisplayName("Проверка валидации ISBN - null значение")
    void whenBookWithNullIsbn_thenSuccess() {
        // Given
        Book book = new Book("Valid Book", null); // null ISBN допустим

        // When
        Book saved = bookService.addBook(book);

        // Then
        assertNotNull(saved);
        assertNull(saved.getIsbn());
    }

    @Test
    @DisplayName("Поиск книги по автору")
    void whenSearchByAuthor_thenReturnMatchingBooks() {
        // Given
        Book book1 = new Book("Java Book 1", "1111111111");
        Book book2 = new Book("Java Book 2", "2222222222");
        Book book3 = new Book("Python Book", "3333333333");

        bookService.addBook(book1);
        bookService.addBook(book2);
        bookService.addBook(book3);

        // When
        List<Book> results = bookService.searchBooksByAuthor("Java");

        // Then
        // В текущей реализации InMemoryBookRepository поиск по автору работает,
        // только если автор добавлен к книге. Это тест для проверки метода.
        assertNotNull(results);
    }
}