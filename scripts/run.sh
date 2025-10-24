#!/bin/bash

# Financial Data Processing Engine - Run Script

echo "========================================="
echo "Financial Data Processing Engine"
echo "========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required"
    echo "Current version: $JAVA_VERSION"
    exit 1
fi

echo "✓ Java version OK"

# Check if data file exists
if [ ! -f "data/raw/transactions.csv" ]; then
    echo ""
    echo "Warning: Test data not found"
    echo "Generating test data..."
    echo ""
    cd scripts
    python3 generate_data.py
    cd ..
fi

echo "✓ Test data ready"
echo ""

# Build if necessary
if [ ! -f "target/financial-data-engine-1.0.0.jar" ]; then
    echo "Building project..."
    mvn clean package -DskipTests
    echo ""
fi

echo "✓ Build complete"
echo ""

# Run the application
echo "Starting application..."
echo "========================================="
echo ""

java -Xmx2g -XX:+UseG1GC \
    -Dlog4j.configurationFile=src/main/resources/log4j2.xml \
    -jar target/financial-data-engine-1.0.0.jar

echo ""
echo "========================================="
echo "Application finished"
echo "========================================="
