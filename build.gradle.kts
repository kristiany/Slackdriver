import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"

    //https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.slackdriver"
version = "0.0.2"

application {
    mainClassName = "MainKt"
}

dependencies {
    implementation("khttp:khttp:1.0.0")
    implementation("com.google.cloud:google-cloud-errorreporting:0.120.5-beta")
    implementation("com.google.cloud:google-cloud-logging:1.102.0")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")

    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.test {
    useJUnitPlatform()
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