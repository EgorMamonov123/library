package com.library.core.repository.book.impl;

import com.library.core.domain.book.Genre;
import com.library.core.repository.book.GenreRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryGenreRepository implements GenreRepository {

    private final Map<Long, Genre> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> nameIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<Genre> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Genre save(Genre genre) {
        if (genre.getId() == null) {
            genre.setId(idGenerator.getAndIncrement());
        }

        // Проверка уникальности названия жанра
        if (genre.getName() != null) {
            String key = genre.getName().toLowerCase();
            Long existingId = nameIndex.get(key);

            if (existingId != null && !existingId.equals(genre.getId())) {
                throw new IllegalArgumentException(
                        "Genre with name '" + genre.getName() + "' already exists"
                );
            }

            nameIndex.put(key, genre.getId());
        }

        storage.put(genre.getId(), genre);
        return genre;
    }

    @Override
    public void delete(Genre genre) {
        if (genre.getId() != null) {
            storage.remove(genre.getId());
            if (genre.getName() != null) {
                nameIndex.remove(genre.getName().toLowerCase());
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Genre genre = storage.get(id);
        if (genre != null) {
            delete(genre);
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
    public Optional<Genre> findByName(String name) {
        if (name == null) return Optional.empty();

        Long id = nameIndex.get(name.toLowerCase());
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Genre> findGenresWithBooks() {
        return storage.values().stream()
                .filter(genre -> genre.getBooks() != null && !genre.getBooks().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<Genre> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String term = searchTerm.toLowerCase();
        return storage.values().stream()
                .filter(genre -> genre.getName() != null && genre.getName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }
}