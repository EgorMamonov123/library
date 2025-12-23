package lab.library.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lab.library.exceptions.BookNotFoundException;
import lab.library.exceptions.ReaderNotFoundException;
import lab.library.models.Book;
import lab.library.models.Loan;
import lab.library.models.Reader;
import lab.library.services.LibraryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Loans", description = "Управление выдачей книг")
public class LoanController {

    private final LibraryService libraryService;

    @GetMapping
    @Operation(summary = "Получить все выдачи книг", tags = "Loans")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список выдач успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Loan.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Object> getAllLoans() {
        log.info("Getting all book loans");
        try {
            List<Loan> loans = libraryService.getAllLoans();
            log.info("Retrieved {} loans", loans.size());
            return ResponseEntity.ok(loans);
        } catch (RuntimeException e) {
            log.error("Error getting all loans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/overdue")
    @Operation(summary = "Получить просроченные выдачи", tags = "Loans")
    public ResponseEntity<Object> getOverdueLoans() {
        log.info("Getting overdue loans");
        try {
            List<Loan> overdueLoans = libraryService.getOverdueLoans();
            log.info("Found {} overdue loans", overdueLoans.size());
            return ResponseEntity.ok(overdueLoans);
        } catch (RuntimeException e) {
            log.error("Error getting overdue loans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/reader/{readerId}")
    @Operation(summary = "Получить выдачи читателя", tags = "Loans")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Выдачи читателя успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Loan.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Читатель не найден"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Object> getReaderLoans(@PathVariable Long readerId) {
        log.info("Getting loans for reader {}", readerId);
        try {
            List<Loan> loans = libraryService.getReaderLoans(readerId);
            log.info("Found {} loans for reader {}", loans.size(), readerId);
            return ResponseEntity.ok(loans);
        } catch (ReaderNotFoundException e) {
            log.warn("Reader not found: {}", readerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reader with id " + readerId + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting loans for reader {}", readerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/borrow")
    @Operation(summary = "Выдать книгу читателю", tags = "Loans")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Книга успешно выдана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невозможно выдать книгу"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Книга или читатель не найдены"
            )
    })
    public ResponseEntity<Object> borrowBook(@RequestBody BorrowBookRequest request) {
        log.info("Borrowing book {} to reader {} for {} days",
                request.getBookId(), request.getReaderId(), request.getLoanDays());

        try {
            if (request.getLoanDays() <= 0 || request.getLoanDays() > 60) {
                return ResponseEntity.badRequest().body("Loan days must be between 1 and 60");
            }

            Loan loan = libraryService.borrowBook(
                    request.getBookId(),
                    request.getReaderId(),
                    request.getLoanDays(),
                    request.getNotes()
            );

            log.info("Book borrowed successfully with loan ID {}", loan.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);

        } catch (BookNotFoundException e) {
            log.warn("Book not found: {}", request.getBookId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found: " + e.getMessage());
        } catch (ReaderNotFoundException e) {
            log.warn("Reader not found: {}", request.getReaderId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reader not found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Cannot borrow book: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Cannot borrow book: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error borrowing book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/return/{loanId}")
    @Operation(summary = "Вернуть книгу", tags = "Loans")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Книга успешно возвращена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Loan.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Выдача не найдена"
            )
    })
    public ResponseEntity<Object> returnBook(
            @PathVariable Long loanId,
            @RequestParam(required = false) String conditionNotes) {

        log.info("Returning book for loan {}", loanId);

        try {
            Loan returnedLoan = libraryService.returnBook(loanId, conditionNotes);
            log.info("Book returned successfully for loan {}", loanId);
            return ResponseEntity.ok(returnedLoan);
        } catch (RuntimeException e) {
            log.warn("Loan not found: {}", loanId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan with id " + loanId + " not found");
        } catch (Exception e) {
            log.error("Error returning book", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PutMapping("/{loanId}/extend")
    @Operation(summary = "Продлить срок выдачи", tags = "Loans")
    public ResponseEntity<Object> extendLoan(
            @PathVariable Long loanId,
            @RequestParam @Parameter(description = "Количество дополнительных дней") Integer additionalDays) {

        log.info("Extending loan {} by {} days", loanId, additionalDays);

        try {
            if (additionalDays <= 0 || additionalDays > 30) {
                return ResponseEntity.badRequest().body("Additional days must be between 1 and 30");
            }

            Loan extendedLoan = libraryService.extendLoan(loanId, additionalDays);
            return ResponseEntity.ok(extendedLoan);
        } catch (RuntimeException e) {
            log.warn("Cannot extend loan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot extend loan: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error extending loan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @Getter
    @Setter
    @Schema(description = "Запрос на выдачу книги")
    public static class BorrowBookRequest {
        @Schema(description = "ID книги", example = "1")
        private Long bookId;

        @Schema(description = "ID читателя", example = "1")
        private Long readerId;

        @Schema(description = "Количество дней выдачи", example = "14")
        private Integer loanDays = 14;

        @Schema(description = "Примечания", example = "Выдано для курсовой работы")
        private String notes;
    }
}