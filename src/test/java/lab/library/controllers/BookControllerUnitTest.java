package lab.library.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.library.controllers.BookController.AddBookRequest;
import lab.library.controllers.BookController.UpdateBookRequest;
import lab.library.enums.BookStatus;
import lab.library.exceptions.BookNotFoundException;
import lab.library.models.Book;
import lab.library.services.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerUnitTest {

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private BookController bookController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Book createTestBook() {
        Book testBook = new Book("Test Book", "Test Author", "1234567890");
        testBook.setId(1L);
        testBook.setStatus(BookStatus.AVAILABLE);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);
        return testBook;
    }

    @Test
    void getAllBooks_ShouldReturnBooksList() {
        // Arrange
        Book testBook = createTestBook();
        List<Book> books = Arrays.asList(testBook);
        when(libraryService.getAllBooks()).thenReturn(books);

        // Act
        ResponseEntity<Object> response = bookController.getAllBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<Book> result = (List<Book>) response.getBody();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");

        verify(libraryService, times(1)).getAllBooks();
    }

    @Test
    void getAllBooks_WhenServiceThrowsException_ShouldReturn500() {
        // Arrange
        when(libraryService.getAllBooks()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Object> response = bookController.getAllBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");

        verify(libraryService, times(1)).getAllBooks();
    }

    @Test
    void getBookById_WithExistingId_ShouldReturnBook() {
        // Arrange
        Book testBook = createTestBook();
        when(libraryService.getBookById(1L)).thenReturn(testBook);

        // Act
        ResponseEntity<Object> response = bookController.getBookById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Book.class);
        Book result = (Book) response.getBody();
        assertThat(result.getTitle()).isEqualTo("Test Book");

        verify(libraryService, times(1)).getBookById(1L);
    }

    @Test
    void getBookById_WithNonExistingId_ShouldReturn404() {
        // Arrange
        when(libraryService.getBookById(999L))
                .thenThrow(new BookNotFoundException(999L));

        // Act
        ResponseEntity<Object> response = bookController.getBookById(999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Book with id 999 not found");

        verify(libraryService, times(1)).getBookById(999L);
    }

    @Test
    void addBook_WithValidData_ShouldReturnCreatedBook() {
        // Arrange
        Book testBook = createTestBook();
        AddBookRequest request = new AddBookRequest();
        request.setTitle("New Book");
        request.setAuthor("New Author");
        request.setIsbn("978-5-17-090456-7");

        when(libraryService.addBook(
                eq("New Book"),
                eq("New Author"),
                eq("978-5-17-090456-7"),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(testBook);

        // Act
        ResponseEntity<Object> response = bookController.addBook(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Book.class);
        Book result = (Book) response.getBody();
        assertThat(result.getTitle()).isEqualTo("Test Book");

        verify(libraryService, times(1)).addBook(
                eq("New Book"),
                eq("New Author"),
                eq("978-5-17-090456-7"),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void deleteBook_WithExistingId_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(libraryService).deleteBook(1L);

        // Act
        ResponseEntity<String> response = bookController.deleteBook(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Book deleted successfully");

        verify(libraryService, times(1)).deleteBook(1L);
    }

    @Test
    void deleteBook_WithNonExistingId_ShouldReturn404() {
        // Arrange
        doThrow(new BookNotFoundException(999L))
                .when(libraryService).deleteBook(999L);

        // Act
        ResponseEntity<String> response = bookController.deleteBook(999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Book with id 999 not found");

        verify(libraryService, times(1)).deleteBook(999L);
    }
}