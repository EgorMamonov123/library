package com.library.core.repository.user.impl;

import com.library.core.domain.user.Reader;
import com.library.core.repository.user.ReaderRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryReaderRepository implements ReaderRepository {

    private final Map<Long, Reader> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> libraryCardIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<Reader> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Reader> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Reader save(Reader reader) {
        if (reader.getId() == null) {
            reader.setId(idGenerator.getAndIncrement());
        }

        // Проверка уникальности номера читательского билета
        if (reader.getLibraryCardNumber() != null) {
            Long existingId = libraryCardIndex.get(reader.getLibraryCardNumber());
            if (existingId != null && !existingId.equals(reader.getId())) {
                throw new IllegalArgumentException("Library card number already exists: " +
                        reader.getLibraryCardNumber());
            }
            libraryCardIndex.put(reader.getLibraryCardNumber(), reader.getId());
        }

        storage.put(reader.getId(), reader);
        return reader;
    }

    @Override
    public void delete(Reader reader) {
        if (reader.getId() != null) {
            storage.remove(reader.getId());
            if (reader.getLibraryCardNumber() != null) {
                libraryCardIndex.remove(reader.getLibraryCardNumber());
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        Reader reader = storage.get(id);
        if (reader != null) {
            delete(reader);
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
    public Optional<Reader> findByLibraryCardNumber(String libraryCardNumber) {
        Long id = libraryCardIndex.get(libraryCardNumber);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<Reader> findByLastName(String lastName) {
        return storage.values().stream()
                .filter(reader -> reader.getLastName() != null &&
                        reader.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reader> findActiveReaders() {
        return storage.values().stream()
                .filter(reader -> Boolean.TRUE.equals(reader.getActive()))
                .collect(Collectors.toList());
    }

    @Override
    public long countReaders() {
        return storage.size();
    }
}