package com.tushar.finance.processors;

import com.tushar.finance.models.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * High-performance transaction processor with validation, transformation,
 * and aggregation capabilities. Optimized for processing 100,000+ records.
 */
public class TransactionProcessor {
    private static final Logger logger = LogManager.getLogger(TransactionProcessor.class);
    
    // Currency conversion rates (simplified - in production, fetch from API)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();
    static {
        EXCHANGE_RATES.put("USD", BigDecimal.ONE);
        EXCHANGE_RATES.put("EUR", new BigDecimal("1.08"));
        EXCHANGE_RATES.put("GBP", new BigDecimal("1.27"));
        EXCHANGE_RATES.put("JPY", new BigDecimal("0.0067"));
        EXCHANGE_RATES.put("CNY", new BigDecimal("0.14"));
    }
    
    private final int threadPoolSize;
    
    public TransactionProcessor() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public TransactionProcessor(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    
    /**
     * Validate transactions using parallel processing for optimal performance.
     */
    public List<Transaction> validate(List<Transaction> transactions) {
        logger.info("Validating {} transactions using {} threads", 
                transactions.size(), threadPoolSize);
        long startTime = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<Transaction>> futures = new ArrayList<>();
        
        for (Transaction transaction : transactions) {
            futures.add(executor.submit(() -> validateSingle(transaction)));
        }
        
        List<Transaction> validated = new ArrayList<>();
        for (Future<Transaction> future : futures) {
            try {
                validated.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Validation failed: {}", e.getMessage());
            }
        }
        
        executor.shutdown();
        
        long validCount = validated.stream().filter(Transaction::isValidated).count();
        long duration = System.currentTimeMillis() - startTime;
        
        logger.info("Validation complete: {} valid out of {} in {} ms", 
                validCount, transactions.size(), duration);
        
        return validated;
    }
    
    private Transaction validateSingle(Transaction transaction) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (transaction.getTransactionId() == null || 
                transaction.getTransactionId().trim().isEmpty()) {
            errors.add("Missing transaction ID");
        }
        
        if (transaction.getAccountId() == null || 
                transaction.getAccountId().trim().isEmpty()) {
            errors.add("Missing account ID");
        }
        
        // Validate amount
        if (transaction.getAmount() == null) {
            errors.add("Missing amount");
        } else if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Negative amount");
        } else if (transaction.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            errors.add("Amount exceeds maximum limit");
        }
        
        // Validate currency
        if (transaction.getCurrency() == null || 
                !EXCHANGE_RATES.containsKey(transaction.getCurrency())) {
            errors.add("Invalid or unsupported currency");
        }
        
        // Validate timestamp
        if (transaction.getTimestamp() == null) {
            errors.add("Missing timestamp");
        } else if (transaction.getTimestamp().isAfter(LocalDateTime.now())) {
            errors.add("Future timestamp not allowed");
        }
        
        boolean isValid = errors.isEmpty();
        transaction.setValidated(isValid);
        transaction.setValidationMessage(isValid ? "Valid" : String.join("; ", errors));
        
        return transaction;
    }
    
    /**
     * Transform transactions by converting currencies and enriching data.
     */
    public List<Transaction> transform(List<Transaction> transactions) {
        logger.info("Transforming {} transactions", transactions.size());
        long startTime = System.currentTimeMillis();
        
        List<Transaction> transformed = transactions.parallelStream()
                .map(this::transformSingle)
                .collect(Collectors.toList());
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Transformation complete in {} ms", duration);
        
        return transformed;
    }
    
    private Transaction transformSingle(Transaction transaction) {
        // Convert to base currency (USD)
        BigDecimal rate = EXCHANGE_RATES.getOrDefault(
                transaction.getCurrency(), BigDecimal.ONE);
        BigDecimal convertedAmount = transaction.getAmount().multiply(rate);
        transaction.setConvertedAmount(convertedAmount);
        
        return transaction;
    }
    
    /**
     * Aggregate transactions for analytical insights.
     */
    public AggregationResult aggregate(List<Transaction> transactions) {
        logger.info("Aggregating {} transactions", transactions.size());
        long startTime = System.currentTimeMillis();
        
        // Filter only valid transactions
        List<Transaction> validTransactions = transactions.stream()
                .filter(Transaction::isValidated)
                .collect(Collectors.toList());
        
        // Calculate total amount
        BigDecimal totalAmount = validTransactions.stream()
                .map(Transaction::getConvertedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate average amount
        BigDecimal averageAmount = validTransactions.isEmpty() ? 
                BigDecimal.ZERO : 
                totalAmount.divide(new BigDecimal(validTransactions.size()), 
                        2, BigDecimal.ROUND_HALF_UP);
        
        // Group by type
        Map<Transaction.TransactionType, Long> countByType = validTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType, 
                        Collectors.counting()));
        
        // Group by account
        Map<String, BigDecimal> amountByAccount = validTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getAccountId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getConvertedAmount,
                                BigDecimal::add)));
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Aggregation complete in {} ms", duration);
        
        return new AggregationResult(
                transactions.size(),
                validTransactions.size(),
                totalAmount,
                averageAmount,
                countByType,
                amountByAccount
        );
    }
    
    /**
     * Remove duplicate transactions based on transaction ID.
     */
    public List<Transaction> removeDuplicates(List<Transaction> transactions) {
        logger.info("Removing duplicates from {} transactions", transactions.size());
        
        // Use LinkedHashSet to maintain order while removing duplicates
        Set<String> seen = new HashSet<>();
        List<Transaction> unique = transactions.stream()
                .filter(t -> seen.add(t.getTransactionId()))
                .collect(Collectors.toList());
        
        logger.info("Removed {} duplicate transactions", 
                transactions.size() - unique.size());
        
        return unique;
    }
    
    /**
     * Result object for aggregation operations.
     */
    public static class AggregationResult {
        private final int totalRecords;
        private final int validRecords;
        private final BigDecimal totalAmount;
        private final BigDecimal averageAmount;
        private final Map<Transaction.TransactionType, Long> countByType;
        private final Map<String, BigDecimal> amountByAccount;
        
        public AggregationResult(int totalRecords, int validRecords,
                                BigDecimal totalAmount, BigDecimal averageAmount,
                                Map<Transaction.TransactionType, Long> countByType,
                                Map<String, BigDecimal> amountByAccount) {
            this.totalRecords = totalRecords;
            this.validRecords = validRecords;
            this.totalAmount = totalAmount;
            this.averageAmount = averageAmount;
            this.countByType = countByType;
            this.amountByAccount = amountByAccount;
        }
        
        public int getTotalRecords() { return totalRecords; }
        public int getValidRecords() { return validRecords; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getAverageAmount() { return averageAmount; }
        public Map<Transaction.TransactionType, Long> getCountByType() { return countByType; }
        public Map<String, BigDecimal> getAmountByAccount() { return amountByAccount; }
        
        @Override
        public String toString() {
            return String.format(
                    "AggregationResult{total=%d, valid=%d, totalAmount=%s, avgAmount=%s}",
                    totalRecords, validRecords, totalAmount, averageAmount);
        }
    }
}
