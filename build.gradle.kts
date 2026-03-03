import org.gradle.api.artifacts.repositories.IvyArtifactRepository
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

    // Workaround for ORT Gradle Inspector NPE: localPlatformArtifacts() creates an Ivy repo with
    // absolute patterns but no URL, causing UrlArtifactRepository.getUrl() to return null.
    // Setting a placeholder URL does not affect resolution since the patterns are absolute paths.
    forEach {
        if (it is IvyArtifactRepository && it.url == null) {
            it.setUrl(layout.buildDirectory.dir("tmp/ivy-placeholder"))
        }
    }
}

plugins {
    id("java")
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("com.palantir.git-version") version "5.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

dependencies {
    implementation("com.dylibso.chicory","runtime","1.7.2")
    implementation("com.dylibso.chicory","wasi","1.7.2")
    implementation("com.dylibso.chicory","compiler","1.7.2")

    testImplementation("junit", "junit", "4.13.2")

    implementation(kotlin("reflect"))
    intellijPlatform {
        create(providers.gradleProperty("platform-type").get(),providers.gradleProperty("platform-version").get()) {}
        pluginVerifier()
        zipSigner()
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
            sinceBuild = "251"
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
    jvmToolchain(21)
}

tasks {
    val copyLicenseFiles by registering(Copy::class) {
        from(rootDir) {
            include("LICENSE", "NOTICE")
        }
        into(layout.buildDirectory.dir("generated/resources/license"))
    }

    test {
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        useJUnit()

        // Prevent "File access outside allowed roots" in multi-module tests, because modules each have an .iml
        environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
    }
}

sourceSets {
    main {
        resources.srcDir(tasks.named("copyLicenseFiles").map { (it as Copy).destinationDir })
    }
}
