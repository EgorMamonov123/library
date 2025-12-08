package com.library.core.repository.loan;

import com.library.core.domain.loan.BorrowRecord;
import com.library.core.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordRepository extends Repository<BorrowRecord> {
    List<BorrowRecord> findByReaderId(Long readerId);
    List<BorrowRecord> findByBookCopyId(Long bookCopyId);
    List<BorrowRecord> findOverdueRecords(LocalDate currentDate);
    List<BorrowRecord> findActiveBorrowsByReaderId(Long readerId);
}