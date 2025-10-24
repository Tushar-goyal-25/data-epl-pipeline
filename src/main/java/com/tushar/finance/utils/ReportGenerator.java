package com.tushar.finance.utils;

import com.tushar.finance.models.Transaction;
import com.tushar.finance.processors.TransactionProcessor.AggregationResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating various types of reports from processed data.
 */
public class ReportGenerator {
    private static final Logger logger = LogManager.getLogger(ReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generate a console-friendly summary report.
     */
    public void generateConsoleReport(AggregationResult result) {
        logger.info("\n" + "=".repeat(60));
        logger.info("TRANSACTION SUMMARY REPORT");
        logger.info("=".repeat(60));
        logger.info("Total Records: {}", result.getTotalRecords());
        logger.info("Valid Records: {} ({:.2f}%)", 
                result.getValidRecords(), 
                (result.getValidRecords() * 100.0) / result.getTotalRecords());
        logger.info("Invalid Records: {}", 
                result.getTotalRecords() - result.getValidRecords());
        logger.info("-".repeat(60));
        logger.info("Total Amount (USD): ${}", result.getTotalAmount());
        logger.info("Average Amount (USD): ${}", result.getAverageAmount());
        logger.info("-".repeat(60));
        logger.info("Transactions by Type:");
        result.getCountByType().forEach((type, count) -> 
                logger.info("  {}: {}", type, count));
        logger.info("-".repeat(60));
        logger.info("Top 10 Accounts by Volume:");
        result.getAmountByAccount().entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> logger.info("  {}: ${}", 
                        entry.getKey(), entry.getValue()));
        logger.info("=".repeat(60));
    }
    
    /**
     * Export processed transactions to CSV format.
     */
    public void generateCSVReport(List<Transaction> transactions, String outputPath) 
            throws IOException {
        logger.info("Generating CSV report: {}", outputPath);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("transaction_id", "account_id", "timestamp", 
                             "amount", "currency", "type", "category", 
                             "description", "status", "converted_amount_usd", 
                             "validated", "validation_message"))) {
            
            for (Transaction t : transactions) {
                csvPrinter.printRecord(
                        t.getTransactionId(),
                        t.getAccountId(),
                        t.getTimestamp().format(DATE_FORMATTER),
                        t.getAmount(),
                        t.getCurrency(),
                        t.getType(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getStatus(),
                        t.getConvertedAmount(),
                        t.isValidated(),
                        t.getValidationMessage()
                );
            }
        }
        
        logger.info("CSV report generated successfully with {} records", 
                transactions.size());
    }
    
    /**
     * Generate a text-based summary report file.
     */
    public void generateSummaryReport(AggregationResult result, String outputPath) 
            throws IOException {
        logger.info("Generating summary report: {}", outputPath);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("FINANCIAL DATA PROCESSING ENGINE\n");
            writer.write("Summary Report\n");
            writer.write("=".repeat(60) + "\n\n");
            
            writer.write("OVERVIEW\n");
            writer.write("-".repeat(60) + "\n");
            writer.write(String.format("Total Records: %d\n", result.getTotalRecords()));
            writer.write(String.format("Valid Records: %d (%.2f%%)\n", 
                    result.getValidRecords(),
                    (result.getValidRecords() * 100.0) / result.getTotalRecords()));
            writer.write(String.format("Invalid Records: %d\n\n", 
                    result.getTotalRecords() - result.getValidRecords()));
            
            writer.write("FINANCIAL METRICS\n");
            writer.write("-".repeat(60) + "\n");
            writer.write(String.format("Total Transaction Volume (USD): $%s\n", 
                    result.getTotalAmount()));
            writer.write(String.format("Average Transaction Size (USD): $%s\n\n", 
                    result.getAverageAmount()));
            
            writer.write("DISTRIBUTION BY TRANSACTION TYPE\n");
            writer.write("-".repeat(60) + "\n");
            result.getCountByType().forEach((type, count) -> {
                try {
                    writer.write(String.format("%s: %d\n", type, count));
                } catch (IOException e) {
                    logger.error("Error writing type distribution", e);
                }
            });
            
            writer.write("\n" + "=".repeat(60) + "\n");
        }
        
        logger.info("Summary report generated successfully");
    }
}
