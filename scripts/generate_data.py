#!/usr/bin/env python3
"""
Generate realistic financial transaction data for testing.
Creates 100,000+ transactions with various scenarios including:
- Multiple account types
- Different transaction types
- Various currencies
- Some invalid/edge case data for validation testing
"""

import csv
import random
from datetime import datetime, timedelta
from decimal import Decimal

# Configuration
NUM_TRANSACTIONS = 100000
OUTPUT_FILE = '../data/raw/transactions.csv'

# Test data
ACCOUNT_IDS = [f'ACC{str(i).zfill(3)}' for i in range(1, 501)]  # 500 accounts
CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CNY']
TRANSACTION_TYPES = ['DEBIT', 'CREDIT', 'TRANSFER', 'FEE', 'DIVIDEND', 'INTEREST']
CATEGORIES = ['SALARY', 'PURCHASE', 'TRANSFER', 'INVESTMENT', 'FEE', 'DIVIDEND', 
              'INTEREST', 'REFUND', 'PAYMENT', 'WITHDRAWAL']
STATUSES = ['COMPLETED', 'PENDING', 'FAILED', 'REVERSED']
DESCRIPTIONS = [
    'Regular monthly payment',
    'Online purchase',
    'ATM withdrawal',
    'Wire transfer',
    'Direct deposit',
    'Card payment',
    'Investment transaction',
    'Fee charge',
    'Interest payment',
    'Dividend payment'
]

def generate_transaction_id(index):
    """Generate unique transaction ID."""
    timestamp = datetime.now().strftime('%Y%m%d')
    return f'TXN{timestamp}{str(index).zfill(8)}'

def generate_timestamp(base_date, days_back):
    """Generate random timestamp within specified days."""
    delta = timedelta(days=random.randint(0, days_back),
                     hours=random.randint(0, 23),
                     minutes=random.randint(0, 59),
                     seconds=random.randint(0, 59))
    return (base_date - delta).strftime('%Y-%m-%d %H:%M:%S')

def generate_amount():
    """Generate transaction amount with realistic distribution."""
    # 70% small transactions, 20% medium, 10% large
    rand = random.random()
    if rand < 0.7:
        return round(random.uniform(10, 1000), 2)
    elif rand < 0.9:
        return round(random.uniform(1000, 10000), 2)
    else:
        return round(random.uniform(10000, 100000), 2)

def should_create_invalid_record():
    """Randomly determine if record should be invalid (5% chance)."""
    return random.random() < 0.05

def create_invalid_transaction(index, base_date):
    """Create intentionally invalid transaction for testing."""
    invalid_type = random.choice(['missing_id', 'negative_amount', 'future_date', 
                                  'invalid_currency', 'missing_account'])
    
    if invalid_type == 'missing_id':
        return {
            'transaction_id': '',
            'account_id': random.choice(ACCOUNT_IDS),
            'timestamp': generate_timestamp(base_date, 90),
            'amount': generate_amount(),
            'currency': random.choice(CURRENCIES),
            'type': random.choice(TRANSACTION_TYPES),
            'category': random.choice(CATEGORIES),
            'description': random.choice(DESCRIPTIONS),
            'status': random.choice(STATUSES)
        }
    elif invalid_type == 'negative_amount':
        return {
            'transaction_id': generate_transaction_id(index),
            'account_id': random.choice(ACCOUNT_IDS),
            'timestamp': generate_timestamp(base_date, 90),
            'amount': -generate_amount(),
            'currency': random.choice(CURRENCIES),
            'type': random.choice(TRANSACTION_TYPES),
            'category': random.choice(CATEGORIES),
            'description': random.choice(DESCRIPTIONS),
            'status': random.choice(STATUSES)
        }
    elif invalid_type == 'future_date':
        future_date = base_date + timedelta(days=random.randint(1, 30))
        return {
            'transaction_id': generate_transaction_id(index),
            'account_id': random.choice(ACCOUNT_IDS),
            'timestamp': future_date.strftime('%Y-%m-%d %H:%M:%S'),
            'amount': generate_amount(),
            'currency': random.choice(CURRENCIES),
            'type': random.choice(TRANSACTION_TYPES),
            'category': random.choice(CATEGORIES),
            'description': random.choice(DESCRIPTIONS),
            'status': random.choice(STATUSES)
        }
    elif invalid_type == 'invalid_currency':
        return {
            'transaction_id': generate_transaction_id(index),
            'account_id': random.choice(ACCOUNT_IDS),
            'timestamp': generate_timestamp(base_date, 90),
            'amount': generate_amount(),
            'currency': 'XXX',  # Invalid currency
            'type': random.choice(TRANSACTION_TYPES),
            'category': random.choice(CATEGORIES),
            'description': random.choice(DESCRIPTIONS),
            'status': random.choice(STATUSES)
        }
    else:  # missing_account
        return {
            'transaction_id': generate_transaction_id(index),
            'account_id': '',
            'timestamp': generate_timestamp(base_date, 90),
            'amount': generate_amount(),
            'currency': random.choice(CURRENCIES),
            'type': random.choice(TRANSACTION_TYPES),
            'category': random.choice(CATEGORIES),
            'description': random.choice(DESCRIPTIONS),
            'status': random.choice(STATUSES)
        }

