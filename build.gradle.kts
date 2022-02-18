plugins {
    id("org.springframework.boot") version "2.5.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("java")
}

group = "io.picthor"
version = "v1.0.0-beta-3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0")
    implementation("org.mybatis:mybatis-typehandlers-jsr310:1.0.2")
    implementation("org.mybatis:mybatis-migrations:3.3.10")
    implementation("org.postgresql:postgresql")
    implementation("com.realcnbs:horizon-framework:1.0.5")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.jayway.jsonpath:json-path:2.6.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.3")


    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    compileOnly("org.projectlombok:lombok")


    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val copyPublicResources = tasks.register<Copy>("copyPublicResources") {
    from("src/main/public")
    include("**/*.*")
    into("$buildDir/resources/main/public")
}


val copyMigrations = tasks.register<Copy>("copyMigrations") {
    from("migrations/scripts")
    include("**/*.sql")
    into("$buildDir/resources/main/migrations")
}

val copyMappersToResources = tasks.register<Copy>("copyMappersToResources") {
    from("src/main/java/io/picthor/data/mapper")
    include("**/*.xml")
    into("$buildDir/resources/main/io/picthor/data/mapper")
}

tasks.withType<ProcessResources> {
    dependsOn(copyMappersToResources)
    dependsOn(copyPublicResources)
    dependsOn(copyMigrations)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    launchScript()
}
