plugins {
    kotlin("jvm") version "2.5.0-Beta2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(27)

    compilerOptions {
        freeCompilerArgs.add("-Xfull-value-classes")
        freeCompilerArgs.add("-Xcompanion-blocks-and-extensions")
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.1.0")
    implementation("com.sksamuel.scrimage:scrimage-core:4.3.0")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.1.3")
    implementation("dev.reimer:progressbar-ktx:0.1.0")
    implementation("me.tongfei:progressbar:0.10.2")
    implementation("com.github.ajalt.clikt:clikt:5.1.0")
}

application {
    mainClass.set("org.example.ktracer.KTracerKt")
}

tasks.test {
    useJUnitPlatform()
}
