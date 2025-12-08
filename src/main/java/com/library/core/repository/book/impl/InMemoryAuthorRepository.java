package com.library.core.repository.book.impl;

import com.library.core.domain.book.Author;
import com.library.core.repository.book.AuthorRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryAuthorRepository implements AuthorRepository {

    private final Map<Long, Author> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> nameIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Author> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Author save(Author author) {
        if (author.getId() == null) {
            author.setId(idGenerator.getAndIncrement());
        }

        // Создаем ключ для индекса по имени
        String key = (author.getFirstName() + " " + author.getLastName()).toLowerCase();
        Long existingId = nameIndex.get(key);

        if (existingId != null && !existingId.equals(author.getId())) {
            throw new IllegalArgumentException(
                    "Author with name " + author.getFirstName() + " " + author.getLastName() + " already exists"
            );
        }

        storage.put(author.getId(), author);
        nameIndex.put(key, author.getId());

        return author;
    }

    @Override
    public void delete(Author author) {
        if (author.getId() != null) {
            storage.remove(author.getId());
            String key = (author.getFirstName() + " " + author.getLastName()).toLowerCase();
            nameIndex.remove(key);
        }
    }

    @Override
    public void deleteById(Long id) {
        Author author = storage.get(id);
        if (author != null) {
            delete(author);
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
    public Optional<Author> findByFirstNameAndLastName(String firstName, String lastName) {
        String key = (firstName + " " + lastName).toLowerCase();
        Long id = nameIndex.get(key);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Author> findByLastName(String lastName) {
        return storage.values().stream()
                .filter(author -> author.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Author> findByCountry(String country) {
        return storage.values().stream()
                .filter(author -> country == null || author.getCountry() == null ?
                        country == author.getCountry() :
                        author.getCountry().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

    @Override
    public List<Author> findAuthorsWithMultipleBooks(int minBooks) {
        return storage.values().stream()
                .filter(author -> author.getBooks() != null && author.getBooks().size() >= minBooks)
                .collect(Collectors.toList());
    }
}