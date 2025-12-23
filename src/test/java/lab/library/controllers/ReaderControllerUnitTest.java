package lab.library.controllers;

import lab.library.controllers.ReaderController.RegisterReaderRequest;
import lab.library.controllers.ReaderController.UpdateReaderRequest;
import lab.library.exceptions.ReaderNotFoundException;
import lab.library.models.Reader;
import lab.library.enums.UserRole;
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
class ReaderControllerUnitTest {

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private ReaderController readerController;

    private Reader createTestReader() {
        Reader testReader = new Reader("John", "Doe", "john@example.com", "RDR123456");
        testReader.setId(1L);
        testReader.setPhoneNumber("+1234567890");
        testReader.setRegistrationDate(LocalDate.now());
        testReader.setMaxBooksAllowed(5);
        testReader.setRole(UserRole.READER);
        return testReader;
    }

    @Test
    void getAllReaders_ShouldReturnReadersList() {
        // Arrange
        Reader testReader = createTestReader();
        List<Reader> readers = Arrays.asList(testReader);
        when(libraryService.getAllReaders()).thenReturn(readers);

        // Act
        ResponseEntity<Object> response = readerController.getAllReaders();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<Reader> result = (List<Reader>) response.getBody();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");

        verify(libraryService, times(1)).getAllReaders();
    }

    @Test
    void getReaderById_WithExistingId_ShouldReturnReader() {
        // Arrange
        Reader testReader = createTestReader();
        when(libraryService.getReaderById(1L)).thenReturn(testReader);

        // Act
        ResponseEntity<Object> response = readerController.getReaderById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Reader.class);
        Reader result = (Reader) response.getBody();
        assertThat(result.getFirstName()).isEqualTo("John");

        verify(libraryService, times(1)).getReaderById(1L);
    }

    @Test
    void getReaderById_WithNonExistingId_ShouldReturn404() {
        // Arrange
        when(libraryService.getReaderById(999L))
                .thenThrow(new ReaderNotFoundException(999L));

        // Act
        ResponseEntity<Object> response = readerController.getReaderById(999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Reader with id 999 not found");

        verify(libraryService, times(1)).getReaderById(999L);
    }

    @Test
    void registerReader_WithValidData_ShouldReturnCreatedReader() {
        // Arrange
        Reader testReader = createTestReader();
        RegisterReaderRequest request = new RegisterReaderRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("+1234567890");

        when(libraryService.registerReader(
                eq("John"),
                eq("Doe"),
                eq("john@example.com"),
                anyString(),
                eq("+1234567890")
        )).thenReturn(testReader);

        // Act
        ResponseEntity<Object> response = readerController.registerReader(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Reader.class);
        Reader result = (Reader) response.getBody();
        assertThat(result.getFirstName()).isEqualTo("John");

        verify(libraryService, times(1)).registerReader(
                eq("John"),
                eq("Doe"),
                eq("john@example.com"),
                anyString(),
                eq("+1234567890")
        );
    }

    @Test
    void deleteReader_WithExistingId_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(libraryService).deleteReader(1L);

        // Act
        ResponseEntity<String> response = readerController.deleteReader(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Reader deleted successfully");

        verify(libraryService, times(1)).deleteReader(1L);
    }
}