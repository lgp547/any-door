plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.5.2"
    id("com.github.johnrengelman.shadow") version "7.0.0" // Include dependency packaging

}

group = "io.github.lgp547"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("cn.hutool:hutool-http:5.8.9")

}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("org.jetbrains.idea.maven","com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
