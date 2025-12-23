package lab.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lab.library.enums.BookStatus;
import lab.library.exceptions.BookNotFoundException;
import lab.library.models.Book;
import lab.library.services.LibraryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Books", description = "Управление каталогом книг")
public class BookController {

    private final LibraryService libraryService;

    @GetMapping
    @Operation(summary = "Получить все книги", tags = "Books")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список книг успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Book.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Object> getAllBooks() {
        log.info("Getting all books");
        try {
            List<Book> books = libraryService.getAllBooks();
            log.info("Retrieved {} books", books.size());
            return ResponseEntity.ok(books);
        } catch (RuntimeException e) {
            log.error("Error getting all books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID", tags = "Books")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга не найдена"
            )
    })
    public ResponseEntity<Object> getBookById(@PathVariable Long id) {
        log.info("Getting book by ID {}", id);
        try {
            Book book = libraryService.getBookById(id);
            log.info("Found book: {}", book.getTitle());
            return ResponseEntity.ok(book);
        } catch (BookNotFoundException e) {
            log.warn("Book not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book with id " + id + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting book {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Получить книгу по ISBN", tags = "Books")
    public ResponseEntity<Object> getBookByIsbn(@PathVariable String isbn) {
        log.info("Getting book by ISBN {}", isbn);
        try {
            Book book = libraryService.getBookByIsbn(isbn);
            return ResponseEntity.ok(book);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book with ISBN " + isbn + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting book by ISBN", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск книг", tags = "Books")
    public ResponseEntity<Object> searchBooks(
            @RequestParam String query,
            @RequestParam(required = false) BookStatus status) {

        log.info("Searching books with query: {}", query);

        try {
            List<Book> books;
            if (status != null) {
                books = libraryService.searchBooksByStatus(query, status);
            } else {
                books = libraryService.searchBooks(query);
            }

            log.info("Found {} books matching query", books.size());
            return ResponseEntity.ok(books);
        } catch (RuntimeException e) {
            log.error("Error searching books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Получить доступные книги", tags = "Books")
    public ResponseEntity<Object> getAvailableBooks() {
        log.info("Getting available books");
        try {
            List<Book> books = libraryService.getAvailableBooks();
            log.info("Found {} available books", books.size());
            return ResponseEntity.ok(books);
        } catch (RuntimeException e) {
            log.error("Error getting available books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping
    @Operation(summary = "Добавить новую книгу", tags = "Books")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Книга успешно добавлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            )
    })
    public ResponseEntity<Object> addBook(@RequestBody AddBookRequest request) {
        log.info("Adding new book: {} by {}", request.getTitle(), request.getAuthor());

        try {
            Book book = libraryService.addBook(
                    request.getTitle(),
                    request.getAuthor(),
                    request.getIsbn(),
                    request.getPublisher(),
                    request.getPublicationYear(),
                    request.getGenre(),
                    request.getPages(),
                    request.getDescription(),
                    request.getTotalCopies()
            );

            log.info("Book added successfully with ID {}", book.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(book);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid book data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid book data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error adding book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить информацию о книге", tags = "Books")
    public ResponseEntity<Object> updateBook(@PathVariable Long id,
                                             @RequestBody UpdateBookRequest request) {
        log.info("Updating book {}", id);

        try {
            Book updatedBook = libraryService.updateBook(
                    id,
                    request.getTitle(),
                    request.getAuthor(),
                    request.getIsbn(),
                    request.getPublisher(),
                    request.getPublicationYear(),
                    request.getGenre(),
                    request.getPages(),
                    request.getDescription(),
                    request.getTotalCopies()
            );

            return ResponseEntity.ok(updatedBook);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error updating book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Обновить статус книги", tags = "Books")
    public ResponseEntity<Object> updateBookStatus(@PathVariable Long id,
                                                   @RequestParam BookStatus status) {
        log.info("Updating book {} status to {}", id, status);

        try {
            Book updatedBook = libraryService.updateBookStatus(id, status);
            return ResponseEntity.ok(updatedBook);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        } catch (RuntimeException e) {
            log.error("Error updating book status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу", tags = "Books")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        log.info("Deleting book {}", id);

        try {
            libraryService.deleteBook(id);
            log.info("Book {} deleted successfully", id);
            return ResponseEntity.ok("Book deleted successfully");
        } catch (BookNotFoundException e) {
            log.warn("Book not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book with id " + id + " not found");
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete book: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Cannot delete book: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error deleting book {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Getter
    @Setter
    @Schema(description = "Запрос на добавление книги")
    public static class AddBookRequest {
        @Schema(description = "Название книги", example = "Война и мир")
        private String title;

        @Schema(description = "Автор", example = "Лев Толстой")
        private String author;

        @Schema(description = "ISBN", example = "978-5-17-090456-7")
        private String isbn;

        @Schema(description = "Издательство", example = "АСТ")
        private String publisher;

        @Schema(description = "Год издания", example = "2020")
        private Integer publicationYear;

        @Schema(description = "Жанр", example = "Роман")
        private String genre;

        @Schema(description = "Количество страниц", example = "1225")
        private Integer pages;

        @Schema(description = "Описание", example = "Роман-эпопея Льва Толстого")
        private String description;

        @Schema(description = "Общее количество экземпляров", example = "3")
        private Integer totalCopies = 1;
    }

    @Getter
    @Setter
    @Schema(description = "Запрос на обновление информации о книге")
    public static class UpdateBookRequest {
        @Schema(description = "Название книги", example = "Война и мир")
        private String title;

        @Schema(description = "Автор", example = "Лев Толстой")
        private String author;

        @Schema(description = "ISBN", example = "978-5-17-090456-7")
        private String isbn;

        @Schema(description = "Издательство", example = "АСТ")
        private String publisher;

        @Schema(description = "Год издания", example = "2020")
        private Integer publicationYear;

        @Schema(description = "Жанр", example = "Роман")
        private String genre;

        @Schema(description = "Количество страниц", example = "1225")
        private Integer pages;

        @Schema(description = "Описание", example = "Роман-эпопея Льва Толстого")
        private String description;

        @Schema(description = "Общее количество экземпляров", example = "3")
        private Integer totalCopies;
    }
}