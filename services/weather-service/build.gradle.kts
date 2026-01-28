plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")
}

springBoot {
    mainClass.set("com.trailequip.weather.WeatherServiceApplication")
}
