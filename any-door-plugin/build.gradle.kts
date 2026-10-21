import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask
import java.util.zip.ZipFile

fun properties(key: String) = providers.gradleProperty(key)

val anyDoorAllDependenceJarName = "any-door-all-dependence.jar"
val excludedAnyDoorBundlePrefixes = listOf("any-door", "arthas")
// 与 IDEA 2024.3 保持一致，避免测试和表单编译工具漂移到其他构建。
val platformBuild = "243.21565.193"

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.github.lgp547"
version = "2.3.2"

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// 目标 JVM 使用独立 Kotlin 运行库；IDE 使用平台提供的 Kotlin，避免插件类加载冲突。
val agentRuntime by configurations.creating

dependencies {
    listOf("any-door-core", "any-door-attach").forEach { artifact ->
        implementation("io.github.lgp547:$artifact:2.3.2") {
            exclude(group = "org.jetbrains.kotlin")
            exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-kotlin")
        }
        agentRuntime("io.github.lgp547:$artifact:2.3.2")
    }
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        intellijIdeaCommunity("2024.3", useInstaller = false)
        bundledPlugins("com.intellij.java", "com.intellij.modules.json", "org.jetbrains.kotlin")
        javaCompiler(platformBuild)
        testFramework(TestFrameworkType.Platform, platformBuild)
        testFramework(TestFrameworkType.Plugin.Java, platformBuild)
    }
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
        agentRuntime
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
            "com/fasterxml/jackson/module/kotlin/KotlinModule.class",
            "kotlin/Metadata.class",
            "kotlin/reflect/jvm/ReflectJvmMapping.class",
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

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set(properties("pluginSinceBuild"))
            untilBuild.set(provider { null })
        }
    }
}

tasks {
    test {
        systemProperty("java.awt.headless", "true")
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    named("check") {
        dependsOn(verifyAnyDoorAllDependenceJar)
    }

}

tasks.withType<PrepareSandboxTask>().configureEach {
    dependsOn(buildAnyDoorAllDependenceJar)
    intoChild(pluginName.map { "$it/agent" })
        .from(buildAnyDoorAllDependenceJar.flatMap { it.archiveFile })
}
