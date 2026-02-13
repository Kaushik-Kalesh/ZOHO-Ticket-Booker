plugins {
    java
    war
}

group = "org.example"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.9")
    implementation("tools.jackson.core:jackson-databind:3.1.0-rc1")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
}