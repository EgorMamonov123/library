package lab.library.controllers;

import lab.library.controllers.LoanController.BorrowBookRequest;
import lab.library.exceptions.BookNotFoundException;
import lab.library.exceptions.ReaderNotFoundException;
import lab.library.models.Book;
import lab.library.models.Loan;
import lab.library.models.Reader;
import lab.library.services.LibraryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanControllerUnitTest {

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private LoanController loanController;

    private Loan createTestLoan() {
        Book testBook = new Book("Test Book", "Test Author", "1234567890");
        testBook.setId(1L);

        Reader testReader = new Reader("John", "Doe", "john@example.com", "RDR123456");
        testReader.setId(1L);

        Loan testLoan = new Loan(testBook, testReader, LocalDate.now(), LocalDate.now().plusDays(14));
        testLoan.setId(1L);
        return testLoan;
    }

    @Test
    void getAllLoans_ShouldReturnLoansList() {
        // Arrange
        Loan testLoan = createTestLoan();
        List<Loan> loans = Arrays.asList(testLoan);
        when(libraryService.getAllLoans()).thenReturn(loans);

        // Act
        ResponseEntity<Object> response = loanController.getAllLoans();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<Loan> result = (List<Loan>) response.getBody();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(libraryService, times(1)).getAllLoans();
    }

    @Test
    void borrowBook_WithValidData_ShouldReturnCreatedLoan() {
        // Arrange
        Loan testLoan = createTestLoan();
        BorrowBookRequest request = new BorrowBookRequest();
        request.setBookId(1L);
        request.setReaderId(1L);
        request.setLoanDays(14);
        request.setNotes("Test notes");

        when(libraryService.borrowBook(1L, 1L, 14, "Test notes")).thenReturn(testLoan);

        // Act
        ResponseEntity<Object> response = loanController.borrowBook(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Loan.class);
        Loan result = (Loan) response.getBody();
        assertThat(result.getId()).isEqualTo(1L);

        verify(libraryService, times(1)).borrowBook(1L, 1L, 14, "Test notes");
    }

    @Test
    void borrowBook_WithNonExistingBook_ShouldReturn404() {
        // Arrange
        BorrowBookRequest request = new BorrowBookRequest();
        request.setBookId(999L);
        request.setReaderId(1L);
        request.setLoanDays(14);

        when(libraryService.borrowBook(999L, 1L, 14, null))
                .thenThrow(new BookNotFoundException(999L));

        // Act
        ResponseEntity<Object> response = loanController.borrowBook(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) response.getBody()).contains("Book not found");

        verify(libraryService, times(1)).borrowBook(999L, 1L, 14, null);
    }

    @Test
    void borrowBook_WithNonExistingReader_ShouldReturn404() {
        // Arrange
        BorrowBookRequest request = new BorrowBookRequest();
        request.setBookId(1L);
        request.setReaderId(999L);
        request.setLoanDays(14);

        when(libraryService.borrowBook(1L, 999L, 14, null))
                .thenThrow(new ReaderNotFoundException(999L));

        // Act
        ResponseEntity<Object> response = loanController.borrowBook(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) response.getBody()).contains("Reader not found");

        verify(libraryService, times(1)).borrowBook(1L, 999L, 14, null);
    }
}