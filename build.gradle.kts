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
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.intellij") version "1.15.0"
    id("com.palantir.git-version") version "0.15.0"
    id("com.adarshr.test-logger") version "3.2.0"
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation("junit", "junit", "4.12")
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
                // PS - PhpStorm
                "PS-223.8617.59", // 2022.3.2
                "PS-222.4345.15", // 2022.2.3

                // IU - IntelliJ IDEA Ultimate
                "IU-223.8617.56", // 2022.3.2
                "IU-222.4345.14", // 2022.2.3

                // IC - IntelliJ IDEA Community Edition
                "IC-223.8617.56", // 2022.3.2
                "IC-222.4345.14", // 2022.2.3

                // WS - WebStorm
                "WS-223.8617.44", // 2022.3.2
                "WS-222.4345.14", // 2022.2.3
            )
        )
        failureLevel.set(
            listOf(
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }
}
