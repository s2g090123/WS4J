plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "jiachian"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.5")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    jar {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")

    implementation("org.apache.opennlp:opennlp-tools:1.8.4")
    implementation("com.github.rm-hull:jwi:7d37b0d23d")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("com.google.guava:guava:32.0.0-jre")
    implementation("com.google.code.gson:gson:2.8.9")
    compileOnly("org.jetbrains:annotations:16.0.2")

    testImplementation("junit:junit:4.13.1")
}