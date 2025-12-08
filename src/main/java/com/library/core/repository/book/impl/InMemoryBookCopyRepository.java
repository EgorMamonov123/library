package com.library.core.repository.book.impl;

import com.library.core.domain.book.BookCopy;
import com.library.core.repository.book.BookCopyRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryBookCopyRepository implements BookCopyRepository {

    private final Map<Long, BookCopy> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> inventoryNumberIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<BookCopy> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<BookCopy> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public BookCopy save(BookCopy bookCopy) {
        if (bookCopy.getId() == null) {
            bookCopy.setId(idGenerator.getAndIncrement());
        }

        // Проверка уникальности инвентарного номера
        if (bookCopy.getInventoryNumber() != null) {
            Long existingId = inventoryNumberIndex.get(bookCopy.getInventoryNumber());
            if (existingId != null && !existingId.equals(bookCopy.getId())) {
                throw new IllegalArgumentException(
                        "BookCopy with inventory number " + bookCopy.getInventoryNumber() + " already exists"
                );
            }
            inventoryNumberIndex.put(bookCopy.getInventoryNumber(), bookCopy.getId());
        }

        storage.put(bookCopy.getId(), bookCopy);
        return bookCopy;
    }

    @Override
    public void delete(BookCopy bookCopy) {
        if (bookCopy.getId() != null) {
            storage.remove(bookCopy.getId());
            if (bookCopy.getInventoryNumber() != null) {
                inventoryNumberIndex.remove(bookCopy.getInventoryNumber());
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        BookCopy bookCopy = storage.get(id);
        if (bookCopy != null) {
            delete(bookCopy);
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
    public Optional<BookCopy> findByInventoryNumber(String inventoryNumber) {
        Long id = inventoryNumberIndex.get(inventoryNumber);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<BookCopy> findByBookId(Long bookId) {
        return storage.values().stream()
                .filter(copy -> copy.getBook() != null && copy.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookCopy> findByStatus(BookCopy.CopyStatus status) {
        return storage.values().stream()
                .filter(copy -> copy.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookCopy> findAvailableCopiesByBookId(Long bookId) {
        return storage.values().stream()
                .filter(copy -> copy.getBook() != null &&
                        copy.getBook().getId().equals(bookId) &&
                        copy.getStatus() == BookCopy.CopyStatus.AVAILABLE)
                .collect(Collectors.toList());
    }
}