package com.microsave.repository;

import com.microsave.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByMemberMemberId(Long memberId);
    
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c")
    BigDecimal getTotalContributions();

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c WHERE c.member.group.groupId = :groupId")
    BigDecimal getTotalContributionsByGroupId(@Param("groupId") Long groupId);
}
