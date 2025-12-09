package com.library.core.repository.user.impl;

import com.library.core.domain.user.User;
import com.library.core.repository.user.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, Long> usernameIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> libraryCardIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }

        // Проверка уникальности username
        if (user.getUsername() != null) {
            Long existingId = usernameIndex.get(user.getUsername());
            if (existingId != null && !existingId.equals(user.getId())) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }
            usernameIndex.put(user.getUsername(), user.getId());
        }

        // Проверка уникальности email
        if (user.getEmail() != null) {
            Long existingId = emailIndex.get(user.getEmail());
            if (existingId != null && !existingId.equals(user.getId())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
            emailIndex.put(user.getEmail(), user.getId());
        }

        // Для Reader проверяем уникальность номера читательского билета
        if (user instanceof com.library.core.domain.user.Reader) {
            com.library.core.domain.user.Reader reader = (com.library.core.domain.user.Reader) user;
            if (reader.getLibraryCardNumber() != null) {
                Long existingId = libraryCardIndex.get(reader.getLibraryCardNumber());
                if (existingId != null && !existingId.equals(user.getId())) {
                    throw new IllegalArgumentException("Library card number already exists: " +
                            reader.getLibraryCardNumber());
                }
                libraryCardIndex.put(reader.getLibraryCardNumber(), user.getId());
            }
        }

        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(User user) {
        if (user.getId() != null) {
            storage.remove(user.getId());

            if (user.getUsername() != null) {
                usernameIndex.remove(user.getUsername());
            }

            if (user.getEmail() != null) {
                emailIndex.remove(user.getEmail());
            }

            if (user instanceof com.library.core.domain.user.Reader) {
                com.library.core.domain.user.Reader reader = (com.library.core.domain.user.Reader) user;
                if (reader.getLibraryCardNumber() != null) {
                    libraryCardIndex.remove(reader.getLibraryCardNumber());
                }
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        User user = storage.get(id);
        if (user != null) {
            delete(user);
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
    public Optional<User> findByUsername(String username) {
        Long id = usernameIndex.get(username);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Long id = emailIndex.get(email);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByRole(String role) {
        return storage.values().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findActiveUsers() {
        return storage.values().stream()
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByLastName(String lastName) {
        return storage.values().stream()
                .filter(user -> user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByLibraryCardNumber(String libraryCardNumber) {
        Long id = libraryCardIndex.get(libraryCardNumber);
        if (id != null) {
            return Optional.ofNullable(storage.get(id));
        }
        return Optional.empty();
    }

    @Override
    public long countByRole(String role) {
        return storage.values().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .count();
    }
}