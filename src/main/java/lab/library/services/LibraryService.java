package lab.library.services;

import lab.library.enums.*;
import lab.library.models.*;
import lab.library.repositories.*;
import lab.library.exceptions.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;

    // ================= КНИГИ =================

    public Book addBook(String title, String author, String isbn, String publisher,
                        Integer publicationYear, String genre, Integer pages,
                        String description, Integer totalCopies) {

        Book book = new Book(title, author, isbn);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setPages(pages);
        book.setDescription(description);
        book.setTotalCopies(totalCopies != null ? totalCopies : 1);
        book.setAvailableCopies(book.getTotalCopies());

        return bookRepository.save(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE);
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.searchBooks(query);
    }

    public List<Book> searchBooksByStatus(String query, BookStatus status) {
        List<Book> books = bookRepository.searchBooks(query);
        return books.stream()
                .filter(book -> book.getStatus() == status)
                .toList();
    }

    public Book updateBook(Long id, String title, String author, String isbn,
                           String publisher, Integer publicationYear, String genre,
                           Integer pages, String description, Integer totalCopies) {

        Book book = getBookById(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setPages(pages);
        book.setDescription(description);

        if (totalCopies != null) {
            int currentBorrowed = book.getTotalCopies() - book.getAvailableCopies();
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(Math.max(0, totalCopies - currentBorrowed));
        }

        return bookRepository.save(book);
    }

    public Book updateBookStatus(Long id, BookStatus status) {
        Book book = getBookById(id);
        book.setStatus(status);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);

        // Проверка, есть ли активные выдачи этой книги
        Long activeLoans = loanRepository.countActiveLoansByReader(id);
        if (activeLoans > 0) {
            throw new IllegalArgumentException("Cannot delete book with active loans");
        }

        bookRepository.delete(book);
    }

    // ================= ЧИТАТЕЛИ =================

    public Reader registerReader(String firstName, String lastName, String email,
                                 String libraryCardNumber, String phoneNumber) {

        Reader reader = new Reader(firstName, lastName, email, libraryCardNumber);
        reader.setPhoneNumber(phoneNumber);

        return readerRepository.save(reader);
    }

    public Reader getReaderById(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new ReaderNotFoundException(id));
    }

    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    public List<Reader> searchReaders(String query) {
        return readerRepository.searchReaders(query);
    }

    public Reader updateReader(Long id, String firstName, String lastName,
                               String email, String phoneNumber, Integer maxBooksAllowed) {

        Reader reader = getReaderById(id);
        reader.setFirstName(firstName);
        reader.setLastName(lastName);
        reader.setEmail(email);
        reader.setPhoneNumber(phoneNumber);

        if (maxBooksAllowed != null) {
            reader.setMaxBooksAllowed(maxBooksAllowed);
        }

        return readerRepository.save(reader);
    }

    public void deleteReader(Long id) {
        Reader reader = getReaderById(id);

        // Проверка, есть ли активные выдачи у читателя
        List<Loan> activeLoans = loanRepository.findByReaderId(id).stream()
                .filter(loan -> loan.getReturnDate() == null)
                .toList();

        if (!activeLoans.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete reader with active loans");
        }

        readerRepository.delete(reader);
    }

    public int updateReaderMaxBooks(Long readerId, Integer maxBooks) {
        return readerRepository.updateReaderMaxBooks(readerId, maxBooks);
    }

    // ================= ВЫДАЧА КНИГ =================

    @Transactional
    public Loan borrowBook(Long bookId, Long readerId, Integer loanDays, String notes) {
        Book book = getBookById(bookId);
        Reader reader = getReaderById(readerId);

        // Проверка доступности книги
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("No copies of this book are available");
        }

        // Проверка статуса книги
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalArgumentException("Book is not available for borrowing");
        }

        // Проверка максимального количества книг у читателя
        Long activeLoansCount = loanRepository.countActiveLoansByReader(readerId);
        if (activeLoansCount >= reader.getMaxBooksAllowed()) {
            throw new IllegalArgumentException(
                    String.format("Reader has reached the maximum limit of %d books",
                            reader.getMaxBooksAllowed()));
        }

        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(loanDays);

        Loan loan = new Loan(book, reader, loanDate, dueDate);
        loan.setNotes(notes);

        // Обновление статуса книги
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() == 0) {
            book.setStatus(BookStatus.BORROWED);
        }

        bookRepository.save(book);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId, String conditionNotes) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getReturnDate() != null) {
            throw new IllegalArgumentException("Book has already been returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setNotes(loan.getNotes() + "\n" + conditionNotes);

        // Проверка просрочки и расчет штрафа
        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            long daysOverdue = loan.getDaysOverdue();
            double fine = daysOverdue * 10.0; // 10 рублей за каждый день просрочки
            loan.setFineAmount(fine);
        }

        // Обновление статуса книги
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        if (book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        }

        bookRepository.save(book);
        return loanRepository.save(loan);
    }

    public Loan extendLoan(Long loanId, Integer additionalDays) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getReturnDate() != null) {
            throw new IllegalArgumentException("Cannot extend a returned loan");
        }

        // Проверка, можно ли продлевать (не более 2 раз)
        if (loan.getDueDate().isAfter(LocalDate.now().plusDays(additionalDays))) {
            throw new IllegalArgumentException("Loan has already been extended");
        }

        loan.setDueDate(loan.getDueDate().plusDays(additionalDays));
        return loanRepository.save(loan);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getReaderLoans(Long readerId) {
        return loanRepository.findByReaderId(readerId);
    }

    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }
}