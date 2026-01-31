import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    // id("com.diffplug.spotless") version "6.25.0"  // Temporarily disabled for Gradle 9 compatibility
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

// Disable jar creation for root project since it only orchestrates builds
tasks {
    jar.configure { enabled = false }
}

allprojects {
    group = "com.trailequip"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/release") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    // apply(plugin = "com.diffplug.spotless")  // Temporarily disabled for Gradle 9 compatibility

    // spotless {
    //     java {
    //         importOrder()
    //         palantirJavaFormat("2.42.0")  // Explicitly specify version compatible with Java 21
    //         endWithNewline()
    //     }
    // }

    dependencies {
        // Core Spring Boot
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        // APIs & Serialization
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        // Logging
        implementation("org.springframework.boot:spring-boot-starter-logging")
        implementation("org.projectlombok:lombok")

        // Testing
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.mockito:mockito-junit-jupiter")
        testImplementation("org.testcontainers:testcontainers:1.19.3")
        testImplementation("org.testcontainers:junit-jupiter:1.19.3")

        // Annotation processing
        annotationProcessor("org.projectlombok:lombok")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }

    tasks.register<Test>("integrationTest") {
        useJUnitPlatform {
            includeTags("integration")
        }
    }
}
