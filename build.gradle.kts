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
}

plugins {
    id("java")
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
    id("com.palantir.git-version") version "3.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation("junit", "junit", "4.13.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set(properties["idea-version"] as String)
    updateSinceUntilBuild.set(false)
    downloadSources.set(true)
    pluginName.set("MJML Support")
    plugins.set(
        listOf(
            "com.intellij.css",
            "HtmlTools",
            "JavaScript"
        )
    )
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    test {
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        useJUnit()

        // Prevent "File access outside allowed roots" in multi-module tests, because modules each have an .iml
        environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
    }

    patchPluginXml {
        setVersion(project.version)
    }

    publishPlugin {
        dependsOn("patchPluginXml")
        token.set(System.getenv("JB_TOKEN"))
        channels.set(releaseChannels.toList())
    }

    runPluginVerifier {
        ideVersions.set(
            // Generated with https://github.com/timo-reymann/script-shelve/blob/master/jetbrains/query_ide_versions_for_verifier.py
            listOf(
                "RD-232.9921.83", // 2023.2.2
                "WS-232.9921.42", // 2023.2.2
                "IU-232.9921.47", // 2023.2.2
                "PS-232.9921.55", // 2023.2.2
            )
        )
        failureLevel.set(
            listOf(
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }
}
