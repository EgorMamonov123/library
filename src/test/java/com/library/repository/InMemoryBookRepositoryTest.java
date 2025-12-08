package com.library.repository;

import com.library.core.domain.book.Author;
import com.library.core.domain.book.Book;
import com.library.core.domain.book.Genre;
import com.library.core.repository.book.impl.InMemoryBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBookRepositoryTest {

    private InMemoryBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
    }

    @Test
    void whenSaveBook_thenBookIsStored() {
        // Given
        Book book = new Book("Test Book", "1234567890");

        // When
        Book saved = repository.save(book);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Book", saved.getTitle());
        assertEquals("1234567890", saved.getIsbn());
        assertTrue(repository.existsById(saved.getId()));
    }

    @Test
    void whenSaveBookWithDuplicateIsbn_thenThrowException() {
        // Given
        Book book1 = new Book("Book 1", "1234567890");
        repository.save(book1);

        Book book2 = new Book("Book 2", "1234567890");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(book2);
        });
    }

    @Test
    void whenFindById_thenReturnCorrectBook() {
        // Given
        Book book = new Book("Test Book", "1234567890");
        Book saved = repository.save(book);
        Long bookId = saved.getId();

        // When
        Optional<Book> found = repository.findById(bookId);

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().getTitle());
        assertEquals(bookId, found.get().getId());
    }

    @Test
    void whenFindByIsbn_thenReturnCorrectBook() {
        // Given
        Book book = new Book("Test Book", "9781234567890");
        repository.save(book);

        // When
        Optional<Book> found = repository.findByIsbn("9781234567890");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().getTitle());
    }

    @Test
    void whenFindByTitleContaining_thenReturnMatchingBooks() {
        // Given
        repository.save(new Book("Java Programming", "1111111111"));
        repository.save(new Book("Advanced Java", "2222222222"));
        repository.save(new Book("Python Basics", "3333333333"));

        // When
        List<Book> results = repository.findByTitleContaining("Java");

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getTitle().contains("Java")));
    }

    @Test
    void whenFindByAuthorName_thenReturnBooksWithAuthor() {
        // Given
        Book book = new Book("Test Book", "1234567890");
        Author author = new Author("John", "Doe");
        book.addAuthor(author);
        repository.save(book);

        // When
        List<Book> results = repository.findByAuthorName("Doe");

        // Then
        assertEquals(1, results.size());
        assertEquals("Test Book", results.get(0).getTitle());
    }

    @Test
    void whenFindByGenre_thenReturnBooksWithGenre() {
        // Given
        Book book = new Book("Test Book", "1234567890");
        Genre genre = new Genre("Fiction");
        book.setGenre(genre);
        repository.save(book);

        // When
        List<Book> results = repository.findByGenre("Fiction");

        // Then
        assertEquals(1, results.size());
        assertEquals("Test Book", results.get(0).getTitle());
    }

    @Test
    void whenFindAvailableBooks_thenReturnOnlyAvailable() {
        // Given
        Book book1 = new Book("Book 1", "1111111111");
        book1.setAvailableCopies(3);

        Book book2 = new Book("Book 2", "2222222222");
        book2.setAvailableCopies(0);

        repository.save(book1);
        repository.save(book2);

        // When
        List<Book> availableBooks = repository.findAvailableBooks();

        // Then
        assertEquals(1, availableBooks.size());
        assertEquals("Book 1", availableBooks.get(0).getTitle());
    }

    @Test
    void whenFindBooksPublishedAfter_thenReturnCorrectBooks() {
        // Given
        Book book1 = new Book("Book 1", "1111111111");
        book1.setPublicationYear(Year.of(2020));

        Book book2 = new Book("Book 2", "2222222222");
        book2.setPublicationYear(Year.of(2015));

        repository.save(book1);
        repository.save(book2);

        // When
        List<Book> recentBooks = repository.findBooksPublishedAfter(2018);

        // Then
        assertEquals(1, recentBooks.size());
        assertEquals("Book 1", recentBooks.get(0).getTitle());
    }

    @Test
    void whenDeleteBook_thenBookIsRemoved() {
        // Given
        Book book = new Book("Test Book", "1234567890");
        Book saved = repository.save(book);
        Long bookId = saved.getId();

        // When
        repository.deleteById(bookId);

        // Then
        assertFalse(repository.existsById(bookId));
        assertEquals(0, repository.count());
    }

    @Test
    void whenFindAll_thenReturnAllBooks() {
        // Given
        repository.save(new Book("Book 1", "1111111111"));
        repository.save(new Book("Book 2", "2222222222"));
        repository.save(new Book("Book 3", "3333333333"));

        // When
        List<Book> allBooks = repository.findAll();

        // Then
        assertEquals(3, allBooks.size());
    }

    @Test
    void whenUpdateBook_thenChangesAreSaved() {
        // Given
        Book book = new Book("Old Title", "1234567890");
        Book saved = repository.save(book);

        // When
        saved.setTitle("New Title");
        saved.setAvailableCopies(5);
        repository.save(saved);

        // Then
        Optional<Book> updated = repository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
        assertEquals(5, updated.get().getAvailableCopies());
    }
}