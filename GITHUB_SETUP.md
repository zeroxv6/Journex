# GitHub Setup Guide - Complete Step-by-Step

## What Gets Built Automatically

| Platform | Format | Size | Notes |
|----------|--------|------|-------|
| Android | APK | ~50MB | Unsigned, ready to install |
| Linux | AppImage | ~100MB | Universal, runs on any distro |
| Windows | EXE | ~100MB | Portable executable |

## Prerequisites

Before starting, make sure you have:
- A GitHub account
- Git installed on your system
- Your project ready to push

---

## STEP 1: Remove Comments (Optional)

If you want to clean up your code before pushing:

```bash
chmod +x remove-comments.sh
./remove-comments.sh
```

This removes all single-line and multi-line comments while preserving URLs.

---

## STEP 2: Protect Sensitive Files

### Check what's being tracked:
```bash
git status
```

### If you see `google-services.json` or `credentials.json`, remove them:
```bash
git rm --cached app/google-services.json
git rm --cached desktop/src/jvmMain/resources/credentials.json
```

These files are now in `.gitignore` and won't be committed.

---

## STEP 3: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `journex` (or whatever you prefer)
3. Choose **Public** (free unlimited Actions) or **Private**
4. **DO NOT** initialize with README (you already have files)
5. Click **Create repository**

---

## STEP 4: Set Up GitHub Secret

This is the MOST IMPORTANT step for Android builds to work.

### Get your google-services.json content:
```bash
cat app/google-services.json
```

Copy the ENTIRE output (including the curly braces).

### Add it to GitHub:
1. Go to your repository on GitHub
2. Click **Settings** (top menu)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret** (green button)
5. Name: `GOOGLE_SERVICES_JSON`
6. Value: Paste the entire JSON content you copied
7. Click **Add secret**

**Screenshot guide:**
```
Settings → Secrets and variables → Actions → New repository secret
Name: GOOGLE_SERVICES_JSON
Value: { "project_info": { ... } }  ← paste entire JSON here
```

---

## STEP 5: Push Your Code to GitHub

### If this is your first time with this project:
```bash
git init
git add .
git commit -m "Initial commit with CI/CD workflow"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/journex.git
git push -u origin main
```

### If you already have a git repo:
```bash
git add .
git commit -m "Add CI/CD workflow for automated builds"
git push origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

---

## STEP 6: Trigger the Build

### Method A: Create a Release Tag (Recommended)
```bash
git tag v1.0.0
git push origin v1.0.0
```

This will:
- Trigger the automated build workflow
- Create builds for Android, Linux, and Windows
- Create a GitHub Release with all files attached

### Method B: Manual Trigger
1. Go to your repository on GitHub
2. Click **Actions** tab
3. Click **Build Multi-Platform Release** workflow
4. Click **Run workflow** button
5. Click the green **Run workflow** button

---

## STEP 7: Monitor the Build

1. Go to **Actions** tab in your repository
2. You'll see a workflow running (yellow dot = in progress)
3. Click on it to see detailed logs
4. Wait 10-15 minutes for all builds to complete

**Build times:**
- Android APK: ~5 minutes
- Linux AppImage: ~8 minutes
- Windows EXE: ~7 minutes

---

## STEP 8: Download Your Built Apps

### If you used a tag (v1.0.0):
1. Go to **Releases** tab
2. Click on your release (v1.0.0)
3. Download files from **Assets** section:
   - `app-release.apk` - Android app
   - `Journex-x86_64.AppImage` - Linux app
   - `Journex.exe` - Windows app

### If you manually triggered:
1. Go to **Actions** tab
2. Click on the completed workflow
3. Scroll down to **Artifacts** section
4. Download:
   - `android-apk`
   - `linux-appimage`
   - `windows-exe`

---

## STEP 9: Test Your Apps

### Android:
```bash
adb install app-release.apk
```
Or transfer to your phone and install.

### Linux:
```bash
chmod +x Journex-x86_64.AppImage
./Journex-x86_64.AppImage
```

### Windows:
Just double-click `Journex.exe`

---

## Future Releases

Every time you want to release a new version:

```bash
git add .
git commit -m "Your changes"
git push origin main

git tag v1.0.1
git push origin v1.0.1
```

The builds will automatically trigger and create a new release.

---

## Troubleshooting

### ❌ Build fails with "google-services.json not found"
**Solution:** You didn't add the `GOOGLE_SERVICES_JSON` secret. Go back to Step 4.

### ❌ "Permission denied" when pushing
**Solution:** Set up SSH keys or use personal access token:
```bash
git remote set-url origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/journex.git
```

### ❌ AppImage build fails
**Solution:** Check the Actions logs. Usually it's a path issue. The workflow will retry automatically.

### ❌ Windows EXE not created
**Solution:** Make sure your `desktop/build.gradle.kts` has `TargetFormat.Exe` configured.

### ✅ Build succeeds but app crashes
**Solution:** This is a code issue, not a build issue. Check your app logs.

---

## Important Notes

### What's NOT committed to GitHub:
- ✅ `google-services.json` (injected from secrets)
- ✅ `credentials.json` (not needed for basic builds)
- ✅ Keystore files (for signed APKs)
- ✅ Local build folders

### What IS committed:
- ✅ All source code
- ✅ Build scripts
- ✅ GitHub Actions workflow
- ✅ Resources and assets

### Cost:
- **Public repos:** FREE unlimited
- **Private repos:** 2,000 minutes/month free (enough for ~100 builds)

---

## Quick Reference Commands

```bash
# Remove sensitive files from git
git rm --cached app/google-services.json

# Push code
git add .
git commit -m "message"
git push origin main

# Create release
git tag v1.0.0
git push origin v1.0.0

# Check build status
# Go to: https://github.com/YOUR_USERNAME/journex/actions
```

---

## Need Help?

1. Check the **Actions** tab for detailed error logs
2. Make sure `GOOGLE_SERVICES_JSON` secret is set correctly
3. Verify your code builds locally first: `./gradlew build`

That's it! Your automated build pipeline is ready. 🚀
