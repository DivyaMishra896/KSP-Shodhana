package com.ksp.shodhana.repository;

import com.ksp.shodhana.model.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    List<FinancialTransaction> findByLinkedCriminalId(Long linkedCriminalId);
    List<FinancialTransaction> findByIsFlaggedTrue();
}
