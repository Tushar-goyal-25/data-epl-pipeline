package com.tushar.finance;

import com.tushar.finance.loaders.CSVLoader;
import com.tushar.finance.models.Transaction;
import com.tushar.finance.processors.TransactionProcessor;
import com.tushar.finance.processors.TransactionProcessor.AggregationResult;
import com.tushar.finance.utils.ReportGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Main entry point for the Financial Data Processing Engine.
 * Demonstrates complete ETL pipeline with performance metrics.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Financial Data Processing Engine v1.0.0");
        logger.info("========================================");
        
        try {
            // Measure total execution time
            long overallStart = System.currentTimeMillis();
            
            // Step 1: Load Data
            logger.info("\n[STEP 1] Loading data from CSV...");
            CSVLoader loader = new CSVLoader(1000);
            String inputFile = "data/raw/transactions.csv";
            List<Transaction> transactions = loader.load(inputFile);
            
            logger.info("Loaded {} transactions", transactions.size());
            
            // Step 2: Process Data
            logger.info("\n[STEP 2] Processing transactions...");
            TransactionProcessor processor = new TransactionProcessor();
            
            // Remove duplicates
            transactions = processor.removeDuplicates(transactions);
            
            // Validate transactions
            List<Transaction> validated = processor.validate(transactions);
            
            // Transform transactions (currency conversion)
            List<Transaction> transformed = processor.transform(validated);
            
            // Step 3: Aggregate Results
            logger.info("\n[STEP 3] Aggregating results...");
            AggregationResult result = processor.aggregate(transformed);
            
            // Step 4: Generate Report
            logger.info("\n[STEP 4] Generating reports...");
            ReportGenerator reportGenerator = new ReportGenerator();
            reportGenerator.generateConsoleReport(result);
            reportGenerator.generateCSVReport(transformed, "data/processed/processed_transactions.csv");
            reportGenerator.generateSummaryReport(result, "data/processed/summary_report.txt");
            
            // Calculate performance metrics
            long overallDuration = System.currentTimeMillis() - overallStart;
            double throughput = (transactions.size() * 1000.0) / overallDuration;
            
            // Final summary
            logger.info("\n========================================");
            logger.info("PROCESSING COMPLETE");
            logger.info("========================================");
            logger.info("Total records processed: {}", transactions.size());
            logger.info("Valid records: {}", result.getValidRecords());
            logger.info("Invalid records: {}", result.getTotalRecords() - result.getValidRecords());
            logger.info("Total execution time: {} ms", overallDuration);
            logger.info("Throughput: {:.2f} records/second", throughput);
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("Fatal error during processing", e);
            System.exit(1);
        }
    }
}
