import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        kotlin.srcDirs("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    
    // Shared module
    implementation(project(":shared"))
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    
    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Google Drive API for Desktop
    implementation("com.google.apis:google-api-services-drive:v3-rev20240914-2.0.0")
    implementation("com.google.api-client:google-api-client:2.7.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.http-client:google-http-client-gson:1.45.0")
}

compose.desktop {
    application {
        mainClass = "space.zeroxv6.journex.desktop.MainKt"
        
        jvmArgs += listOf(
            "--add-exports", "java.base/sun.net.www.protocol.http=ALL-UNNAMED",
            "--add-exports", "jdk.httpserver/com.sun.net.httpserver=ALL-UNNAMED",
            "--add-modules", "jdk.httpserver"
        )
        
        nativeDistributions {
            // All target formats: Windows (EXE, MSI), macOS (DMG, PKG), Linux (DEB, RPM)
            targetFormats(TargetFormat.AppImage,TargetFormat.Dmg, TargetFormat.Pkg, TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)

            packageName = "Journex"
            packageVersion = "1.0.0"
            
            description = "A beautiful journaling application with mood tracking, tasks, and more"
            vendor = "ZeroXV6"
            copyright = "© 2026 zeroxv6. All rights reserved."
            
            // Disable ProGuard (doesn't support Java 21)
            buildTypes.release.proguard {
                isEnabled.set(false)
            }
            
            // Windows configuration
            windows {
                menuGroup = "Journex"
                // Unique ID for Windows installer upgrades
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                // EXE specific
                perUserInstall = true
                dirChooser = true
                shortcut = true
            }

            
            // Linux configuration
            linux {
                packageName = "journex"
                debMaintainer = "zeroxv6.space@gmail.com"
                menuGroup = "Office"
                appCategory = "Office"
                // RPM specific
                rpmLicenseType = "MIT"
                // DEB specific
                debPackageVersion = "1.0.0"
            }
        }
    }
}

