package com.library.core.repository.loan.impl;

import com.library.core.domain.loan.BorrowRecord;
import com.library.core.repository.loan.BorrowRecordRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryBorrowRecordRepository implements BorrowRecordRepository {

    private final Map<Long, BorrowRecord> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<BorrowRecord> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<BorrowRecord> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public BorrowRecord save(BorrowRecord record) {
        if (record.getId() == null) {
            record.setId(idGenerator.getAndIncrement());
        }
        storage.put(record.getId(), record);
        return record;
    }

    @Override
    public void delete(BorrowRecord record) {
        if (record.getId() != null) {
            storage.remove(record.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public List<BorrowRecord> findByReaderId(Long readerId) {
        return storage.values().stream()
                .filter(record -> record.getReader() != null &&
                        record.getReader().getId().equals(readerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecord> findByBookCopyId(Long bookCopyId) {
        return storage.values().stream()
                .filter(record -> record.getBookCopy() != null &&
                        record.getBookCopy().getId().equals(bookCopyId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecord> findOverdueRecords(LocalDate currentDate) {
        return storage.values().stream()
                .filter(record -> record.getReturnDate() == null &&
                        record.getDueDate().isBefore(currentDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecord> findActiveBorrowsByReaderId(Long readerId) {
        return storage.values().stream()
                .filter(record -> record.getReader() != null &&
                        record.getReader().getId().equals(readerId) &&
                        record.getReturnDate() == null)
                .collect(Collectors.toList());
    }
}