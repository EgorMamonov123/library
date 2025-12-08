package com.library.core.repository.book;

import com.library.core.domain.book.BookCopy;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends Repository<BookCopy> {
    Optional<BookCopy> findByInventoryNumber(String inventoryNumber);
    List<BookCopy> findByBookId(Long bookId);
    List<BookCopy> findByStatus(BookCopy.CopyStatus status);
    List<BookCopy> findAvailableCopiesByBookId(Long bookId);
}