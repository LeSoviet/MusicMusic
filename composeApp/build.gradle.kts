import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

version = "1.0.0"
group = "com.musicmusic"

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            
            // Kotlinx Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            
            // Kotlinx DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            
            // Ktor Client (para streaming de radios)
            implementation("io.ktor:ktor-client-core:2.3.8")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
            
            // SQLDelight
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            
            // Koin (Dependency Injection)
            implementation("io.insert-koin:koin-core:3.5.3")
            implementation("io.insert-koin:koin-compose:1.1.2")
            
            // DataStore Preferences
            implementation("androidx.datastore:datastore-preferences-core:1.0.0")
            
            // Immutable Collections (performance)
            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
        }
        
        desktopMain.dependencies {
            // Compose Desktop
            implementation(compose.desktop.currentOs)
            
            // Coroutines Swing (para Desktop)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            
            // Ktor Client CIO (para Desktop)
            implementation("io.ktor:ktor-client-cio:2.3.8")
            
            // SQLDelight Driver para Desktop
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            
            // VLCJ (Audio Player)
            implementation("uk.co.caprica:vlcj:4.8.2")
            
            // JAudioTagger (Metadata de audio)
            implementation("net.jthink:jaudiotagger:3.0.1")
            
            // SLF4J (Logging - requerido por algunas librerías)
            implementation("org.slf4j:slf4j-simple:2.0.9")
            
            // File System
            implementation("com.squareup.okio:okio:3.7.0")
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
            implementation("app.cash.turbine:turbine:1.0.0")
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.musicmusic.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
            
            // Generar schema desde archivos .sq directamente
            deriveSchemaFromMigrations.set(false)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.musicmusic.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            
            packageName = "MusicMusic"
            packageVersion = project.version.toString()
            description = "Modern music player for local libraries and online radios"
            vendor = "MusicMusic"
            
            windows {
                console = false
                dirChooser = true
                perUserInstall = true
                menuGroup = "MusicMusic"
                shortcut = true
                
                iconFile.set(project.file("src/desktopMain/resources/app_icon.ico"))
            }
            
            linux {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.png"))
                shortcut = true
                menuGroup = "Audio"
                packageName = "musicmusic"
                debMaintainer = "musicmusic@example.com"
                
                // Dependencias del sistema para Linux
                debPackageVersion = project.version.toString()
            }
            
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.icns"))
                bundleID = "com.musicmusic.app"
                dockName = "MusicMusic"
            }
        }
        
        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
            obfuscate.set(false)
        }
        
        jvmArgs += listOf(
            "-Xmx2G",
            "-Xms512M",
            "-XX:+UseG1GC"
        )
    }
}

// Tarea para copiar VLC libraries (necesarias para VLCJ)
tasks.register<Copy>("copyVlcLibs") {
    from("libs/vlc") {
        include("**/*")
    }
    into("${layout.buildDirectory.get()}/compose/binaries/main/app/MusicMusic/lib/vlc")
}

// Ejecutar copyVlcLibs antes de package (solo si la tarea existe)
tasks.matching { it.name == "packageDistributionForCurrentOS" }.configureEach {
    dependsOn("copyVlcLibs")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}
