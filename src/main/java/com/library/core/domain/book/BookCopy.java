package com.library.core.domain.book;

import com.library.core.domain.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy extends BaseEntity {

    @Column(name = "inventory_number", unique = true, nullable = false)
    private String inventoryNumber;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(name = "condition")
    private String condition = "Good";

    @Column(name = "acquisition_date")
    private java.time.LocalDate acquisitionDate;

    // Enum для статусов экземпляра
    public enum CopyStatus {
        AVAILABLE,
        BORROWED,
        RESERVED,
        UNDER_REPAIR,
        LOST,
        WITHDRAWN
    }

    // Constructors
    public BookCopy() {}

    public BookCopy(String inventoryNumber, Book book) {
        this.inventoryNumber = inventoryNumber;
        this.book = book;
        this.acquisitionDate = java.time.LocalDate.now();
    }

    // Getters and Setters
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public CopyStatus getStatus() { return status; }
    public void setStatus(CopyStatus status) { this.status = status; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public java.time.LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(java.time.LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
}