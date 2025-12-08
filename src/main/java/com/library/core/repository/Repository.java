package com.library.core.repository;

import com.library.core.domain.Entity;
import java.util.List;
import java.util.Optional;

public interface Repository<T extends Entity<Long>> {
    Optional<T> findById(Long id);
    List<T> findAll();
    T save(T entity);
    void delete(T entity);
    void deleteById(Long id);
    long count();
    boolean existsById(Long id);
}