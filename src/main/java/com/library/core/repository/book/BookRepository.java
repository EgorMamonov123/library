package com.library.core.repository.book;

import com.library.core.domain.book.Book;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends Repository<Book> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorName(String authorName);
    List<Book> findByGenre(String genreName);
    List<Book> findAvailableBooks();
    List<Book> findBooksPublishedAfter(int year);
}