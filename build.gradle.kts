plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.2.0")
    implementation("com.google.cloud:google-cloud-texttospeech:2.2.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("TelegramKt")
}

tasks.shadowJar {
    mergeServiceFiles()
}