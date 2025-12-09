package com.library.core.service;

import com.library.core.domain.user.AdminUser;
import com.library.core.domain.user.LibrarianUser;
import com.library.core.domain.user.Reader;
import com.library.core.domain.user.User;
import com.library.core.domain.user.UserRole;
import com.library.core.repository.user.UserRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сервис для аутентификации и управления пользователями
 */
public class AuthService {

    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private User currentUser; // Текущий авторизованный пользователь

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        User user = userOpt.get();

        if (!user.isAccountActive()) {
            throw new IllegalStateException("Учетная запись заблокирована или неактивна");
        }

        if (user.authenticate(password)) {
            userRepository.save(user);
            this.currentUser = user;
            return user;
        } else {
            userRepository.save(user);
            throw new IllegalArgumentException("Неверный пароль");
        }
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean hasPermission(UserRole requiredRole) {
        return currentUser != null && currentUser.getRole().hasPermission(requiredRole);
    }

    public boolean canPerformAction(String action) {
        return currentUser != null && currentUser.canPerformAction(action);
    }

    // Регистрация разных типов пользователей
    public User registerReader(Reader reader) {
        validateUserRegistration(reader);

        // Проверка уникальности username и email
        if (userRepository.findByUsername(reader.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }

        if (userRepository.findByEmail(reader.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email уже используется");
        }

        reader.setRole(UserRole.READER);
        reader.setRegistrationDate(LocalDate.now());
        reader.setPasswordResetRequired(true); // Требуется смена пароля при первом входе

        return userRepository.save(reader);
    }

    public User registerLibrarian(LibrarianUser librarian) {
        // Только администратор может регистрировать библиотекарей
        if (!hasPermission(UserRole.ADMINISTRATOR)) {
            throw new SecurityException("Недостаточно прав для регистрации библиотекаря");
        }

        validateUserRegistration(librarian);

        if (userRepository.findByUsername(librarian.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }

        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setRegistrationDate(LocalDate.now());
        librarian.setPasswordResetRequired(true);

        return userRepository.save(librarian);
    }

    public User registerAdmin(AdminUser admin) {
        // Только существующий администратор может создавать новых администраторов
        if (!hasPermission(UserRole.ADMINISTRATOR)) {
            throw new SecurityException("Недостаточно прав для регистрации администратора");
        }

        validateUserRegistration(admin);

        admin.setRole(UserRole.ADMINISTRATOR);
        admin.setRegistrationDate(LocalDate.now());

        return userRepository.save(admin);
    }

    private void validateUserRegistration(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя обязательно");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }

        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }

        if (user.getEmail() != null && !isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Неверный формат email");
        }
    }

    // Асинхронные методы
    public CompletableFuture<User> loginAsync(String username, String password) {
        return CompletableFuture.supplyAsync(() -> login(username, password), executorService);
    }

    public CompletableFuture<User> registerReaderAsync(Reader reader) {
        return CompletableFuture.supplyAsync(() -> registerReader(reader), executorService);
    }

    // Вспомогательные методы
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public void changeCurrentUserPassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        currentUser.changePassword(oldPassword, newPassword);
        userRepository.save(currentUser);
    }

    public void resetUserPassword(Long userId, String newPassword) {
        // Только администратор может сбрасывать пароли других пользователей
        if (!hasPermission(UserRole.ADMINISTRATOR)) {
            throw new SecurityException("Недостаточно прав для сброса пароля");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        User user = userOpt.get();
        user.resetPassword(newPassword);
        userRepository.save(user);
    }

    public void unlockUserAccount(Long userId) {
        if (!hasPermission(UserRole.ADMINISTRATOR)) {
            throw new SecurityException("Недостаточно прав для разблокировки учетной записи");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        User user = userOpt.get();
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}