package com.amoura.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookTransationHistoryRespository extends JpaRepository<BookTransactionHistory, Integer> {

    @Query("""
            select h
            from BookTransactionHistory h
            where h.user.id = : userId
           """)
    Page<BookTransactionHistory> findAllBorowedBooks(Pageable pageable, Integer userId);
}
