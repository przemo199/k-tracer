import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0-RC1"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
    sourceSets.all {
        languageSettings.apply {
            languageVersion = "2.0"
        }
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val junitVersion = "5.10.1"

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.3")
    implementation("dev.reimer:progressbar-ktx:0.1.0")
    implementation("me.tongfei:progressbar:0.9.5")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

application {
    mainClass.set("org.example.ktracer.KTracerKt")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    test {
        useJUnitPlatform()
    }
}
