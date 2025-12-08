package com.library.core.service;

import com.library.core.domain.user.Reader;
import com.library.core.repository.user.ReaderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderService {

    private final ReaderRepository readerRepository;
    private final ExecutorService executorService;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public Reader registerReader(Reader reader) {
        validateReader(reader);
        return readerRepository.save(reader);
    }

    public Optional<Reader> getReaderById(Long id) {
        return readerRepository.findById(id);
    }

    public Optional<Reader> getReaderByLibraryCard(String cardNumber) {
        return readerRepository.findByLibraryCardNumber(cardNumber);
    }

    public List<Reader> findReadersByLastName(String lastName) {
        return readerRepository.findByLastName(lastName);
    }

    public List<Reader> getActiveReaders() {
        return readerRepository.findActiveReaders();
    }

    public Reader updateReaderInfo(Long readerId, String email, String phone) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        if (!readerOpt.isPresent()) {
            throw new IllegalArgumentException("Reader not found with id: " + readerId);
        }

        Reader reader = readerOpt.get();
        reader.setEmail(email);
        reader.setPhone(phone);

        return readerRepository.save(reader);
    }

    public boolean deactivateReader(Long readerId) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        if (!readerOpt.isPresent()) {
            return false;
        }

        Reader reader = readerOpt.get();
        reader.setActive(false);
        readerRepository.save(reader);
        return true;
    }

    public boolean activateReader(Long readerId) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        if (!readerOpt.isPresent()) {
            return false;
        }

        Reader reader = readerOpt.get();
        reader.setActive(true);
        readerRepository.save(reader);
        return true;
    }

    public long getReaderCount() {
        return readerRepository.count();
    }

    // Асинхронные методы
    public CompletableFuture<Reader> registerReaderAsync(Reader reader) {
        return CompletableFuture.supplyAsync(() -> registerReader(reader), executorService);
    }

    public CompletableFuture<List<Reader>> findReadersAsync(String searchTerm) {
        return CompletableFuture.supplyAsync(() -> {
            List<Reader> byLastName = readerRepository.findByLastName(searchTerm);
            // Можно добавить поиск по имени или другим полям
            return byLastName;
        }, executorService);
    }

    // Валидация
    private void validateReader(Reader reader) {
        if (reader.getLibraryCardNumber() == null || reader.getLibraryCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Library card number cannot be empty");
        }

        if (reader.getFirstName() == null || reader.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }

        if (reader.getLastName() == null || reader.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        if (reader.getEmail() != null && !isValidEmail(reader.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (reader.getRegistrationDate() != null &&
                reader.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Registration date cannot be in the future");
        }
    }

    private boolean isValidEmail(String email) {
        // Простая проверка email
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public void shutdown() {
        executorService.shutdown();
    }
}