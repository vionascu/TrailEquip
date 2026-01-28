plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.hibernate.orm:hibernate-spatial:6.4.1.Final")
    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

    testImplementation("org.testcontainers:postgresql:1.19.3")
}

springBoot {
    mainClass.set("com.trailequip.trail.TrailServiceApplication")
}
