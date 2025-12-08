package com.library.core.domain.loan;

import com.library.core.domain.BaseEntity;
import com.library.core.domain.book.BookCopy;
import com.library.core.domain.user.Reader;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount")
    private Double fineAmount = 0.0;

    @Column(name = "fine_paid")
    private Boolean finePaid = false;

    @Column(name = "notes")
    private String notes;

    // Constructors
    public BorrowRecord() {}

    public BorrowRecord(Reader reader, BookCopy bookCopy, LocalDate borrowDate, LocalDate dueDate) {
        this.reader = reader;
        this.bookCopy = bookCopy;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    // Business methods
    public boolean isOverdue() {
        if (returnDate != null) {
            return returnDate.isAfter(dueDate);
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public long calculateOverdueDays() {
        LocalDate comparisonDate = (returnDate != null) ? returnDate : LocalDate.now();
        if (comparisonDate.isAfter(dueDate)) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, comparisonDate);
        }
        return 0;
    }

    public double calculateFine() {
        long overdueDays = calculateOverdueDays();
        return overdueDays > 0 ? overdueDays * 10.0 : 0.0; // 10 рублей в день
    }

    // Getters and Setters
    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }

    public BookCopy getBookCopy() { return bookCopy; }
    public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }

    public Boolean getFinePaid() { return finePaid; }
    public void setFinePaid(Boolean finePaid) { this.finePaid = finePaid; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}