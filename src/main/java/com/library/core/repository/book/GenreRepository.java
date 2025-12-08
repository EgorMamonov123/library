package com.library.core.repository.book;

import com.library.core.domain.book.Genre;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends Repository<Genre> {
    Optional<Genre> findByName(String name);
    List<Genre> findGenresWithBooks();
    List<Genre> searchByName(String searchTerm);
}