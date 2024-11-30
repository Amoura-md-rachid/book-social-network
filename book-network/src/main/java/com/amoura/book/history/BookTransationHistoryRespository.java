package com.amoura.book.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookTransationHistoryRespository extends JpaRepository<BookTransactionHistory, Integer> {

    @Query("""
            select h
            from BookTransactionHistory h
            where h.user.id = : userId
           """)
    Page<BookTransactionHistory> findAllBorowedBooks(Pageable pageable, Integer userId);

    @Query("""
            select h
            from BookTransactionHistory h
            where h.book.owner.id = : userId
           """)
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Integer id);

    @Query("""
            select 
            (count(*) > 0 ) as isBorrowed
            from BookTransactionHistory h
            where h.user.id = : userId
            and h.book.id = : bookId
            and h.returnApproved = false 
           """)
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);


    @Query("""
            select transaction
            from BookTransactionHistory transaction
            where transaction.user.id = : userId
            and transaction.book.id = : bookId
            and transaction.returned = false
            and transaction.returnApproved = false
           """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer userId);

    @Query("""
           select transaction
            from BookTransactionHistory transaction
            where transaction.book.owner.id = : userId
            and transaction.book.id = : bookId
            and transaction.returned = true
            and transaction.returnApproved = false
           """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer id);
}
