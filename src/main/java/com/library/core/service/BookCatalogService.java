package com.library.core.service;

import com.library.core.domain.book.Author;
import com.library.core.domain.book.Book;
import com.library.core.domain.book.Genre;
import com.library.core.repository.book.AuthorRepository;
import com.library.core.repository.book.BookRepository;
import com.library.core.repository.book.GenreRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для каталогизации книг
 */
public class BookCatalogService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    public BookCatalogService(BookRepository bookRepository,
                              AuthorRepository authorRepository,
                              GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    /**
     * Получает статистику по библиотеке
     */
    public Map<String, Object> getLibraryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBooks", bookRepository.count());
        stats.put("totalAuthors", authorRepository.count());
        stats.put("totalGenres", genreRepository.count());

        // Количество доступных книг
        long availableBooks = bookRepository.findAvailableBooks().size();
        stats.put("availableBooks", availableBooks);

        // Самый популярный жанр
        Optional<Genre> popularGenre = findMostPopularGenre();
        popularGenre.ifPresent(genre -> stats.put("mostPopularGenre", genre.getName()));

        // Самый продуктивный автор
        Optional<Author> productiveAuthor = findMostProductiveAuthor();
        productiveAuthor.ifPresent(author -> {
            stats.put("mostProductiveAuthor", author.getFullName());
            stats.put("authorBookCount", author.getBooks().size());
        });

        return stats;
    }

    /**
     * Ищет книги по комплексному запросу
     */
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return bookRepository.findAll();
        }

        Set<Book> results = new HashSet<>();

        // Поиск по названию
        results.addAll(bookRepository.findByTitleContaining(query));

        // Поиск по автору
        results.addAll(bookRepository.findByAuthorName(query));

        // Поиск по жанру
        results.addAll(bookRepository.findByGenre(query));

        // Поиск по ISBN
        bookRepository.findByIsbn(query).ifPresent(results::add);

        return new ArrayList<>(results);
    }

    /**
     * Получает книги определенного жанра
     */
    public List<Book> getBooksByGenre(String genreName) {
        return bookRepository.findByGenre(genreName);
    }

    /**
     * Получает книги определенного автора
     */
    public List<Book> getBooksByAuthor(Long authorId) {
        Optional<Author> author = authorRepository.findById(authorId);
        if (author.isPresent()) {
            return new ArrayList<>(author.get().getBooks());
        }
        return Collections.emptyList();
    }

    /**
     * Добавляет автора к книге
     */
    public Book addAuthorToBook(Long bookId, Long authorId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<Author> authorOpt = authorRepository.findById(authorId);

        if (bookOpt.isPresent() && authorOpt.isPresent()) {
            Book book = bookOpt.get();
            Author author = authorOpt.get();

            book.addAuthor(author);
            return bookRepository.save(book);
        }

        throw new IllegalArgumentException("Book or Author not found");
    }

    /**
     * Устанавливает жанр для книги
     */
    public Book setGenreForBook(Long bookId, Long genreId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        Optional<Genre> genreOpt = genreRepository.findById(genreId);

        if (bookOpt.isPresent() && genreOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setGenre(genreOpt.get());
            return bookRepository.save(book);
        }

        throw new IllegalArgumentException("Book or Genre not found");
    }

    /**
     * Находит самый популярный жанр (по количеству книг)
     */
    private Optional<Genre> findMostPopularGenre() {
        List<Genre> genres = genreRepository.findGenresWithBooks();
        if (genres.isEmpty()) {
            return Optional.empty();
        }

        return genres.stream()
                .max(Comparator.comparingInt(g -> g.getBooks().size()));
    }

    /**
     * Находит самого продуктивного автора (по количеству книг)
     */
    private Optional<Author> findMostProductiveAuthor() {
        List<Author> authors = authorRepository.findAll();
        if (authors.isEmpty()) {
            return Optional.empty();
        }

        return authors.stream()
                .filter(author -> author.getBooks() != null && !author.getBooks().isEmpty())
                .max(Comparator.comparingInt(a -> a.getBooks().size()));
    }

    /**
     * Получает все жанры с количеством книг
     */
    public Map<String, Integer> getGenresWithBookCounts() {
        List<Genre> genres = genreRepository.findAll();

        return genres.stream()
                .collect(Collectors.toMap(
                        Genre::getName,
                        genre -> genre.getBooks() != null ? genre.getBooks().size() : 0
                ));
    }

    /**
     * Получает все авторы с количеством книг
     */
    public Map<String, Integer> getAuthorsWithBookCounts() {
        List<Author> authors = authorRepository.findAll();

        return authors.stream()
                .collect(Collectors.toMap(
                        Author::getFullName,
                        author -> author.getBooks() != null ? author.getBooks().size() : 0
                ));
    }

    /**
     * Получает последние добавленные книги
     */
    public List<Book> getRecentlyAddedBooks(int limit) {
        List<Book> allBooks = bookRepository.findAll();

        // Сортируем по ID (предполагая, что больший ID = более новая книга)
        return allBooks.stream()
                .sorted((b1, b2) -> Long.compare(b2.getId(), b1.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}