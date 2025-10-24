package com.tushar.finance.loaders;

import com.tushar.finance.models.Transaction;
import com.tushar.finance.models.Transaction.TransactionType;
import com.tushar.finance.models.Transaction.TransactionStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance CSV loader for financial data.
 * Uses buffered reading and streaming for optimal memory usage.
 */
public class CSVLoader implements DataLoader<Transaction> {
    private static final Logger logger = LogManager.getLogger(CSVLoader.class);
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final int batchSize;
    
    public CSVLoader() {
        this(1000); // Default batch size
    }
    
    public CSVLoader(int batchSize) {
        this.batchSize = batchSize;
    }
    
    @Override
    public List<Transaction> load(String filePath) throws IOException {
        logger.info("Loading transactions from CSV file: {}", filePath);
        long startTime = System.currentTimeMillis();
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            int recordCount = 0;
            for (CSVRecord record : csvParser) {
                try {
                    Transaction transaction = parseRecord(record);
                    transactions.add(transaction);
                    recordCount++;
                    
                    if (recordCount % batchSize == 0) {
                        logger.debug("Loaded {} records", recordCount);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse record at line {}: {}", 
                            record.getRecordNumber(), e.getMessage());
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Successfully loaded {} transactions in {} ms", 
                    transactions.size(), duration);
            logger.info("Throughput: {} records/sec", 
                    (transactions.size() * 1000) / Math.max(duration, 1));
        }
        
        return transactions;
    }
    
    private Transaction parseRecord(CSVRecord record) {
        return new Transaction.Builder()
                .transactionId(record.get("transaction_id"))
                .accountId(record.get("account_id"))
                .timestamp(LocalDateTime.parse(record.get("timestamp"), DATE_FORMATTER))
                .amount(new BigDecimal(record.get("amount")))
                .currency(record.get("currency"))
                .type(TransactionType.valueOf(record.get("type").toUpperCase()))
                .category(record.get("category"))
                .description(record.get("description"))
                .status(TransactionStatus.valueOf(
                        record.get("status").toUpperCase()))
                .build();
    }
    
    @Override
    public List<Transaction> loadBatch(String filePath, int offset, int limit) 
            throws IOException {
        logger.info("Loading batch from CSV: offset={}, limit={}", offset, limit);
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            int currentIndex = 0;
            for (CSVRecord record : csvParser) {
                if (currentIndex >= offset && transactions.size() < limit) {
                    try {
                        Transaction transaction = parseRecord(record);
                        transactions.add(transaction);
                    } catch (Exception e) {
                        logger.warn("Failed to parse record: {}", e.getMessage());
                    }
                }
                currentIndex++;
                
                if (transactions.size() >= limit) {
                    break;
                }
            }
        }
        
        logger.info("Loaded batch of {} transactions", transactions.size());
        return transactions;
    }
}
