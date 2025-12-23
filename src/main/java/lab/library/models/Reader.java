package lab.library.models;

import lab.library.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "readers")
@Data
@NoArgsConstructor
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String libraryCardNumber;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @Column
    private String phoneNumber;

    @Column(nullable = false)
    private Integer maxBooksAllowed = 5; // Максимальное количество книг одновременно

    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.READER;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }

    public Reader(String firstName, String lastName, String email, String libraryCardNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.libraryCardNumber = libraryCardNumber;
        this.registrationDate = LocalDate.now();
    }
}