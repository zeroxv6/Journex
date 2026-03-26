#!/bin/bash

echo "🔍 Verifying Kotlin Multiplatform Build Setup..."
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "📋 Checking Java version..."
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ "$java_version" > "11" ]] || [[ "$java_version" == "11"* ]]; then
    echo -e "${GREEN}✓${NC} Java version: $java_version (OK)"
else
    echo -e "${RED}✗${NC} Java version: $java_version (Need 11+)"
    exit 1
fi
echo ""

# Check Gradle
echo "📋 Checking Gradle..."
if ./gradlew --version > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Gradle is working"
else
    echo -e "${RED}✗${NC} Gradle is not working"
    exit 1
fi
echo ""

# Sync Gradle
echo "🔄 Syncing Gradle dependencies..."
if ./gradlew tasks > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Gradle sync successful"
else
    echo -e "${RED}✗${NC} Gradle sync failed"
    exit 1
fi
echo ""

# Check modules
echo "📦 Checking modules..."
modules=("app" "shared" "desktop")
for module in "${modules[@]}"; do
    if [ -d "$module" ]; then
        echo -e "${GREEN}✓${NC} Module '$module' exists"
    else
        echo -e "${RED}✗${NC} Module '$module' not found"
        exit 1
    fi
done
echo ""

# Try to build Android
echo "🤖 Building Android app..."
if ./gradlew :app:assembleDebug > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Android build successful"
else
    echo -e "${YELLOW}⚠${NC} Android build failed (may need Android SDK)"
fi
echo ""

# Try to build Desktop
echo "🖥️  Building Desktop app..."
if ./gradlew :desktop:jar > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Desktop build successful"
else
    echo -e "${RED}✗${NC} Desktop build failed"
    echo "Run './gradlew :desktop:jar --stacktrace' for details"
    exit 1
fi
echo ""

echo -e "${GREEN}✅ All checks passed!${NC}"
echo ""
echo "🚀 You can now run:"
echo "   Android: ./gradlew :app:installDebug"
echo "   Desktop: ./gradlew :desktop:run"
echo ""
