package com.tushar.finance.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a financial transaction in the system.
 * Optimized for high-performance processing with immutable design.
 */
public class Transaction {
    private final String transactionId;
    private final String accountId;
    private final LocalDateTime timestamp;
    private final BigDecimal amount;
    private final String currency;
    private final TransactionType type;
    private final String category;
    private final String description;
    private final TransactionStatus status;
    
    // Calculated fields
    private BigDecimal convertedAmount; // Amount in base currency
    private boolean validated;
    private String validationMessage;
    
    public enum TransactionType {
        DEBIT, CREDIT, TRANSFER, FEE, DIVIDEND, INTEREST
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, REVERSED
    }
    
    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.accountId = builder.accountId;
        this.timestamp = builder.timestamp;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.type = builder.type;
        this.category = builder.category;
        this.description = builder.description;
        this.status = builder.status;
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public boolean isValidated() { return validated; }
    public String getValidationMessage() { return validationMessage; }
    
    // Setters for mutable calculated fields
    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id='%s', account='%s', amount=%s %s, type=%s, timestamp=%s}",
                transactionId, accountId, amount, currency, type, timestamp);
    }
    
    // Builder Pattern for flexible object construction
    public static class Builder {
        private String transactionId;
        private String accountId;
        private LocalDateTime timestamp;
        private BigDecimal amount;
        private String currency;
        private TransactionType type;
        private String category;
        private String description;
        private TransactionStatus status = TransactionStatus.COMPLETED;
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(this);
        }
    }
}
