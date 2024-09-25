import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun getVersionDetails(): com.palantir.gradle.gitversion.VersionDetails =
    (extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails

val gitInfo = getVersionDetails()
var releaseChannels = arrayOf<String>()

when {
    properties.containsKey("snapshotVersion") -> {
        version = properties["snapshotVersion"]!!
        releaseChannels = arrayOf("snapshot")
    }

    gitInfo.isCleanTag -> {
        version = gitInfo.lastTag
        releaseChannels = arrayOf("default")
    }

    else -> {
        version = gitInfo.version
        releaseChannels = arrayOf("local")
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    id("java")
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("com.palantir.git-version") version "3.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation("junit", "junit", "4.13.2")
    intellijPlatform {
        intellijIdeaUltimate(providers.gradleProperty("idea-version"))
        pluginVerifier()
        zipSigner()
        instrumentationTools()
        bundledPlugins(
            listOf(
                "com.intellij.css",
                "HtmlTools",
                "JavaScript",
            )
        )
        testFramework(TestFrameworkType.Platform)
    }

}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellijPlatform {

    pluginConfiguration {
        name = "MJML Support"
        ideaVersion {
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    publishing {
        token = System.getenv("JB_TOKEN")
        channels = releaseChannels.toList()
    }
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        useJUnit()

        // Prevent "File access outside allowed roots" in multi-module tests, because modules each have an .iml
        environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
    }
}
