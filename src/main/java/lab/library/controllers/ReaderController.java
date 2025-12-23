package lab.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lab.library.exceptions.ReaderNotFoundException;
import lab.library.models.Reader;
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
@RequestMapping("/api/readers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Readers", description = "Управление читателями")
public class ReaderController {

    private final LibraryService libraryService;

    @GetMapping
    @Operation(summary = "Получить всех читателей", tags = "Readers")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список читателей успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reader.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Object> getAllReaders() {
        log.info("Getting all readers");
        try {
            List<Reader> readers = libraryService.getAllReaders();
            log.info("Retrieved {} readers", readers.size());
            return ResponseEntity.ok(readers);
        } catch (RuntimeException e) {
            log.error("Error getting all readers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить читателя по ID", tags = "Readers")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Читатель найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reader.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Читатель не найден"
            )
    })
    public ResponseEntity<Object> getReaderById(@PathVariable Long id) {
        log.info("Getting reader by ID {}", id);
        try {
            Reader reader = libraryService.getReaderById(id);
            log.info("Found reader {} {}", reader.getFirstName(), reader.getLastName());
            return ResponseEntity.ok(reader);
        } catch (ReaderNotFoundException e) {
            log.warn("Reader not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reader with id " + id + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting reader {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск читателей", tags = "Readers")
    public ResponseEntity<Object> searchReaders(@RequestParam String query) {
        log.info("Searching readers with query: {}", query);
        try {
            List<Reader> readers = libraryService.searchReaders(query);
            log.info("Found {} readers matching query", readers.size());
            return ResponseEntity.ok(readers);
        } catch (RuntimeException e) {
            log.error("Error searching readers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping
    @Operation(summary = "Зарегистрировать нового читателя", tags = "Readers")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Читатель успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reader.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные"
            )
    })
    public ResponseEntity<Object> registerReader(@RequestBody RegisterReaderRequest request) {
        log.info("Registering new reader: {} {}", request.getFirstName(), request.getLastName());

        try {
            // Генерация номера читательского билета
            String libraryCardNumber = generateLibraryCardNumber();

            Reader reader = libraryService.registerReader(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    libraryCardNumber,
                    request.getPhoneNumber()
            );

            log.info("Reader registered successfully with ID {} and card number {}",
                    reader.getId(), reader.getLibraryCardNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(reader);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid reader data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid reader data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error registering reader", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить информацию о читателе", tags = "Readers")
    public ResponseEntity<Object> updateReader(@PathVariable Long id,
                                               @RequestBody UpdateReaderRequest request) {
        log.info("Updating reader {}", id);

        try {
            Reader updatedReader = libraryService.updateReader(
                    id,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    request.getMaxBooksAllowed()
            );

            return ResponseEntity.ok(updatedReader);
        } catch (ReaderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reader not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error updating reader", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить читателя", tags = "Readers")
    public ResponseEntity<String> deleteReader(@PathVariable Long id) {
        log.info("Deleting reader {}", id);

        try {
            libraryService.deleteReader(id);
            log.info("Reader {} deleted successfully", id);
            return ResponseEntity.ok("Reader deleted successfully");
        } catch (ReaderNotFoundException e) {
            log.warn("Reader not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reader with id " + id + " not found");
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete reader: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Cannot delete reader: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error deleting reader {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    private String generateLibraryCardNumber() {
        // Простая генерация номера читательского билета
        long timestamp = System.currentTimeMillis();
        return "RDR" + String.format("%08d", timestamp % 100000000);
    }

    @Getter
    @Setter
    @Schema(description = "Запрос на регистрацию читателя")
    public static class RegisterReaderRequest {
        @Schema(description = "Имя", example = "Иван")
        private String firstName;

        @Schema(description = "Фамилия", example = "Иванов")
        private String lastName;

        @Schema(description = "Email", example = "ivanov@example.com")
        private String email;

        @Schema(description = "Телефон", example = "+7 (123) 456-78-90")
        private String phoneNumber;
    }

    @Getter
    @Setter
    @Schema(description = "Запрос на обновление информации о читателе")
    public static class UpdateReaderRequest {
        @Schema(description = "Имя", example = "Иван")
        private String firstName;

        @Schema(description = "Фамилия", example = "Иванов")
        private String lastName;

        @Schema(description = "Email", example = "ivanov@example.com")
        private String email;

        @Schema(description = "Телефон", example = "+7 (123) 456-78-90")
        private String phoneNumber;

        @Schema(description = "Максимальное количество книг", example = "5")
        private Integer maxBooksAllowed;
    }
}