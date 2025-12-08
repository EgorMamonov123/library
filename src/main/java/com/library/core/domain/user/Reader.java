package com.library.core.domain.user;

import com.library.core.domain.BaseEntity;
import com.library.core.domain.loan.BorrowRecord;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Reader extends BaseEntity {

    private String libraryCardNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private Boolean active = true;
    private Set<BorrowRecord> borrowRecords = new HashSet<>();

    public Reader() {}

    public Reader(String libraryCardNumber, String firstName, String lastName) {
        this.libraryCardNumber = libraryCardNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationDate = LocalDate.now();
    }

    // Business methods
    public boolean canBorrowBooks() {
        if (!active) {
            return false;
        }

        long activeBorrows = borrowRecords.stream()
                .filter(record -> record.getReturnDate() == null)
                .count();

        return activeBorrows < 5; // Максимум 5 книг одновременно
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getters and Setters
    public String getLibraryCardNumber() {
        return libraryCardNumber;
    }

    public void setLibraryCardNumber(String libraryCardNumber) {
        this.libraryCardNumber = libraryCardNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    public void setBorrowRecords(Set<BorrowRecord> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }
}