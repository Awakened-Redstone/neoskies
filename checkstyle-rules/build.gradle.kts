plugins {
    id("java")
    `maven-publish`
}

group = "com.awakenedredstone.checkstyle"
version = "1.0.0-dev.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.puppycrawl.tools:checkstyle:10.17.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
