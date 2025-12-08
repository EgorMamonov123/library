package com.library;

import com.library.core.domain.book.Author;
import com.library.core.domain.book.Book;
import com.library.core.domain.book.Genre;
import com.library.core.domain.user.Reader;
import com.library.core.repository.book.AuthorRepository;
import com.library.core.repository.book.BookRepository;
import com.library.core.repository.book.GenreRepository;
import com.library.core.repository.book.impl.InMemoryAuthorRepository;
import com.library.core.repository.book.impl.InMemoryBookRepository;
import com.library.core.repository.book.impl.InMemoryGenreRepository;
import com.library.core.repository.user.ReaderRepository;
import com.library.core.repository.user.impl.InMemoryReaderRepository;
import com.library.core.service.BookCatalogService;
import com.library.core.service.BookService;
import com.library.core.service.ReaderService;

import java.time.Year;
import java.util.Scanner;

public class LibraryApplication {  // Измените имя класса

    public static void main(String[] args) {
        System.out.println("=== Библиотечная система ===");

        // Инициализация репозиториев
        BookRepository bookRepository = new InMemoryBookRepository();
        AuthorRepository authorRepository = new InMemoryAuthorRepository();
        GenreRepository genreRepository = new InMemoryGenreRepository();
        ReaderRepository readerRepository = new InMemoryReaderRepository();

        // Инициализация сервисов
        BookService bookService = new BookService(bookRepository);
        ReaderService readerService = new ReaderService(readerRepository);
        BookCatalogService catalogService = new BookCatalogService(
                bookRepository, authorRepository, genreRepository
        );

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Инициализация тестовых данных
        initializeSampleData(bookRepository, authorRepository, genreRepository, readerRepository);

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    manageBooks(scanner, bookService, catalogService);
                    break;
                case "2":
                    manageReaders(scanner, readerService);
                    break;
                case "3":
                    showStatistics(catalogService);
                    break;
                case "4":
                    searchBooks(scanner, catalogService);
                    break;
                case "0":
                    running = false;
                    System.out.println("Выход из системы...");
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }

