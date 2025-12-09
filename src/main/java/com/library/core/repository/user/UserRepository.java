package com.library.core.repository.user;

import com.library.core.domain.user.User;
import com.library.core.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findActiveUsers();
    List<User> findByLastName(String lastName);
    Optional<User> findByLibraryCardNumber(String libraryCardNumber);
    long countByRole(String role);
}