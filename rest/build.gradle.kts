plugins {
    id("java")
    alias(libs.plugins.springBoot) apply true
    alias(libs.plugins.springDependencyManagement) apply true
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

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
	implementation("io.swagger:swagger-core:1.6.16")
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterDataRest)
    implementation(libs.springBootStarterDataJPA)
    implementation(libs.springBootStarterThymeleaf)
    implementation(libs.springBootStarterSecurity)
    
    compileOnly("org.projectlombok:lombok:1.18.38")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
	
	testCompileOnly("org.projectlombok:lombok:1.18.38")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

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