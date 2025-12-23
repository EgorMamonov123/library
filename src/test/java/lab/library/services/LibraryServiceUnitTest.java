package lab.library.services;

import lab.library.enums.BookStatus;
import lab.library.exceptions.BookNotFoundException;
import lab.library.exceptions.ReaderNotFoundException;
import lab.library.models.Book;
import lab.library.models.Reader;
import lab.library.repositories.BookRepository;
import lab.library.repositories.LoanRepository;
import lab.library.repositories.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReaderRepository readerRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LibraryService libraryService;

    @Test
    void getBookById_WithExistingId_ShouldReturnBook() {
        // Arrange
        Book book = new Book("Test Book", "Test Author", "1234567890");
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act
        Book result = libraryService.getBookById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getBookById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> libraryService.getBookById(999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book with id 999 not found");

        verify(bookRepository, times(1)).findById(999L);
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Arrange
        Book book = new Book("Test Book", "Test Author", "1234567890");
        when(bookRepository.findAll()).thenReturn(List.of(book));

        // Act
        List<Book> result = libraryService.getAllBooks();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getReaderById_WithExistingId_ShouldReturnReader() {
        // Arrange
        Reader reader = new Reader("John", "Doe", "john@example.com", "RDR123456");
        reader.setId(1L);
        when(readerRepository.findById(1L)).thenReturn(Optional.of(reader));

        // Act
        Reader result = libraryService.getReaderById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(readerRepository, times(1)).findById(1L);
    }

    @Test
    void getReaderById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(readerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> libraryService.getReaderById(999L))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessage("Reader with ID 999 not found");

        verify(readerRepository, times(1)).findById(999L);
    }

    @Test
    void addBook_ShouldSaveAndReturnBook() {
        // Arrange
        Book book = new Book("Test Book", "Test Author", "1234567890");
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = libraryService.addBook(
                "Test Book", "Test Author", "1234567890",
                null, null, null, null, null, null
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void registerReader_ShouldSaveAndReturnReader() {
        // Arrange
        Reader reader = new Reader("John", "Doe", "john@example.com", "RDR123456");
        when(readerRepository.save(any(Reader.class))).thenReturn(reader);

        // Act
        Reader result = libraryService.registerReader(
                "John", "Doe", "john@example.com", "RDR123456", "+1234567890"
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(readerRepository, times(1)).save(any(Reader.class));
    }
}