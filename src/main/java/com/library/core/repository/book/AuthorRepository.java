package com.library.core.repository.book;

import com.library.core.domain.book.Author;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends Repository<Author> {
    Optional<Author> findByFirstNameAndLastName(String firstName, String lastName);
    List<Author> findByLastName(String lastName);
    List<Author> findByCountry(String country);
    List<Author> findAuthorsWithMultipleBooks(int minBooks);
}