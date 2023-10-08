plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "io.github.lgp547"
version = "2.0.2"

repositories {
    mavenCentral()
    mavenLocal {
        url = uri("/Users/lgp/.m2/repository")
    }
}

dependencies {
    implementation("io.github.lgp547:any-door-core:2.0.2")
    implementation("io.github.lgp547:any-door-attach:2.0.2")

}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2022.1")
    type.set("IU") // Target IDE Platform
//    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("203.*")
        untilBuild.set("232.*")
    }

//    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }
//
//    publishPlugin {
//        token.set(System.getenv("PUBLISH_TOKEN"))
//    }
}
