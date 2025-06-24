import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.1")
    implementation("com.sksamuel.scrimage:scrimage-core:4.3.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.1")
    implementation("dev.reimer:progressbar-ktx:0.1.0")
    implementation("me.tongfei:progressbar:0.10.1")
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
}

application {
    mainClass.set("org.example.ktracer.KTracerKt")
}

tasks.test {
    useJUnitPlatform()
}
