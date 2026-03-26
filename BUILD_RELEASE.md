# Building Release Packages

This guide explains how to build release packages for Windows, Linux (Debian/Arch), and AppImage from Arch Linux.

## Prerequisites

### Install Required Tools

```bash
# Update system
sudo pacman -Syu

# Install JDK (if not already installed)
sudo pacman -S jdk-openjdk

# Install tools for cross-platform builds
sudo pacman -S base-devel fakeroot dpkg rpm-tools

# Install AppImage tools
yay -S appimagetool  # or install from AUR manually

# For Windows builds (optional, for signing)
sudo pacman -S osslsigncode
```

## Desktop App Release Builds

### Quick Fix for Java 21 / ProGuard Issue

If you get ProGuard errors with Java 21, use these commands instead:

```bash
# Build distributable (no packaging, just runnable app)
./gradlew :desktop:createDistributable

# Output: desktop/build/compose/binaries/main/app/
# This creates a folder with the app and all dependencies
# You can zip this folder for distribution
```

### 1. Linux Package (for Arch)

```bash
# Build native package for Arch Linux
./gradlew :desktop:packageReleaseDistributionForCurrentOS

# Output location:
# desktop/build/compose/binaries/main-release/app/
```

This creates a `.tar.gz` archive with the application.

### 2. Debian Package (.deb)

```bash
# Build Debian package
./gradlew :desktop:packageReleaseDeb

# Output location:
# desktop/build/compose/binaries/main-release/deb/
```

### 3. RPM Package (for Fedora/RHEL)

```bash
# Build RPM package
./gradlew :desktop:packageReleaseRpm

# Output location:
# desktop/build/compose/binaries/main-release/rpm/
```

### 4. Windows Executable (.exe)

```bash
# Build Windows executable (cross-compile from Linux)
./gradlew :desktop:packageReleaseMsi

# Output location:
# desktop/build/compose/binaries/main-release/msi/
```

**Note:** For Windows builds from Linux, you need to configure the build properly. See configuration below.

### 5. AppImage (Universal Linux)

AppImage is the most portable format for Linux. Here's how to create one:

#### Step 1: Build the application

```bash
./gradlew :desktop:createRuntimeImage
```

#### Step 2: Create AppImage structure

```bash
# Create AppDir structure
mkdir -p AppDir/usr/bin
mkdir -p AppDir/usr/share/applications
mkdir -p AppDir/usr/share/icons/hicolor/256x256/apps

# Copy the runtime image
cp -r desktop/build/compose/binaries/main-release/app/* AppDir/usr/bin/

# Create desktop entry
cat > AppDir/usr/share/applications/journex.desktop << 'EOF'
[Desktop Entry]
Type=Application
Name=Journex
Comment=Journaling Application
Exec=journex
Icon=journex
Categories=Office;Utility;
Terminal=false
EOF

# Copy icon (if you have one)
cp app/src/main/res/drawable/journex.png AppDir/usr/share/icons/hicolor/256x256/apps/journex.png

# Create AppRun script
cat > AppDir/AppRun << 'EOF'
#!/bin/bash
SELF=$(readlink -f "$0")
HERE=${SELF%/*}
export PATH="${HERE}/usr/bin:${PATH}"
export LD_LIBRARY_PATH="${HERE}/usr/lib:${LD_LIBRARY_PATH}"
cd "${HERE}/usr/bin"
exec ./journex "$@"
EOF

chmod +x AppDir/AppRun

# Build AppImage
appimagetool AppDir Journex-x86_64.AppImage
```

## Android App Release Build

### Build Signed APK

```bash
# Generate keystore (first time only)
keytool -genkey -v -keystore journex-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias journex

# Create keystore.properties file
cat > keystore.properties << EOF
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=journex
storeFile=../journex-release-key.jks
EOF

# Build release APK
./gradlew :app:assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### Build Android App Bundle (for Play Store)

```bash
./gradlew :app:bundleRelease

# Output location:
# app/build/outputs/bundle/release/app-release.aab
```

## Configuration for Cross-Platform Builds

### Update desktop/build.gradle.kts

Add this configuration for better packaging:

```kotlin
compose.desktop {
    application {
        mainClass = "space.zeroxv6.journex.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,      // macOS
                TargetFormat.Msi,      // Windows
                TargetFormat.Deb,      // Debian/Ubuntu
                TargetFormat.Rpm       // Fedora/RHEL
            )
            
            packageName = "Journex"
            packageVersion = "1.0.0"
            description = "A beautiful journaling application"
            copyright = "© 2024 Your Name. All rights reserved."
            vendor = "Your Company"
            
            linux {
                iconFile.set(project.file("src/jvmMain/resources/journex_icon.png"))
                packageName = "journex"
                debMaintainer = "your.email@example.com"
                menuGroup = "Office"
                appCategory = "Office"
            }
            
            windows {
                iconFile.set(project.file("src/jvmMain/resources/journex_icon.ico"))
                menuGroup = "Journex"
                upgradeUuid = "GENERATE-UUID-HERE"
            }
            
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/journex_icon.icns"))
                bundleID = "space.zeroxv6.journex"
            }
        }
    }
}
```

## Build All Packages at Once

Create a build script:

```bash
#!/bin/bash
# build-all.sh

