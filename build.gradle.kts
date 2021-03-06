plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.intellij") version "1.2.1"
    kotlin("plugin.serialization") version "1.5.31"
}

group = "com.tradetested"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        url = uri("https://gitlab.com/api/v4/groups/tradetested/-/packages/maven")
        name = "GitLab"
        credentials(HttpHeaderCredentials::class) {
            name = "Deploy-Token"
            value = System.getenv("TT_GITLAB_PACKAGE_DEPLOY_TOKEN")
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("com.tradetested:quarkus-testsocket-spi:1.0.3")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.3")
    plugins.set(listOf("java", "JUnit", "Kotlin"))
}
tasks {
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}