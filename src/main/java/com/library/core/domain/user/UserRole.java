package com.library.core.domain.user;

/**
 * Роли пользователей системы
 */
public enum UserRole {
    READER("Читатель", 1),
    LIBRARIAN("Библиотекарь", 2),
    ADMINISTRATOR("Администратор", 3);

    private final String displayName;
    private final int priority; // для сравнения прав доступа

    UserRole(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasPermission(UserRole requiredRole) {
        return this.priority >= requiredRole.priority;
    }

    public boolean canManageUsers() {
        return this == ADMINISTRATOR;
    }

    public boolean canManageBooks() {
        return this == LIBRARIAN || this == ADMINISTRATOR;
    }

    public boolean canBorrowBooks() {
        return this == READER || this == LIBRARIAN || this == ADMINISTRATOR;
    }

    public boolean canViewStatistics() {
        return this == LIBRARIAN || this == ADMINISTRATOR;
    }

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return READER; // По умолчанию
        }
    }
}