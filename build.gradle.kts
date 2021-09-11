import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.30"

    kotlin("jvm") version kotlinVersion
    idea
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.google.protobuf") version "0.8.14"
}

group = "ctr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencyManagement {
    imports {
        mavenBom("org.apache.logging.log4j:log4j-bom:2.11.1")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.4")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.23")
    implementation("org.flywaydb:flyway-core")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.tukaani:xz:1.9")
    implementation("io.micrometer:micrometer-registry-influx")
    implementation("com.google.protobuf:protobuf-java:3.17.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("com.h2database:h2")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

configurations.all {
    exclude("org.springframework.boot", "spring-boot-starter-logging")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    launchScript()
}

allOpen {
    annotation("javax.persistence.Entity")
}

protobuf {
    protoc {
        // Download from repositories
        artifact = "com.google.protobuf:protoc:3.14.0"
    }
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java)
    .configure {
        val badVersions = ".*(-rc(-.*)?$|-m.*)".toRegex()
        rejectVersionIf {
            candidate.version.toLowerCase().matches(badVersions)
        }
    }