        scanner.close();
        bookService.shutdown();
        readerService.shutdown();
    }

    private static void printMainMenu() {
        System.out.println("\n=== Главное меню ===");
        System.out.println("1. Управление книгами");
        System.out.println("2. Управление читателями");
        System.out.println("3. Статистика библиотеки");
        System.out.println("4. Поиск книг");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private static void manageBooks(Scanner scanner, BookService bookService, BookCatalogService catalogService) {
        boolean inBookMenu = true;

        while (inBookMenu) {
            System.out.println("\n=== Управление книгами ===");
            System.out.println("1. Добавить книгу");
            System.out.println("2. Показать все книги");
            System.out.println("3. Найти книгу");
            System.out.println("4. Удалить книгу");
            System.out.println("5. Показать доступные книги");
            System.out.println("6. Вернуться в главное меню");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addBook(scanner, bookService);
                    break;
                case "2":
                    listAllBooks(bookService);
                    break;
                case "3":
                    searchBook(scanner, bookService);
                    break;
                case "4":
                    deleteBook(scanner, bookService);
                    break;
                case "5":
                    listAvailableBooks(bookService);
                    break;
                case "6":
                    inBookMenu = false;
                    break;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private static void manageReaders(Scanner scanner, ReaderService readerService) {
        boolean inReaderMenu = true;

        while (inReaderMenu) {
            System.out.println("\n=== Управление читателями ===");
            System.out.println("1. Зарегистрировать читателя");
            System.out.println("2. Показать всех читателей");
            System.out.println("3. Найти читателя");
            System.out.println("4. Обновить информацию о читателе");
            System.out.println("5. Деактивировать читателя");
            System.out.println("6. Активировать читателя");
            System.out.println("7. Вернуться в главное меню");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addReader(scanner, readerService);
                    break;
                case "2":
                    listAllReaders(readerService);
                    break;
                case "3":
                    findReader(scanner, readerService);
                    break;
                case "4":
                    updateReader(scanner, readerService);
                    break;
                case "5":
                    deactivateReader(scanner, readerService);
                    break;
                case "6":
                    activateReader(scanner, readerService);
                    break;
                case "7":
                    inReaderMenu = false;
                    break;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private static void showStatistics(BookCatalogService catalogService) {
        System.out.println("\n=== Статистика библиотеки ===");

        var stats = catalogService.getLibraryStatistics();

        System.out.println("Общее количество книг: " + stats.get("totalBooks"));
        System.out.println("Доступно книг: " + stats.get("availableBooks"));
        System.out.println("Количество авторов: " + stats.get("totalAuthors"));
        System.out.println("Количество жанров: " + stats.get("totalGenres"));

        if (stats.containsKey("mostPopularGenre")) {
            System.out.println("Самый популярный жанр: " + stats.get("mostPopularGenre"));
        }

        if (stats.containsKey("mostProductiveAuthor")) {
            System.out.println("Самый продуктивный автор: " + stats.get("mostProductiveAuthor") +
                    " (книг: " + stats.get("authorBookCount") + ")");
        }
    }

    private static void searchBooks(Scanner scanner, BookCatalogService catalogService) {
        System.out.println("\n=== Расширенный поиск книг ===");
        System.out.print("Введите поисковый запрос: ");
        String query = scanner.nextLine();

        var results = catalogService.searchBooks(query);

        if (results.isEmpty()) {
            System.out.println("Книги не найдены.");
        } else {
            System.out.println("Найдено книг: " + results.size());
            results.forEach(book -> {
                System.out.printf("- %s", book.getTitle());
                if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    System.out.printf(" (ISBN: %s)", book.getIsbn());
                }
                if (book.getPublicationYear() != null) {
                    System.out.printf(" - %d", book.getPublicationYear().getValue());
                }
                System.out.println();
            });
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private static void addBook(Scanner scanner, BookService bookService) {
        System.out.println("\n=== Добавление книги ===");

        System.out.print("Введите название книги: ");
        String title = scanner.nextLine();

        System.out.print("Введите ISBN (опционально): ");
        String isbn = scanner.nextLine();
        if (isbn.isEmpty()) isbn = null;

        System.out.print("Введите год издания (опционально): ");
        String yearStr = scanner.nextLine();
        Year year = yearStr.isEmpty() ? null : Year.of(Integer.parseInt(yearStr));

        System.out.print("Введите количество копий: ");
        int copies = Integer.parseInt(scanner.nextLine());

        Book book = new Book(title, isbn);
        book.setPublicationYear(year);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);

        try {
            Book savedBook = bookService.addBook(book);
            System.out.println("Книга успешно добавлена с ID: " + savedBook.getId());
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении книги: " + e.getMessage());
        }
    }

    private static void listAllBooks(BookService bookService) {
        System.out.println("\n=== Список всех книг ===");
        var books = bookService.getAvailableBooks();

        if (books.isEmpty()) {
            System.out.println("Книг пока нет в библиотеке.");
        } else {
            System.out.println("Всего книг: " + books.size());
            books.forEach(book -> {
                System.out.printf("ID: %d | %s", book.getId(), book.getTitle());
                if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    System.out.printf(" (ISBN: %s)", book.getIsbn());
                }
                System.out.printf(" | Доступно: %d/%d",
                        book.getAvailableCopies(), book.getTotalCopies());
                System.out.println();
            });
        }
    }

    private static void searchBook(Scanner scanner, BookService bookService) {
        System.out.println("\n=== Поиск книги ===");
        System.out.print("Введите поисковый запрос (название или автора): ");
        String query = scanner.nextLine();

        var resultsByTitle = bookService.searchBooksByTitle(query);
        var resultsByAuthor = bookService.searchBooksByAuthor(query);

        if (resultsByTitle.isEmpty() && resultsByAuthor.isEmpty()) {
            System.out.println("Книги не найдены.");
        } else {
            if (!resultsByTitle.isEmpty()) {
                System.out.println("Найдено по названию:");
                resultsByTitle.forEach(b -> System.out.println("  - " + b.getTitle()));
            }
            if (!resultsByAuthor.isEmpty()) {
                System.out.println("Найдено по автору:");
                resultsByAuthor.forEach(b -> System.out.println("  - " + b.getTitle()));
            }
        }
    }

    private static void deleteBook(Scanner scanner, BookService bookService) {
        System.out.println("\n=== Удаление книги ===");
        System.out.print("Введите ID книги для удаления: ");
        Long bookId = Long.parseLong(scanner.nextLine());

        boolean deleted = bookService.deleteBook(bookId);
        if (deleted) {
            System.out.println("Книга успешно удалена.");
        } else {
            System.out.println("Книга с указанным ID не найдена.");
        }
    }

    private static void listAvailableBooks(BookService bookService) {
        System.out.println("\n=== Доступные книги ===");
        var availableBooks = bookService.getAvailableBooks();

        if (availableBooks.isEmpty()) {
            System.out.println("Нет доступных книг.");
        } else {
            System.out.println("Доступно книг: " + availableBooks.size());
            availableBooks.forEach(book -> {
                System.out.printf("- %s", book.getTitle());
                if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    System.out.printf(" (ISBN: %s)", book.getIsbn());
                }
                System.out.printf(" | Копий: %d", book.getAvailableCopies());
                System.out.println();
            });
        }
    }

    private static void addReader(Scanner scanner, ReaderService readerService) {
        System.out.println("\n=== Регистрация читателя ===");

        System.out.print("Введите номер читательского билета: ");
        String cardNumber = scanner.nextLine();

        System.out.print("Введите имя: ");
        String firstName = scanner.nextLine();

        System.out.print("Введите фамилию: ");
        String lastName = scanner.nextLine();

        System.out.print("Введите email (опционально): ");
        String email = scanner.nextLine();
        if (email.isEmpty()) email = null;

        System.out.print("Введите телефон (опционально): ");
        String phone = scanner.nextLine();
        if (phone.isEmpty()) phone = null;

        Reader reader = new Reader(cardNumber, firstName, lastName);
        reader.setEmail(email);
        reader.setPhone(phone);

        try {
            Reader saved = readerService.registerReader(reader);
            System.out.println("Читатель успешно зарегистрирован с ID: " + saved.getId());
        } catch (Exception e) {
            System.out.println("Ошибка при регистрации читателя: " + e.getMessage());
        }
    }

    private static void listAllReaders(ReaderService readerService) {
        System.out.println("\n=== Список всех читателей ===");
        var readers = readerService.getActiveReaders();

        if (readers.isEmpty()) {
            System.out.println("Читателей пока нет.");
        } else {
            System.out.println("Всего читателей: " + readers.size());
            readers.forEach(reader -> {
                System.out.printf("ID: %d | %s %s (Билет: %s)",
                        reader.getId(),
                        reader.getFirstName(),
                        reader.getLastName(),
                        reader.getLibraryCardNumber());

                if (reader.getEmail() != null) {
                    System.out.printf(" | Email: %s", reader.getEmail());
                }
                if (reader.getPhone() != null) {
                    System.out.printf(" | Телефон: %s", reader.getPhone());
                }

                System.out.printf(" | Статус: %s",
                        Boolean.TRUE.equals(reader.getActive()) ? "активен" : "неактивен");
                System.out.println();
            });
        }
    }

    private static void findReader(Scanner scanner, ReaderService readerService) {
        System.out.println("\n=== Поиск читателя ===");
        System.out.println("1. Поиск по фамилии");
        System.out.println("2. Поиск по номеру билета");
        System.out.print("Выберите тип поиска: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Введите фамилию: ");
                String lastName = scanner.nextLine();
                var readersByLastName = readerService.findReadersByLastName(lastName);

                if (readersByLastName.isEmpty()) {
                    System.out.println("Читатели не найдены.");
                } else {
                    System.out.println("Найдено читателей: " + readersByLastName.size());
                    readersByLastName.forEach(r ->
                            System.out.printf("- %s %s (Билет: %s)%n",
                                    r.getFirstName(), r.getLastName(), r.getLibraryCardNumber())
                    );
                }
                break;

            case "2":
                System.out.print("Введите номер читательского билета: ");
                String cardNumber = scanner.nextLine();
                var readerOpt = readerService.getReaderByLibraryCard(cardNumber);

                if (readerOpt.isPresent()) {
                    Reader r = readerOpt.get();
                    System.out.printf("Найден: %s %s (ID: %d, Email: %s, Телефон: %s)%n",
                            r.getFirstName(), r.getLastName(), r.getId(),
                            r.getEmail() != null ? r.getEmail() : "нет",
                            r.getPhone() != null ? r.getPhone() : "нет");
                } else {
                    System.out.println("Читатель не найден.");
                }
                break;

            default:
                System.out.println("Неверный выбор.");
        }
    }

    private static void updateReader(Scanner scanner, ReaderService readerService) {
        System.out.println("\n=== Обновление информации о читателе ===");
        System.out.print("Введите ID читателя: ");
        Long readerId = Long.parseLong(scanner.nextLine());

        System.out.print("Введите новый email (оставьте пустым чтобы не менять): ");
        String email = scanner.nextLine();
        if (email.isEmpty()) email = null;

        System.out.print("Введите новый телефон (оставьте пустым чтобы не менять): ");
        String phone = scanner.nextLine();
        if (phone.isEmpty()) phone = null;

        try {
            Reader updated = readerService.updateReaderInfo(readerId, email, phone);
            System.out.println("Информация успешно обновлена.");
            System.out.printf("Читатель: %s %s%n", updated.getFirstName(), updated.getLastName());
            if (updated.getEmail() != null) {
                System.out.println("Email: " + updated.getEmail());
            }
            if (updated.getPhone() != null) {
                System.out.println("Телефон: " + updated.getPhone());
            }
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении информации: " + e.getMessage());
        }
    }

    private static void deactivateReader(Scanner scanner, ReaderService readerService) {
        System.out.println("\n=== Деактивация читателя ===");
        System.out.print("Введите ID читателя: ");
        Long readerId = Long.parseLong(scanner.nextLine());

        boolean result = readerService.deactivateReader(readerId);
        if (result) {
            System.out.println("Читатель успешно деактивирован.");
        } else {
            System.out.println("Читатель не найден.");
        }
    }

    private static void activateReader(Scanner scanner, ReaderService readerService) {
        System.out.println("\n=== Активация читателя ===");
        System.out.print("Введите ID читателя: ");
        Long readerId = Long.parseLong(scanner.nextLine());

        boolean result = readerService.activateReader(readerId);
        if (result) {
            System.out.println("Читатель успешно активирован.");
        } else {
            System.out.println("Читатель не найден.");
        }
    }

    private static void initializeSampleData(BookRepository bookRepo,
                                             AuthorRepository authorRepo,
                                             GenreRepository genreRepo,
                                             ReaderRepository readerRepo) {
        System.out.println("Загрузка тестовых данных...");

        try {
            // Создаем жанры
            Genre fiction = new Genre("Художественная литература");
            fiction.setDescription("Романы, повести, рассказы");

            Genre programming = new Genre("Программирование");
            programming.setDescription("Книги по разработке программного обеспечения");

            Genre science = new Genre("Научная литература");
            science.setDescription("Научно-популярные и академические издания");

            Genre classic = new Genre("Классика");
            classic.setDescription("Классические произведения мировой литературы");

            genreRepo.save(fiction);
            genreRepo.save(programming);
            genreRepo.save(science);
            genreRepo.save(classic);

            // Создаем авторов
            Author author1 = new Author("Роберт", "Мартин");
            author1.setCountry("США");

            Author author2 = new Author("Джошуа", "Блох");
            author2.setCountry("США");

            Author author3 = new Author("Лев", "Толстой");
            author3.setCountry("Россия");
            author3.setBirthDate(java.time.LocalDate.of(1828, 9, 9));

            Author author4 = new Author("Федор", "Достоевский");
            author4.setCountry("Россия");
            author4.setBirthDate(java.time.LocalDate.of(1821, 11, 11));

            authorRepo.save(author1);
            authorRepo.save(author2);
            authorRepo.save(author3);
            authorRepo.save(author4);

            // Создаем книги
            Book book1 = new Book("Чистый код", "9780132350884");
            book1.setPublicationYear(Year.of(2008));
            book1.setGenre(programming);
            book1.addAuthor(author1);
            book1.setTotalCopies(3);
            book1.setAvailableCopies(3);
            book1.setCreatedAt(java.time.LocalDateTime.now().minusDays(30));

            Book book2 = new Book("Effective Java", "9780134685991");
            book2.setPublicationYear(Year.of(2018));
            book2.setGenre(programming);
            book2.addAuthor(author2);
            book2.setTotalCopies(2);
            book2.setAvailableCopies(2);
            book2.setCreatedAt(java.time.LocalDateTime.now().minusDays(20));

            Book book3 = new Book("Война и мир", "9785170901546");
            book3.setPublicationYear(Year.of(1869));
            book3.setGenre(classic);
            book3.addAuthor(author3);
            book3.setTotalCopies(5);
            book3.setAvailableCopies(5);
            book3.setCreatedAt(java.time.LocalDateTime.now().minusDays(10));

            Book book4 = new Book("Преступление и наказание", "9785171143921");
            book4.setPublicationYear(Year.of(1866));
            book4.setGenre(classic);
            book4.addAuthor(author4);
            book4.setTotalCopies(4);
            book4.setAvailableCopies(4);
            book4.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));

            bookRepo.save(book1);
            bookRepo.save(book2);
            bookRepo.save(book3);
            bookRepo.save(book4);

            // Создаем читателей
            Reader reader1 = new Reader("001", "Иван", "Иванов");
            reader1.setEmail("ivan@example.com");
            reader1.setPhone("+79991112233");
            reader1.setRegistrationDate(java.time.LocalDate.now().minusMonths(6));

            Reader reader2 = new Reader("002", "Мария", "Петрова");
            reader2.setEmail("maria@example.com");
            reader2.setPhone("+79994445566");
            reader2.setRegistrationDate(java.time.LocalDate.now().minusMonths(3));

            Reader reader3 = new Reader("003", "Алексей", "Сидоров");
            reader3.setEmail("alex@example.com");
            reader3.setPhone("+79997778899");
            reader3.setRegistrationDate(java.time.LocalDate.now().minusMonths(1));
            reader3.setActive(false); // Неактивный читатель

            readerRepo.save(reader1);
            readerRepo.save(reader2);
            readerRepo.save(reader3);

            System.out.println("Тестовые данные успешно загружены.");
            System.out.println("Загружено: 4 книги, 4 автора, 4 жанра, 3 читателя");

        } catch (Exception e) {
            System.out.println("Ошибка при загрузке тестовых данных: " + e.getMessage());
        }
    }
}