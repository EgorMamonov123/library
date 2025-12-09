package com.library.core.domain.book;

/**
 * Статусы экземпляра книги
 */
public enum CopyStatus {
    AVAILABLE("Доступна"),
    BORROWED("Выдана"),
    RESERVED("Зарезервирована"),
    UNDER_REPAIR("На ремонте"),
    LOST("Утеряна"),
    WITHDRAWN("Списана");

    private final String description;

    CopyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isBorrowable() {
        return this == AVAILABLE;
    }

    public boolean isReservable() {
        return this == AVAILABLE;
    }
}