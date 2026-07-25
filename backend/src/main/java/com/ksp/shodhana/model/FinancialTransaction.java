package com.ksp.shodhana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a Financial Transaction entity linked to suspects.
 * Maps to "financial_transactions" table in PostgreSQL & Spring Data JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "linked_criminal_id")
    private Long linkedCriminalId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "from_account")
    private String fromAccount;

    @Column(name = "to_account")
    private String toAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "is_flagged")
    private Boolean isFlagged;

    @Column(name = "flag_reason", length = 1000)
    private String flagReason;
}