def create_valid_transaction(index, base_date):
    """Create valid transaction."""
    return {
        'transaction_id': generate_transaction_id(index),
        'account_id': random.choice(ACCOUNT_IDS),
        'timestamp': generate_timestamp(base_date, 90),
        'amount': generate_amount(),
        'currency': random.choice(CURRENCIES),
        'type': random.choice(TRANSACTION_TYPES),
        'category': random.choice(CATEGORIES),
        'description': random.choice(DESCRIPTIONS),
        'status': random.choice(STATUSES)
    }

def generate_transactions():
    """Generate all transactions and write to CSV."""
    print(f'Generating {NUM_TRANSACTIONS:,} transactions...')
    
    base_date = datetime.now()
    transactions = []
    
    for i in range(1, NUM_TRANSACTIONS + 1):
        if should_create_invalid_record():
            transaction = create_invalid_transaction(i, base_date)
        else:
            transaction = create_valid_transaction(i, base_date)
        
        transactions.append(transaction)
        
        if i % 10000 == 0:
            print(f'Generated {i:,} transactions...')
    
    # Add some duplicate transactions (2% duplication rate)
    num_duplicates = int(NUM_TRANSACTIONS * 0.02)
    for _ in range(num_duplicates):
        duplicate = random.choice(transactions).copy()
        transactions.append(duplicate)
    
    print(f'Total transactions including duplicates: {len(transactions):,}')
    
    # Write to CSV
    print(f'Writing to {OUTPUT_FILE}...')
    with open(OUTPUT_FILE, 'w', newline='', encoding='utf-8') as f:
        fieldnames = ['transaction_id', 'account_id', 'timestamp', 'amount', 
                     'currency', 'type', 'category', 'description', 'status']
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        
        writer.writeheader()
        writer.writerows(transactions)
    
    print(f'Successfully generated {len(transactions):,} transactions!')
    print(f'File size: {os.path.getsize(OUTPUT_FILE) / (1024*1024):.2f} MB')
    
    # Print statistics
    print('\nStatistics:')
    print(f'  Accounts: {len(set(t["account_id"] for t in transactions if t["account_id"]))}')
    print(f'  Currencies: {len(set(t["currency"] for t in transactions))}')
    print(f'  Transaction types: {len(set(t["type"] for t in transactions))}')
    print(f'  Estimated invalid records: ~{int(NUM_TRANSACTIONS * 0.05):,}')
    print(f'  Duplicate transactions: {num_duplicates:,}')

if __name__ == '__main__':
    import os
    
    # Create output directory if it doesn't exist
    os.makedirs('../data/raw', exist_ok=True)
    
    print('=' * 60)
    print('Financial Data Generator')
    print('=' * 60)
    
    generate_transactions()
    
    print('\nDone!')