echo "Building all release packages..."

# Desktop packages
echo "Building Linux packages..."
./gradlew :desktop:packageReleaseDeb
./gradlew :desktop:packageReleaseRpm
./gradlew :desktop:packageReleaseDistributionForCurrentOS

echo "Building Windows package..."
./gradlew :desktop:packageReleaseMsi

# Android
echo "Building Android APK..."
./gradlew :app:assembleRelease

echo "Build complete!"
echo "Outputs:"
echo "  Desktop (Deb): desktop/build/compose/binaries/main-release/deb/"
echo "  Desktop (RPM): desktop/build/compose/binaries/main-release/rpm/"
echo "  Desktop (Windows): desktop/build/compose/binaries/main-release/msi/"
echo "  Android: app/build/outputs/apk/release/"
```

Make it executable:
```bash
chmod +x build-all.sh
./build-all.sh
```

## Icon Preparation

You need different icon formats for different platforms:

### Convert PNG to ICO (Windows)

```bash
# Install ImageMagick
sudo pacman -S imagemagick

# Convert
convert journex.png -define icon:auto-resize=256,128,64,48,32,16 journex.ico
```

### Convert PNG to ICNS (macOS)

```bash
# Create iconset directory
mkdir journex.iconset

# Create different sizes
for size in 16 32 64 128 256 512; do
    convert journex.png -resize ${size}x${size} journex.iconset/icon_${size}x${size}.png
done

# Convert to icns (requires macOS or icnsutils)
# On Linux:
sudo pacman -S libicns
png2icns journex.icns journex.iconset/*.png
```

## Testing Packages

### Test Debian Package
```bash
sudo dpkg -i desktop/build/compose/binaries/main-release/deb/*.deb
journex
```

### Test RPM Package
```bash
sudo rpm -i desktop/build/compose/binaries/main-release/rpm/*.rpm
journex
```

### Test AppImage
```bash
chmod +x Journex-x86_64.AppImage
./Journex-x86_64.AppImage
```

### Test Android APK
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## Distribution

### Upload to GitHub Releases

```bash
# Install GitHub CLI
sudo pacman -S github-cli

# Authenticate
gh auth login

# Create release
gh release create v1.0.0 \
    desktop/build/compose/binaries/main-release/deb/*.deb \
    desktop/build/compose/binaries/main-release/rpm/*.rpm \
    desktop/build/compose/binaries/main-release/msi/*.msi \
    Journex-x86_64.AppImage \
    app/build/outputs/apk/release/app-release.apk \
    --title "Journex v1.0.0" \
    --notes "Release notes here"
```

## Troubleshooting

### Windows build fails on Linux
- Windows MSI builds require WiX toolset, which is Windows-only
- Consider building Windows packages on a Windows machine or CI/CD
- Alternative: Use GitHub Actions with Windows runner

### AppImage doesn't run
- Make sure AppRun is executable: `chmod +x AppDir/AppRun`
- Check that all dependencies are included
- Test on a clean system or Docker container

### Android signing fails
- Verify keystore.properties path is correct
- Ensure keystore password is correct
- Check that keystore file exists

## CI/CD with GitHub Actions

Create `.github/workflows/release.yml`:

```yaml
name: Build Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build-desktop:
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Build packages
        run: ./gradlew :desktop:packageReleaseDistributionForCurrentOS
      
      - name: Upload artifacts
        uses: actions/upload-artifact@v3
        with:
          name: desktop-${{ matrix.os }}
          path: desktop/build/compose/binaries/main-release/

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Build APK
        run: ./gradlew :app:assembleRelease
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: android-apk
          path: app/build/outputs/apk/release/
```

## Summary

**From Arch Linux, you can build:**
- ✅ Linux packages (Deb, RPM, tar.gz) - Native
- ✅ AppImage - Native
- ⚠️ Windows MSI - Limited (requires WiX)
- ❌ macOS DMG - Requires macOS

**Recommended approach:**
1. Build Linux packages natively on Arch
2. Use GitHub Actions for Windows and macOS builds
3. Create AppImage for universal Linux distribution
4. Build Android APK/AAB natively on Arch
