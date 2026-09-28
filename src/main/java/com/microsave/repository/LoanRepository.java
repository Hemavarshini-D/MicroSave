package com.microsave.repository;

import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByMemberMemberId(Long memberId);
    boolean existsByMemberMemberIdAndStatus(Long memberId, LoanStatus status);
    
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByMemberGroupGroupIdAndStatus(Long groupId, LoanStatus status);
    
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingLoans();
}
