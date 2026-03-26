#!/bin/bash

# Journex Desktop - Package Builder Script
# This script helps build distribution packages for different platforms

set -e

echo "╔════════════════════════════════════════╗"
echo "║   Journex Desktop Package Builder     ║"
echo "╔════════════════════════════════════════╗"
echo ""

# Detect OS
OS="unknown"
case "$(uname -s)" in
    Linux*)     OS="Linux";;
    Darwin*)    OS="macOS";;
    MINGW*|MSYS*|CYGWIN*)    OS="Windows";;
esac

echo "Detected OS: $OS"
echo ""

# Function to build packages
build_packages() {
    local format=$1
    echo "Building $format package..."
    ./gradlew :desktop:package${format}
    if [ $? -eq 0 ]; then
        echo "✓ $format package built successfully"
    else
        echo "✗ Failed to build $format package"
        return 1
    fi
    echo ""
}

# Main menu
echo "Select build option:"
echo "1) Build all packages for current OS"
echo "2) Build specific package format"
echo "3) Build Windows packages (EXE + MSI)"
echo "4) Build macOS packages (DMG + PKG)"
echo "5) Build Linux packages (DEB + RPM)"
echo "6) Just run the app (no packaging)"
echo ""
read -p "Enter choice [1-6]: " choice

case $choice in
    1)
        echo "Building all packages for $OS..."
        ./gradlew :desktop:packageDistributionForCurrentOS
        ;;
    2)
        echo ""
        echo "Available formats:"
        echo "  - Exe (Windows)"
        echo "  - Msi (Windows)"
        echo "  - Dmg (macOS)"
        echo "  - Pkg (macOS)"
        echo "  - Deb (Linux)"
        echo "  - Rpm (Linux)"
        echo ""
        read -p "Enter format name: " format
        build_packages "$format"
        ;;
    3)
        if [ "$OS" != "Windows" ]; then
            echo "⚠ Warning: Building Windows packages on $OS may require Wine"
        fi
        build_packages "Exe"
        build_packages "Msi"
        ;;
    4)
        if [ "$OS" != "macOS" ]; then
            echo "✗ Error: macOS packages can only be built on macOS"
            exit 1
        fi
        build_packages "Dmg"
        build_packages "Pkg"
        ;;
    5)
        build_packages "Deb"
        build_packages "Rpm"
        ;;
    6)
        echo "Running application..."
        ./gradlew :desktop:run
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "╔════════════════════════════════════════╗"
echo "║            Build Complete!             ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "Output location: desktop/build/compose/binaries/main/"
echo ""
echo "To install:"
case $OS in
    Linux)
        echo "  DEB: sudo apt install ./journex_1.0.0-1_amd64.deb"
        echo "  RPM: sudo dnf install journex-1.0.0-1.x86_64.rpm"
        ;;
    macOS)
        echo "  DMG: Open the .dmg file and drag to Applications"
        echo "  PKG: Double-click the .pkg file to install"
        ;;
    Windows)
        echo "  EXE/MSI: Double-click to run the installer"
        ;;
esac
echo ""
