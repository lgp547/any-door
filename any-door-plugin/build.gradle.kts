import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import org.jetbrains.intellij.tasks.PrepareSandboxTask
import java.util.zip.ZipFile

fun properties(key: String) = providers.gradleProperty(key)

val anyDoorAllDependenceJarName = "any-door-all-dependence.jar"
val excludedAnyDoorBundlePrefixes = listOf("any-door", "arthas")

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0" /*https://plugins.gradle.org/m2/org/jetbrains/intellij/plugins/gradle-intellij-plugin*/
}

group = "io.github.lgp547"
version = "2.3.2"

repositories {
    mavenLocal()
    mavenCentral()
    // todo：自行修改成本地maven仓库地址
    mavenLocal {
        url = uri("/Users/lgp/.m2/repository")
    }
}

dependencies {
    implementation("io.github.lgp547:any-door-core:2.3.2")
    implementation("io.github.lgp547:any-door-attach:2.3.2")
    testImplementation("junit:junit:4.13.2")

}

val buildAnyDoorAllDependenceJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds the isolated dependency bundle loaded into the target JVM"
    archiveFileName.set(anyDoorAllDependenceJarName)
    destinationDirectory.set(layout.buildDirectory.dir("generated/any-door-agent"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    from({
        configurations.runtimeClasspath.get()
            .filter { dependency ->
                dependency.isFile && excludedAnyDoorBundlePrefixes.none(dependency.name::startsWith)
            }
            .map(::zipTree)
    })

    exclude(
        "META-INF/MANIFEST.MF",
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.RSA",
        "META-INF/*.DSA",
        "module-info.class",
        "META-INF/versions/*/module-info.class",
    )
}

val verifyAnyDoorAllDependenceJar by tasks.registering {
    group = "verification"
    description = "Verifies the target-JVM dependency bundle contents"
    dependsOn(buildAnyDoorAllDependenceJar)
    inputs.file(buildAnyDoorAllDependenceJar.flatMap { it.archiveFile })

    doLast {
        val bundle = buildAnyDoorAllDependenceJar.get().archiveFile.get().asFile
        val requiredEntries = listOf(
            "com/fasterxml/jackson/databind/ObjectMapper.class",
            "org/springframework/context/ApplicationContext.class",
        )
        val forbiddenEntries = listOf(
            "io/github/lgp547/anydoor/core/AnyDoorService.class",
            "io/github/lgp547/anydoor/attach/AnyDoorAttach.class",
            "arthas/VmTool.class",
        )

        check(bundle.isFile) { "Missing target-JVM dependency bundle: $bundle" }
        ZipFile(bundle).use { archive ->
            requiredEntries.forEach { entry ->
                check(archive.getEntry(entry) != null) { "Missing required bundle entry: $entry" }
            }
            forbiddenEntries.forEach { entry ->
                check(archive.getEntry(entry) == null) { "Forbidden bundle entry: $entry" }
            }
        }
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    // todo：若没商业版授权，这里改成社区版进行调式
    version.set("2024.3") // 沙盒 idea 的版本
//    type.set("IU") // 商业版
    type.set("IC") // 社区版

    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    named("check") {
        dependsOn(verifyAnyDoorAllDependenceJar)
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

tasks.withType<PrepareSandboxTask>().configureEach {
    dependsOn(buildAnyDoorAllDependenceJar)
    intoChild(pluginName.map { "$it/agent" })
        .from(buildAnyDoorAllDependenceJar.flatMap { it.archiveFile })
}
