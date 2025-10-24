-- Financial Data Processing Engine Database Schema

-- Drop tables if they exist
DROP TABLE IF EXISTS transaction_summary CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;

-- Accounts table
CREATE TABLE accounts (
    account_id VARCHAR(50) PRIMARY KEY,
    account_name VARCHAR(200),
    account_type VARCHAR(50),
    balance DECIMAL(18, 2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    account_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    converted_amount_usd DECIMAL(18, 2),
    validated BOOLEAN DEFAULT false,
    validation_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- Transaction summary table (for aggregations)
CREATE TABLE transaction_summary (
    id SERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    total_transactions INTEGER DEFAULT 0,
    valid_transactions INTEGER DEFAULT 0,
    total_amount_usd DECIMAL(18, 2) DEFAULT 0,
    average_amount_usd DECIMAL(18, 2) DEFAULT 0,
    debit_count INTEGER DEFAULT 0,
    credit_count INTEGER DEFAULT 0,
    transfer_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(summary_date)
);

-- Indexes for performance
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_validated ON transactions(validated);

-- Sample accounts for testing
INSERT INTO accounts (account_id, account_name, account_type, currency) VALUES
    ('ACC001', 'John Doe Trading Account', 'TRADING', 'USD'),
    ('ACC002', 'Jane Smith Investment Account', 'INVESTMENT', 'USD'),
    ('ACC003', 'ACME Corp Business Account', 'BUSINESS', 'USD'),
    ('ACC004', 'Global Trading Ltd', 'TRADING', 'GBP'),
    ('ACC005', 'Tech Startup Fund', 'INVESTMENT', 'EUR');

-- Function to update account balance
CREATE OR REPLACE FUNCTION update_account_balance()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE accounts 
        SET balance = balance + NEW.converted_amount_usd,
            updated_at = CURRENT_TIMESTAMP
        WHERE account_id = NEW.account_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update balance
CREATE TRIGGER trg_update_balance
AFTER INSERT ON transactions
FOR EACH ROW
EXECUTE FUNCTION update_account_balance();

-- View for transaction analytics
CREATE OR REPLACE VIEW vw_transaction_analytics AS
SELECT 
    DATE(timestamp) as transaction_date,
    COUNT(*) as total_transactions,
    SUM(CASE WHEN validated THEN 1 ELSE 0 END) as valid_transactions,
    SUM(converted_amount_usd) as total_amount_usd,
    AVG(converted_amount_usd) as avg_amount_usd,
    SUM(CASE WHEN type = 'DEBIT' THEN 1 ELSE 0 END) as debit_count,
    SUM(CASE WHEN type = 'CREDIT' THEN 1 ELSE 0 END) as credit_count,
    SUM(CASE WHEN type = 'TRANSFER' THEN 1 ELSE 0 END) as transfer_count
FROM transactions
GROUP BY DATE(timestamp)
ORDER BY transaction_date DESC;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
GRANT SELECT ON vw_transaction_analytics TO postgres;

-- Success message
SELECT 'Database schema created successfully!' as status;
