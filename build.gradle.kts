import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.maven.MavenPom
import java.net.URI

plugins {
    kotlin("jvm") version "1.2.50"
    id("com.jfrog.bintray") version "1.8.1"
    id("com.github.johnrengelman.shadow") version "2.0.2"
    `maven-publish`
}

group = "net.arwix.astronomy2"
val artifactID = "astronomy-core"
version = "0.22"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = URI("http://dl.bintray.com/v170nix/astronomy2")
    }
}

val shadowJar: ShadowJar by tasks
shadowJar.apply {
    baseName = artifactID
    classifier = null
}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType(GradleBuild::class.java) {
        dependsOn(shadowJar)
    }
    withType<GenerateMavenPom> {
        destination = file("$buildDir/libs/${shadowJar.archiveName}.pom")
    }
}

bintray {
    user = "v170nix"
    key = findProperty("bintrayApiKey") as String
    setPublications("ProjectPublication")
    publish = true
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "astronomy2"
        name = "astronomy2-core"
        userOrg = user
//        websiteUrl = "https://blog.simon-wirtz.de"
//        githubRepo = "s1monw1/TlsLibrary"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/v170nix/astronomy2-core.git"
//        version(delegateClosureOf<BintrayExtension.VersionConfig> {
//            name = project.version as String
//        })
    })
}

publishing {
    publications.invoke {
        "ProjectPublication"(MavenPublication::class) {
            groupId = project.group as String
            artifactId = artifactID
            artifact(shadowJar)
            version = project.version as String
            pom.addDependencies()
        }
    }
}

fun MavenPom.addDependencies() = withXml {
    asNode().appendNode("dependencies").let { depNode ->
        configurations.compile.allDependencies.forEach {
            depNode.appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
            }
        }
    }
}

// java.sourceSets["test"].java.srcDir("src/test/kotlin")

