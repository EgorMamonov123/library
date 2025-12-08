package com.library.core.repository.book.impl;

import com.library.core.domain.book.Book;
import com.library.core.repository.book.BookRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryBookRepository implements BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> isbnIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idGenerator.getAndIncrement());
        }

        // Проверка уникальности ISBN
        if (book.getIsbn() != null) {
            Long existingId = isbnIndex.get(book.getIsbn());
            if (existingId != null && !existingId.equals(book.getId())) {
                throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
            }
            isbnIndex.put(book.getIsbn(), book.getId());
        }

        storage.put(book.getId(), book);
        return book;
    }

    @Override
    public void delete(Book book) {
        if (book.getId() != null) {
            storage.remove(book.getId());
            if (book.getIsbn() != null) {
                isbnIndex.remove(book.getIsbn());
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Book book = storage.get(id);
        if (book != null) {
            delete(book);
        }
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        Long id = isbnIndex.get(isbn);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        String searchTerm = title.toLowerCase();
        return storage.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByAuthorName(String authorName) {
        String searchTerm = authorName.toLowerCase();
        return storage.values().stream()
                .filter(book -> book.getAuthors().stream()
                        .anyMatch(author ->
                                author.getFirstName().toLowerCase().contains(searchTerm) ||
                                        author.getLastName().toLowerCase().contains(searchTerm) ||
                                        author.getFullName().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByGenre(String genreName) {
        String searchTerm = genreName.toLowerCase();
        return storage.values().stream()
                .filter(book -> book.getGenre() != null &&
                        book.getGenre().getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findAvailableBooks() {
        return storage.values().stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findBooksPublishedAfter(int year) {
        return storage.values().stream()
                .filter(book -> book.getPublicationYear() != null &&
                        book.getPublicationYear().getValue() > year)
                .collect(Collectors.toList());
    }
}