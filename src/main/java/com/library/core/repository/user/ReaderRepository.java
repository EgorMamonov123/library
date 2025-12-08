package com.library.core.repository.user;

import com.library.core.domain.user.Reader;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ReaderRepository extends Repository<Reader> {
    Optional<Reader> findByLibraryCardNumber(String libraryCardNumber);
    List<Reader> findByLastName(String lastName);
    List<Reader> findActiveReaders();
}