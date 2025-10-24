package com.tushar.finance.loaders;

import java.io.IOException;
import java.util.List;

/**
 * Generic interface for data loading operations.
 * Supports both full and batch loading patterns.
 */
public interface DataLoader<T> {
    
    /**
     * Load all records from the specified data source.
     * 
     * @param source Path or identifier of the data source
     * @return List of loaded objects
     * @throws IOException if loading fails
     */
    List<T> load(String source) throws IOException;
    
    /**
     * Load a batch of records with pagination support.
     * 
     * @param source Path or identifier of the data source
     * @param offset Starting position
     * @param limit Maximum number of records to load
     * @return List of loaded objects
     * @throws IOException if loading fails
     */
    List<T> loadBatch(String source, int offset, int limit) throws IOException;
}
