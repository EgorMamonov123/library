package com.library.core.service;

import com.library.core.domain.user.Reader;
import com.library.core.repository.user.ReaderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    public Reader registerReader(Reader reader) {
        validateReader(reader);

        // Проверка уникальности номера читательского билета
        if (readerRepository.findByLibraryCardNumber(reader.getLibraryCardNumber()).isPresent()) {
            throw new IllegalArgumentException("Номер читательского билета уже используется");
        }

        reader.setRegistrationDate(LocalDate.now());
        reader.setActive(true);

        return readerRepository.save(reader);
    }

    public Optional<Reader> getReaderById(Long id) {
        return readerRepository.findById(id);
    }

    public Optional<Reader> getReaderByLibraryCard(String libraryCardNumber) {
        return readerRepository.findByLibraryCardNumber(libraryCardNumber);
    }

    public List<Reader> findReadersByLastName(String lastName) {
        return readerRepository.findByLastName(lastName);
    }

    public List<Reader> getActiveReaders() {
        return readerRepository.findActiveReaders();
    }

    public Reader updateReaderInfo(Long readerId, String email, String phone) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("Читатель не найден"));

        if (email != null && !isValidEmail(email)) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        reader.setEmail(email);
        reader.setPhone(phone);

        return readerRepository.save(reader);
    }

    public boolean deactivateReader(Long readerId) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        if (readerOpt.isPresent()) {
            Reader reader = readerOpt.get();
            reader.setActive(false);
            readerRepository.save(reader);
            return true;
        }
        return false;
    }

    public boolean activateReader(Long readerId) {
        Optional<Reader> readerOpt = readerRepository.findById(readerId);
        if (readerOpt.isPresent()) {
            Reader reader = readerOpt.get();
            reader.setActive(true);
            readerRepository.save(reader);
            return true;
        }
        return false;
    }

    public long getReaderCount() {
        return readerRepository.count();
    }

    private void validateReader(Reader reader) {
        if (reader.getLibraryCardNumber() == null || reader.getLibraryCardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Номер читательского билета обязателен");
        }

        if (reader.getFirstName() == null || reader.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя обязательно");
        }

        if (reader.getLastName() == null || reader.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия обязательна");
        }

        if (reader.getEmail() != null && !isValidEmail(reader.getEmail())) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        if (reader.getRegistrationDate() != null &&
                reader.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата регистрации не может быть в будущем");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public void shutdown() {

    }
}