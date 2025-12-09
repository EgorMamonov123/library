package com.library.core.domain.user;

import com.library.core.domain.BaseEntity;
import com.library.core.domain.loan.BorrowRecord;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("READER")
public class Reader extends User {

    @Column(name = "library_card_number", unique = true)
    private String libraryCardNumber;

    @Transient
    private Set<BorrowRecord> borrowRecords = new HashSet<>();

    public Reader() {
        super();
    }

    // Конструктор для тестов
    public Reader(String libraryCardNumber, String firstName, String lastName) {
        this();
        this.libraryCardNumber = libraryCardNumber;
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setUsername(libraryCardNumber); // Используем номер билета как username
        this.setPassword("default123"); // Дефолтный пароль
        this.setRole(UserRole.READER);
        this.setRegistrationDate(LocalDate.now());
        this.setActive(true);
    }

    // Конструктор для AuthService
    public Reader(String username, String password, String email,
                  String firstName, String lastName, String libraryCardNumber) {
        super(username, password, email, firstName, lastName, UserRole.READER);
        this.libraryCardNumber = libraryCardNumber;
    }

    @Override
    public boolean canPerformAction(String action) {
        switch (action) {
            case "borrow_books":
            case "reserve_books":
            case "view_loans":
            case "renew_loans":
                return true;
            case "manage_books":
            case "manage_readers":
            case "process_loans":
            case "view_statistics":
            case "manage_users":
            case "system_settings":
                return false;
            default:
                return false;
        }
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getLastName() + " (Читатель)";
    }

    // Business methods
    public boolean canBorrowBooks() {
        if (!getActive()) {
            return false;
        }

        // Здесь должна быть логика проверки количества активных займов
        // Временная заглушка
        return true;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    // Getters and Setters
    public String getLibraryCardNumber() {
        return libraryCardNumber;
    }

    public void setLibraryCardNumber(String libraryCardNumber) {
        this.libraryCardNumber = libraryCardNumber;
    }

    public Set<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    public void setBorrowRecords(Set<BorrowRecord> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }
}