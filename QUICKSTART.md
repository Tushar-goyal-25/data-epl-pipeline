# Quick Start Guide

Get the Financial Data Processing Engine running in 5 minutes!

## Prerequisites Check

```bash
# Check Java (need 17+)
java -version

# Check Maven
mvn -version

# Check Python (for data generation)
python3 --version

# Check Docker (optional, for PostgreSQL)
docker --version
```

## Option 1: Run Without Database (Fastest)

```bash
# 1. Generate test data
cd scripts
python3 generate_data.py
cd ..

# 2. Build project
mvn clean package -DskipTests

# 3. Run
java -jar target/financial-data-engine-1.0.0.jar
```

## Option 2: Run With Database

```bash
# 1. Start PostgreSQL
cd config
docker-compose up -d
cd ..

# 2. Wait for database to be ready (10 seconds)
sleep 10

# 3. Create schema
docker exec -i financial-data-postgres psql -U postgres -d financialdb < config/database.sql

# 4. Generate test data
cd scripts
python3 generate_data.py
cd ..

# 5. Build and run
mvn clean package -DskipTests
java -jar target/financial-data-engine-1.0.0.jar
```

## Option 3: Use the Run Script

```bash
./scripts/run.sh
```

## Expected Output

You should see:
```
========================================
Financial Data Processing Engine v1.0.0
========================================

[STEP 1] Loading data from CSV...
Loaded 102,000 transactions

[STEP 2] Processing transactions...
Validation complete: 95,000 valid out of 100,000 in 245 ms

[STEP 3] Aggregating results...
Aggregation complete in 123 ms

[STEP 4] Generating reports...

========================================
PROCESSING COMPLETE
========================================
Total records processed: 100,000
Valid records: 95,000
Invalid records: 5,000
Total execution time: 2847 ms
Throughput: 35,123.45 records/second
========================================
```

## What Gets Created

After running, check these files:
```
data/processed/
├── processed_transactions.csv   # All processed transactions
└── summary_report.txt            # Summary statistics

logs/
└── application.log               # Detailed logs
```

## Troubleshooting

### "Java version too old"
Update Java to version 17 or higher

### "data file not found"
Run the data generator: `python3 scripts/generate_data.py`

### "Out of memory"
Increase heap size: `java -Xmx2g -jar ...`

### "Port 5432 already in use"
Another PostgreSQL is running. Stop it or change the port in docker-compose.yml

## Next Steps

1. **Read the README.md** - Full documentation
2. **Check TUTORIAL.md** - Learning guide with interview prep
3. **Examine the code** - Start with Main.java
4. **Try modifications** - Add your own features
5. **Run tests** - `mvn test`

## Performance Tuning

To process even faster:

```bash
# Use more threads (for multi-core CPUs)
# Edit application.properties:
thread.pool.size=8

# Increase batch size
batch.size=5000

# Increase heap size
java -Xmx4g -XX:+UseG1GC -jar target/financial-data-engine-1.0.0.jar
```

## Common Commands

```bash
# Clean build
mvn clean

# Build without tests
mvn package -DskipTests

# Run tests only
mvn test

# Run with specific properties
mvn exec:java -Dexec.mainClass="com.tushar.finance.Main"

# Generate code coverage report
mvn test jacoco:report
# Then open: target/site/jacoco/index.html

# Check dependencies
mvn dependency:tree

# Update dependencies
mvn versions:display-dependency-updates
```

## Project Structure at a Glance

```
financial-data-engine/
├── src/main/java/           # Java source code
│   └── com/tushar/finance/
│       ├── models/          # Data models
│       ├── loaders/         # Data loaders (CSV, JSON)
│       ├── processors/      # Business logic
│       ├── utils/           # Utilities
│       └── Main.java        # Entry point
├── src/main/resources/      # Configuration files
├── data/                    # Input/output data
├── config/                  # Database & Docker config
├── scripts/                 # Helper scripts
└── pom.xml                  # Maven configuration
```

## Getting Help

1. Check logs: `tail -f logs/application.log`
2. Enable debug logging in `src/main/resources/log4j2.xml`
3. Review README.md for detailed documentation
4. Check TUTORIAL.md for learning resources

Happy coding! 🚀
