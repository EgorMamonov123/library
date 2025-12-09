package com.library.service;

import com.library.core.domain.user.Reader;
import com.library.core.repository.user.ReaderRepository;
import com.library.core.repository.user.impl.InMemoryReaderRepository;
import com.library.core.service.ReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReaderServiceTest {

    private ReaderRepository readerRepository;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        readerRepository = new InMemoryReaderRepository();
        readerService = new ReaderService(readerRepository);
    }

    @Test
    void whenRegisterValidReader_thenReaderIsSaved() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        reader.setEmail("john.doe@example.com");
        reader.setPhone("+1234567890");

        // When
        Reader saved = readerService.registerReader(reader);

        // Then
        assertNotNull(saved.getId());
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("12345", saved.getLibraryCardNumber());
        assertTrue(readerRepository.existsById(saved.getId()));
    }

    @Test
    void whenRegisterReaderWithoutCardNumber_thenThrowException() {
        // Given
        Reader reader = new Reader("", "John", "Doe");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readerService.registerReader(reader);
        });
    }

    @Test
    void whenRegisterReaderWithoutFirstName_thenThrowException() {
        // Given
        Reader reader = new Reader("12345", "", "Doe");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readerService.registerReader(reader);
        });
    }

    @Test
    void whenGetReaderById_thenReturnCorrectReader() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        Reader saved = readerService.registerReader(reader);

        // When
        Optional<Reader> found = readerService.getReaderById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void whenGetReaderByLibraryCard_thenReturnCorrectReader() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        readerService.registerReader(reader);

        // When
        Optional<Reader> found = readerService.getReaderByLibraryCard("12345");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void whenFindReadersByLastName_thenReturnMatchingReaders() {
        // Given
        readerService.registerReader(new Reader("111", "John", "Doe"));
        readerService.registerReader(new Reader("222", "Jane", "Doe"));
        readerService.registerReader(new Reader("333", "Bob", "Smith"));

        // When
        List<Reader> does = readerService.findReadersByLastName("Doe");

        // Then
        assertEquals(2, does.size());
        assertTrue(does.stream().allMatch(r -> "Doe".equals(r.getLastName())));
    }

    @Test
    void whenGetActiveReaders_thenReturnOnlyActive() {
        // Given
        Reader reader1 = new Reader("111", "John", "Doe");
        Reader reader2 = new Reader("222", "Jane", "Doe");

        // Регистрируем обоих читателей через сервис (они будут активными)
        Reader saved1 = readerService.registerReader(reader1);
        Reader saved2 = readerService.registerReader(reader2);

        // Деактивируем второго читателя через сервис
        readerService.deactivateReader(saved2.getId());

        // When
        List<Reader> activeReaders = readerService.getActiveReaders();

        // Then
        assertEquals(1, activeReaders.size());
        assertEquals("John", activeReaders.get(0).getFirstName());
    }

    @Test
    void whenUpdateReaderInfo_thenInfoIsUpdated() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        Reader saved = readerService.registerReader(reader);

        // When
        Reader updated = readerService.updateReaderInfo(
                saved.getId(),
                "new.email@example.com",
                "+9876543210"
        );

        // Then
        assertEquals("new.email@example.com", updated.getEmail());
        assertEquals("+9876543210", updated.getPhone());
        assertEquals("John", updated.getFirstName()); // Имя не изменилось
    }

    @Test
    void whenDeactivateReader_thenReaderBecomesInactive() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        Reader saved = readerService.registerReader(reader);

        // When
        boolean result = readerService.deactivateReader(saved.getId());

        // Then
        assertTrue(result);
        Optional<Reader> deactivated = readerService.getReaderById(saved.getId());
        assertTrue(deactivated.isPresent());
        assertFalse(deactivated.get().getActive());
    }

    @Test
    void whenActivateReader_thenReaderBecomesActive() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        reader.setActive(false);
        Reader saved = readerService.registerReader(reader);

        // When
        boolean result = readerService.activateReader(saved.getId());

        // Then
        assertTrue(result);
        Optional<Reader> activated = readerService.getReaderById(saved.getId());
        assertTrue(activated.isPresent());
        assertTrue(activated.get().getActive());
    }

    @Test
    void whenRegisterReaderWithInvalidEmail_thenThrowException() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        reader.setEmail("invalid-email");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readerService.registerReader(reader);
        });
    }

    @Test
    void whenRegisterReaderWithFutureRegistrationDate_thenThrowException() {
        // Given
        Reader reader = new Reader("12345", "John", "Doe");
        reader.setRegistrationDate(LocalDate.now().plusDays(1)); // Дата в будущем

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readerService.registerReader(reader);
        });
    }

    @Test
    void whenGetReaderCount_thenReturnCorrectNumber() {
        // Given
        readerService.registerReader(new Reader("111", "John", "Doe"));
        readerService.registerReader(new Reader("222", "Jane", "Doe"));
        readerService.registerReader(new Reader("333", "Bob", "Smith"));

        // When
        long count = readerService.getReaderCount();

        // Then
        assertEquals(3, count);
    }

    @Test
    void whenUpdateNonExistentReader_thenThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            readerService.updateReaderInfo(999L, "email@test.com", "123456");
        });
    }

    @Test
    void whenDeactivateNonExistentReader_thenReturnFalse() {
        // When
        boolean result = readerService.deactivateReader(999L);

        // Then
        assertFalse(result);
    }
}