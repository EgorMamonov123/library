package lab.library.models;

import lab.library.enums.BookStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column
    private String publisher;

    @Column
    private Integer publicationYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;

    @Column
    private String genre;

    @Column
    private Integer pages;

    @Column(length = 1000)
    private String description;

    @Column
    private LocalDate addedDate;

    @Column
    private Integer totalCopies = 1;

    @Column
    private Integer availableCopies = 1;

    @PrePersist
    protected void onCreate() {
        if (addedDate == null) {
            addedDate = LocalDate.now();
        }
        if (availableCopies == null) {
            availableCopies = totalCopies;
        }
    }

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.addedDate = LocalDate.now();
    }
}