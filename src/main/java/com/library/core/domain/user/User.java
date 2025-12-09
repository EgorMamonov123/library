package com.library.core.domain.user;

import com.library.core.domain.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Абстрактный базовый класс для всех пользователей системы
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password; // В реальном приложении должен быть хэширован

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "password_reset_required")
    private Boolean passwordResetRequired = false;

    // Конструкторы
    public User() {}

    public User(String username, String password, String email,
                String firstName, String lastName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.registrationDate = LocalDate.now();
    }

    // Абстрактные методы
    public abstract boolean canPerformAction(String action);

    public abstract String getDisplayName();

    // Общие методы для всех пользователей
    public boolean authenticate(String password) {
        // В реальном приложении здесь должна быть проверка хэша
        if (this.password.equals(password)) {
            this.lastLoginDate = LocalDate.now();
            this.failedLoginAttempts = 0;
            return true;
        } else {
            this.failedLoginAttempts++;
            if (this.failedLoginAttempts >= 5) {
                this.accountLocked = true;
            }
            return false;
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!authenticate(oldPassword)) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }
        this.password = newPassword;
        this.passwordResetRequired = false;
    }

    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.passwordResetRequired = false;
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
    }

    public boolean isAccountActive() {
        return Boolean.TRUE.equals(active) && !Boolean.TRUE.equals(accountLocked);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDate getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Boolean getPasswordResetRequired() { return passwordResetRequired; }
    public void setPasswordResetRequired(Boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }
}