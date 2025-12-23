package lab.library.repositories;

import lab.library.models.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    Optional<Reader> findByLibraryCardNumber(String libraryCardNumber);

    Optional<Reader> findByEmail(String email);

    List<Reader> findByLastNameContainingIgnoreCase(String lastName);

    @Query("SELECT r FROM Reader r WHERE " +
            "LOWER(r.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.libraryCardNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Reader> searchReaders(@Param("query") String query);

    @Modifying
    @Transactional
    @Query("UPDATE Reader r SET r.maxBooksAllowed = :maxBooks WHERE r.id = :readerId")
    int updateReaderMaxBooks(@Param("readerId") Long readerId, @Param("maxBooks") Integer maxBooks);
}