plugins {
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    jacoco
    war
}

group = "ar.edu.unsam"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val kotestVersion = "5.8.0"

    // básicos de cualquier proyecto Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    // base de datos
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")

    // seguridad
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    val jwtVersion = "0.13.0"
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // graphql — misma versión que el profesor; compatible con Spring Boot 3.5.x
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:10.5.0"))
    implementation("com.netflix.graphql.dgs:dgs-starter")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/config/**", "**/entity/**", "**/*Application*.*", "**/ServletInitializer.*")
            }
        })
    )
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.register("runOnGitHub") {
    dependsOn("jacocoTestReport")
    group = "custom"
    description = "$ ./gradlew runOnGitHub # runs on GitHub Action"
}