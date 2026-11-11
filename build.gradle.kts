plugins {
    kotlin("jvm") version "2.3.20"
    id("com.gradleup.shadow") version "9.2.2"
}

val minecraftVersion: String by project
val paperBuild: String by project

group = "de.mermack"
// CI passes -PpluginVersion=<mc>-patch-<n>; local builds are marked as dev builds
version = providers.gradleProperty("pluginVersion").getOrElse("$minecraftVersion-dev")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion.build.$paperBuild-stable")
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(25)
}

tasks {
    processResources {
        val props = mapOf("version" to project.version, "minecraftVersion" to minecraftVersion)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("kotlin", "de.mermack.alwaysinfect.libs.kotlin")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    jar {
        enabled = false
    }
}
