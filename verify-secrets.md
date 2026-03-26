# Verify GitHub Secrets Setup

## Check Your google-services.json

Run this command locally to see what should be in the secret:

```bash
cat app/google-services.json
```

The output should look like this:

```json
{
  "project_info": {
    "project_number": "...",
    "project_id": "...",
    "storage_bucket": "..."
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "...",
        "android_client_info": {
          "package_name": "space.zeroxv6.journex"
        }
      },
      ...
    }
  ]
}
```

**CRITICAL:** Make sure the `package_name` is `space.zeroxv6.journex` (not the old `com.zeroxv6.journaling`)

## Update GitHub Secret

1. Go to your GitHub repository
2. **Settings** → **Secrets and variables** → **Actions**
3. Find `GOOGLE_SERVICES_JSON` secret
4. Click the **pencil icon** to edit
5. Delete the old value
6. Copy the ENTIRE content from `cat app/google-services.json`
7. Paste it (make sure no extra spaces or newlines)
8. Click **Update secret**

## Test the Android Build

```bash
# Push the new workflow
git add .github/workflows/build-android.yml verify-secrets.md
git commit -m "Add Android-only build workflow"
git push origin main

# Trigger Android build
git tag android-v1.0.0
git push origin android-v1.0.0
```

## Manual Test Locally

Before pushing, test locally:

```bash
# Clean build
./gradlew clean

# Build release APK
./gradlew :app:assembleRelease

# Check if APK is valid
ls -lh app/build/outputs/apk/release/

# Install and test
adb install app/build/outputs/apk/release/app-release.apk
```

If the local build works but GitHub Actions fails, the issue is with the secret.

## Common Issues

1. **Package name mismatch** - Make sure Firebase has `space.zeroxv6.journex`
2. **Invalid JSON** - Secret must be valid JSON (no extra quotes or escaping)
3. **Missing SHA-1** - Add your debug/release SHA-1 to Firebase Console
4. **Wrong secret name** - Must be exactly `GOOGLE_SERVICES_JSON`

## Workflow Triggers

- **Full build** (all platforms): `git tag v1.0.x && git push origin v1.0.x`
- **Android only**: `git tag android-v1.0.x && git push origin android-v1.0.x`
- **Manual trigger**: Go to Actions → Build Android APK → Run workflow
