package com.microsave.repository;

import com.microsave.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanLoanId(Long loanId);
    
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Repayment r WHERE r.loan.loanId = :loanId")
    BigDecimal getTotalRepaymentsForLoan(@Param("loanId") Long loanId);
}
