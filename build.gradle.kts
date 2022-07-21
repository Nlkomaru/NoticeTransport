plugins {
    id("java")
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.0.1"
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("org.sonarqube") version "3.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    kotlin("plugin.serialization") version "1.6.10"
    kotlin("kapt") version "1.7.10"
}

group = "com.noticemc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.incendo.org/content/repositories/snapshots")
}

val cloudVersion = "1.7.0"
val exposedVersion = "0.38.2"
dependencies {
    compileOnly("io.papermc.paper", "paper-api", "1.19-R0.1-SNAPSHOT")

    compileOnly("com.velocitypowered", "velocity-api", "3.0.1")
    kapt("com.velocitypowered","velocity-api","3.0.1")

    implementation("cloud.commandframework", "cloud-core", cloudVersion)
    implementation("cloud.commandframework", "cloud-annotations", cloudVersion)
    kapt("cloud.commandframework", "cloud-annotations", cloudVersion)
    implementation("cloud.commandframework", "cloud-velocity", cloudVersion)
    implementation("cloud.commandframework", "cloud-kotlin-extensions", cloudVersion)
    implementation("cloud.commandframework", "cloud-kotlin-coroutines-annotations", cloudVersion)
    implementation("cloud.commandframework", "cloud-kotlin-coroutines", cloudVersion)

    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.3")

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.javaParameters = true
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    shadowJar {
        relocate("cloud.commandframework", "com.noticemc.noticetransport.shaded.cloud")
        relocate("io.leangen.geantyref", "com.noticemc.noticetransport.shaded.typetoken")
    }
    build {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.19")
    }
}


bukkit {
    name = "NoticeTransport"
    version = "miencraft_plugin_version"

    main = "com.noticemc.noticetransport.paper.NoticeTransport"

    apiVersion = "1.19"
    libraries = listOf("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.2.0", "com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.2.0")

    permissions {

    }
}