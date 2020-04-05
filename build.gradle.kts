import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.70"

    //https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.gerror"
version = "0.0.1"

application {
    mainClassName = "MainKt"
}

dependencies {
    implementation("khttp:khttp:1.0.0")
    implementation("com.google.cloud:google-cloud-errorreporting:0.119.1-beta")
    implementation("com.google.cloud:google-cloud-logging:1.100.0")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}

repositories {
    jcenter()
    mavenCentral()
}

configurations {
    "implementation" {
        resolutionStrategy.failOnVersionConflict()
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

// GRPC seems to need a proper shadowed jar
// https://github.com/grpc/grpc-java/issues/5493#issuecomment-478500418
tasks.shadowJar {
    mergeServiceFiles()
}