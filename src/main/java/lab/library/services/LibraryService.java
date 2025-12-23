package lab.library.services;

import lab.library.enums.*;
import lab.library.models.*;
import lab.library.repositories.*;
import lab.library.exceptions.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
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

        log.info("Добавление новой книги: '{}' автора {}", title, author);
        log.debug("Данные книги: ISBN={}, издательство={}, год={}, жанр={}",
                isbn, publisher, publicationYear, genre);

        Book book = new Book(title, author, isbn);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setPages(pages);
        book.setDescription(description);
        book.setTotalCopies(totalCopies != null ? totalCopies : 1);
        book.setAvailableCopies(book.getTotalCopies());

        Book savedBook = bookRepository.save(book);
        log.info("Книга успешно добавлена. ID: {}, доступно экземпляров: {}",
                savedBook.getId(), savedBook.getAvailableCopies());

        return savedBook;
    }

    public Book getBookById(Long id) {
        log.debug("Поиск книги по ID: {}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Книга с ID {} не найдена", id);
                    return new BookNotFoundException(id);
                });
    }

    public Book getBookByIsbn(String isbn) {
        log.debug("Поиск книги по ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> {
                    log.warn("Книга с ISBN {} не найдена", isbn);
                    return new BookNotFoundException(isbn);
                });
    }

    public List<Book> getAllBooks() {
        log.debug("Получение списка всех книг");
        List<Book> books = bookRepository.findAll();
        log.info("Получено {} книг из базы данных", books.size());
        return books;
    }

    public List<Book> getAvailableBooks() {
        log.debug("Получение списка доступных книг");
        List<Book> availableBooks = bookRepository.findByStatus(BookStatus.AVAILABLE);
        log.info("Найдено {} доступных книг", availableBooks.size());
        return availableBooks;
    }

    public List<Book> searchBooks(String query) {
        log.info("Поиск книг по запросу: '{}'", query);
        List<Book> foundBooks = bookRepository.searchBooks(query);
        log.info("По запросу '{}' найдено {} книг", query, foundBooks.size());
        return foundBooks;
    }

    public List<Book> searchBooksByStatus(String query, BookStatus status) {
        log.info("Поиск книг по запросу '{}' со статусом {}", query, status);
        List<Book> books = bookRepository.searchBooks(query);
        List<Book> filteredBooks = books.stream()
                .filter(book -> book.getStatus() == status)
                .toList();
        log.info("Найдено {} книг с запросом '{}' и статусом {}",
                filteredBooks.size(), query, status);
        return filteredBooks;
    }

    public Book updateBook(Long id, String title, String author, String isbn,
                           String publisher, Integer publicationYear, String genre,
                           Integer pages, String description, Integer totalCopies) {

        log.info("Обновление книги ID: {}", id);
        log.debug("Новые данные: название='{}', автор='{}', ISBN={}", title, author, isbn);

        Book book = getBookById(id);

        // Логирование изменений
        if (!book.getTitle().equals(title)) {
            log.debug("Изменение названия книги: '{}' -> '{}'", book.getTitle(), title);
        }
        if (!book.getAuthor().equals(author)) {
            log.debug("Изменение автора книги: '{}' -> '{}'", book.getAuthor(), author);
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        book.setGenre(genre);
        book.setPages(pages);
        book.setDescription(description);

        if (totalCopies != null && !totalCopies.equals(book.getTotalCopies())) {
            log.info("Изменение количества экземпляров: {} -> {}",
                    book.getTotalCopies(), totalCopies);
            int currentBorrowed = book.getTotalCopies() - book.getAvailableCopies();
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(Math.max(0, totalCopies - currentBorrowed));
            log.debug("Доступно экземпляров после изменения: {}", book.getAvailableCopies());
        }

        Book updatedBook = bookRepository.save(book);
        log.info("Книга ID: {} успешно обновлена", id);

        return updatedBook;
    }

    public Book updateBookStatus(Long id, BookStatus status) {
        log.info("Обновление статуса книги ID: {} -> {}", id, status);
        Book book = getBookById(id);

        BookStatus oldStatus = book.getStatus();
        if (oldStatus != status) {
            log.debug("Статус книги изменен: {} -> {}", oldStatus, status);
        }

        book.setStatus(status);
        Book updatedBook = bookRepository.save(book);
        log.info("Статус книги ID: {} успешно изменен на {}", id, status);

        return updatedBook;
    }

    public void deleteBook(Long id) {
        log.info("Удаление книги ID: {}", id);
        Book book = getBookById(id);

        // Проверка, есть ли активные выдачи этой книги
        Long activeLoans = loanRepository.countActiveLoansByReader(id);
        if (activeLoans > 0) {
            log.warn("Не удалось удалить книгу ID: {} - есть активные выдачи", id);
            throw new IllegalArgumentException("Cannot delete book with active loans");
        }

        bookRepository.delete(book);
        log.info("Книга ID: {} успешно удалена. Название: '{}'", id, book.getTitle());
    }

    // ================= ЧИТАТЕЛИ =================

    public Reader registerReader(String firstName, String lastName, String email,
                                 String libraryCardNumber, String phoneNumber) {

        log.info("Регистрация нового читателя: {} {}", firstName, lastName);
        log.debug("Данные читателя: email={}, номер билета={}, телефон={}",
                email, libraryCardNumber, phoneNumber);

        Reader reader = new Reader(firstName, lastName, email, libraryCardNumber);
        reader.setPhoneNumber(phoneNumber);

        Reader savedReader = readerRepository.save(reader);
        log.info("Читатель успешно зарегистрирован. ID: {}, номер билета: {}",
                savedReader.getId(), savedReader.getLibraryCardNumber());

        return savedReader;
    }

    public Reader getReaderById(Long id) {
        log.debug("Поиск читателя по ID: {}", id);
        return readerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Читатель с ID {} не найден", id);
                    return new ReaderNotFoundException(id);
                });
    }

    public List<Reader> getAllReaders() {
        log.debug("Получение списка всех читателей");
        List<Reader> readers = readerRepository.findAll();
        log.info("Получено {} читателей из базы данных", readers.size());
        return readers;
    }

    public List<Reader> searchReaders(String query) {
        log.info("Поиск читателей по запросу: '{}'", query);
        List<Reader> foundReaders = readerRepository.searchReaders(query);
        log.info("По запросу '{}' найдено {} читателей", query, foundReaders.size());
        return foundReaders;
    }

    public Reader updateReader(Long id, String firstName, String lastName,
                               String email, String phoneNumber, Integer maxBooksAllowed) {

        log.info("Обновление информации о читателе ID: {}", id);

        Reader reader = getReaderById(id);

        // Логирование изменений
        if (!reader.getFirstName().equals(firstName)) {
            log.debug("Изменение имени читателя: '{}' -> '{}'", reader.getFirstName(), firstName);
        }
        if (!reader.getLastName().equals(lastName)) {
            log.debug("Изменение фамилии читателя: '{}' -> '{}'", reader.getLastName(), lastName);
        }

        reader.setFirstName(firstName);
        reader.setLastName(lastName);
        reader.setEmail(email);
        reader.setPhoneNumber(phoneNumber);

        if (maxBooksAllowed != null && !maxBooksAllowed.equals(reader.getMaxBooksAllowed())) {
            log.info("Изменение максимального количества книг для читателя ID: {}: {} -> {}",
                    id, reader.getMaxBooksAllowed(), maxBooksAllowed);
            reader.setMaxBooksAllowed(maxBooksAllowed);
        }

        Reader updatedReader = readerRepository.save(reader);
        log.info("Информация о читателе ID: {} успешно обновлена", id);

        return updatedReader;
    }

    public void deleteReader(Long id) {
        log.info("Удаление читателя ID: {}", id);
        Reader reader = getReaderById(id);

        // Проверка, есть ли активные выдачи у читателя
        List<Loan> activeLoans = loanRepository.findByReaderId(id).stream()
                .filter(loan -> loan.getReturnDate() == null)
                .toList();

        if (!activeLoans.isEmpty()) {
            log.warn("Не удалось удалить читателя ID: {} - есть {} активных выдач",
                    id, activeLoans.size());
            throw new IllegalArgumentException("Cannot delete reader with active loans");
        }

        readerRepository.delete(reader);
        log.info("Читатель ID: {} успешно удален. Имя: {} {}",
                id, reader.getFirstName(), reader.getLastName());
    }

    public int updateReaderMaxBooks(Long readerId, Integer maxBooks) {
        log.info("Обновление максимального количества книг для читателя ID: {} -> {}",
                readerId, maxBooks);
        int result = readerRepository.updateReaderMaxBooks(readerId, maxBooks);
        log.debug("Обновлено записей: {}", result);
        return result;
    }

    // ================= ВЫДАЧА КНИГ =================

    @Transactional
    public Loan borrowBook(Long bookId, Long readerId, Integer loanDays, String notes) {
        log.info("Выдача книги. BookID: {}, ReaderID: {}, на {} дней",
                bookId, readerId, loanDays);

        try {
            Book book = getBookById(bookId);
            Reader reader = getReaderById(readerId);

            // Проверка доступности книги
            if (book.getAvailableCopies() <= 0) {
                log.warn("Нет доступных экземпляров книги ID: {}. Название: '{}'",
                        bookId, book.getTitle());
                throw new IllegalArgumentException("No copies of this book are available");
            }

            // Проверка статуса книги
            if (book.getStatus() != BookStatus.AVAILABLE) {
                log.warn("Книга ID: {} недоступна для выдачи. Текущий статус: {}",
                        bookId, book.getStatus());
                throw new IllegalArgumentException("Book is not available for borrowing");
            }

            // Проверка максимального количества книг у читателя
            Long activeLoansCount = loanRepository.countActiveLoansByReader(readerId);
            if (activeLoansCount >= reader.getMaxBooksAllowed()) {
                log.warn("Читатель ID: {} достиг лимита книг. Лимит: {}, текущие: {}",
                        readerId, reader.getMaxBooksAllowed(), activeLoansCount);
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
                log.debug("Все экземпляры книги ID: {} выданы. Установка статуса BORROWED", bookId);
                book.setStatus(BookStatus.BORROWED);
            }

            bookRepository.save(book);
            Loan savedLoan = loanRepository.save(loan);

            log.info("Книга успешно выдана. LoanID: {}, книга: '{}', читатель: {} {}, срок: до {}",
                    savedLoan.getId(), book.getTitle(),
                    reader.getFirstName(), reader.getLastName(),
                    dueDate);

            return savedLoan;

        } catch (Exception e) {
            log.error("Ошибка при выдаче книги. BookID: {}, ReaderID: {}", bookId, readerId, e);
            throw e;
        }
    }

    @Transactional
    public Loan returnBook(Long loanId, String conditionNotes) {
        log.info("Возврат книги. LoanID: {}", loanId);

        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> {
                        log.warn("Выдача с ID {} не найдена", loanId);
                        return new RuntimeException("Loan not found");
                    });

            if (loan.getReturnDate() != null) {
                log.warn("Книга по выдаче ID: {} уже возвращена {}",
                        loanId, loan.getReturnDate());
                throw new IllegalArgumentException("Book has already been returned");
            }

            loan.setReturnDate(LocalDate.now());
            if (conditionNotes != null && !conditionNotes.isEmpty()) {
                String currentNotes = loan.getNotes() != null ? loan.getNotes() : "";
                loan.setNotes(currentNotes + "\nПри возврате: " + conditionNotes);
                log.debug("Добавлены примечания к возврату: {}", conditionNotes);
            }

            // Проверка просрочки и расчет штрафа
            if (loan.getReturnDate().isAfter(loan.getDueDate())) {
                long daysOverdue = loan.getDaysOverdue();
                double fine = daysOverdue * 10.0; // 10 рублей за каждый день просрочки
                loan.setFineAmount(fine);
                log.warn("Просрочка возврата! LoanID: {}, просрочено дней: {}, штраф: {} руб.",
                        loanId, daysOverdue, fine);
            }

            // Обновление статуса книги
            Book book = loan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);

            if (book.getAvailableCopies() > 0) {
                book.setStatus(BookStatus.AVAILABLE);
                log.debug("Книга ID: {} снова доступна. Доступно экземпляров: {}",
                        book.getId(), book.getAvailableCopies());
            }

            bookRepository.save(book);
            Loan returnedLoan = loanRepository.save(loan);

            log.info("Книга успешно возвращена. LoanID: {}, книга: '{}', читатель: {} {}, дата возврата: {}",
                    loanId, book.getTitle(),
                    loan.getReader().getFirstName(), loan.getReader().getLastName(),
                    loan.getReturnDate());

            if (loan.getFineAmount() > 0) {
                log.info("Начислен штраф: {} руб. за LoanID: {}", loan.getFineAmount(), loanId);
            }

            return returnedLoan;

        } catch (Exception e) {
            log.error("Ошибка при возврате книги. LoanID: {}", loanId, e);
            throw e;
        }
    }

    public Loan extendLoan(Long loanId, Integer additionalDays) {
        log.info("Продление выдачи. LoanID: {}, дополнительные дни: {}",
                loanId, additionalDays);

        try {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> {
                        log.warn("Выдача с ID {} не найдена", loanId);
                        return new RuntimeException("Loan not found");
                    });

            if (loan.getReturnDate() != null) {
                log.warn("Не удалось продлить выдачу ID: {} - книга уже возвращена", loanId);
                throw new IllegalArgumentException("Cannot extend a returned loan");
            }

            // Проверка, можно ли продлевать
            if (loan.getDueDate().isAfter(LocalDate.now().plusDays(additionalDays))) {
                log.warn("Выдача ID: {} уже продлена до {}", loanId, loan.getDueDate());
                throw new IllegalArgumentException("Loan has already been extended");
            }

            LocalDate oldDueDate = loan.getDueDate();
            loan.setDueDate(loan.getDueDate().plusDays(additionalDays));
            Loan extendedLoan = loanRepository.save(loan);

            log.info("Выдача успешно продлена. LoanID: {}, старый срок: {}, новый срок: {}",
                    loanId, oldDueDate, extendedLoan.getDueDate());

            return extendedLoan;

        } catch (Exception e) {
            log.error("Ошибка при продлении выдачи. LoanID: {}", loanId, e);
            throw e;
        }
    }

    public List<Loan> getAllLoans() {
        log.debug("Получение списка всех выдач");
        List<Loan> loans = loanRepository.findAll();
        log.info("Получено {} выдач из базы данных", loans.size());
        return loans;
    }

    public List<Loan> getReaderLoans(Long readerId) {
        log.debug("Получение выдач для читателя ID: {}", readerId);
        List<Loan> loans = loanRepository.findByReaderId(readerId);
        log.info("Для читателя ID: {} найдено {} выдач", readerId, loans.size());

        // Подсчет активных выдач
        long activeLoans = loans.stream()
                .filter(loan -> loan.getReturnDate() == null)
                .count();
        if (activeLoans > 0) {
            log.debug("Активных выдач у читателя ID: {}: {}", readerId, activeLoans);
        }

        return loans;
    }

    public List<Loan> getOverdueLoans() {
        log.debug("Поиск просроченных выдач");
        List<Loan> overdueLoans = loanRepository.findOverdueLoans();

        if (!overdueLoans.isEmpty()) {
            log.warn("Найдено {} просроченных выдач", overdueLoans.size());
            // Логируем детали первых 5 просрочек для отладки
            overdueLoans.stream().limit(5).forEach(loan -> {
                log.debug("Просроченная выдача ID: {}, книга: '{}', читатель: {} {}, просрочено дней: {}",
                        loan.getId(), loan.getBook().getTitle(),
                        loan.getReader().getFirstName(), loan.getReader().getLastName(),
                        loan.getDaysOverdue());
            });
        } else {
            log.info("Просроченных выдач не найдено");
        }

        return overdueLoans;
    }
}