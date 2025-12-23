package lab.library.repositories;

import lab.library.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByReaderId(Long readerId);

    List<Loan> findByBookId(Long bookId);

    List<Loan> findByReturnDateIsNull();

    @Query("SELECT l FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    @Query("SELECT l FROM Loan l WHERE l.loanDate >= :startDate AND l.loanDate <= :endDate")
    List<Loan> findLoansBetweenDates(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.reader.id = :readerId AND l.returnDate IS NULL")
    Long countActiveLoansByReader(@Param("readerId") Long readerId);
}