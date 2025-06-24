plugins {
    id("java")
    alias(libs.plugins.springBoot) apply true
    alias(libs.plugins.springDependencyManagement) apply true
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    //toolchain {
    //    languageVersion = JavaLanguageVersion.of(17)
    //}
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterDataRest)
    implementation(libs.springBootStarterDataJPA)
    implementation(libs.springBootStarterThymeleaf)
    implementation(libs.springBootStarterSecurity)

    implementation(libs.h2Database)
    implementation(libs.mysqlConnector)

    // Additional dependencies for Spring Boot 3.4.2 compatibility
    implementation(libs.springBootStarterValidation)
    implementation(libs.jakartaValidation)

    // Test dependencies
    testImplementation(libs.springBootStarterTest)
    testRuntimeOnly(libs.junitPlatform)
}

tasks {
    test {
        useJUnitPlatform()
    }

    bootJar {
        enabled = true
    }
}