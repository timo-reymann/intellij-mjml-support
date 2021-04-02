plugins {
    id("org.jetbrains.intellij") version "0.7.2"
    id("com.palantir.git-version") version "0.12.2"
    java
    kotlin("jvm") version "1.4.31"
}

fun getVersionDetails(): com.palantir.gradle.gitversion.VersionDetails = (extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails
var gitInfo = getVersionDetails()

group = "de.timo_reymann"
version = gitInfo.lastTag

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

repositories {
    mavenCentral()
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies")
    maven("https://jetbrains.bintray.com/jediterm")
    maven("https://jetbrains.bintray.com/pty4j")
    maven("https://cache-redirector.jetbrains.com/www.myget.org/F/rd-snapshots/maven")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "IU-LATEST-EAP-SNAPSHOT"

    updateSinceUntilBuild = false
    downloadSources = true
    pluginName = "MJML Support"

    setPlugins(
        "CSS",
        "HtmlTools",
        "JavaScript"
    )
}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Prevent "File access outside allowed roots" in multi module tests, because modules each have an .iml
    environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    setVersion(gitInfo.lastTag)
}

tasks.getByName<org.jetbrains.intellij.tasks.PublishTask>("publishPlugin") {
    setToken(System.getenv("JB_TOKEN"))
}
