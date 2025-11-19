plugins {
    // Kotlin Multiplatform
    kotlin("multiplatform") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    
    // Compose Multiplatform
    id("org.jetbrains.compose") version "1.6.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
    
    // SQLDelight
    id("app.cash.sqldelight") version "2.0.1" apply false
}

allprojects {
    group = "com.musicmusic"
    version = findProperty("app.version") as String? ?: "0.1.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
