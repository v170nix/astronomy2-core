import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.2.50"
    id("com.jfrog.bintray") version "1.8.1"
}

group = "net.arwix.astronomy2"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
    maven {
       url = URI("http://dl.bintray.com/v170nix/astronomy2")
    }
}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}



// java.sourceSets["test"].java.srcDir("src/test/kotlin")

