import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.maven.MavenPom
import java.net.URI

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

plugins {
    kotlin("jvm") version "1.2.50"
    id("com.github.johnrengelman.shadow") version "2.0.2"
//    id("java")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.3"
}

group = "net.arwix.astronomy2"
val artifactID = "astronomy-core"
version = "0.1.4"

setProperty("targetCompatibility", JavaVersion.VERSION_1_6)
setProperty("sourceCompatibility", JavaVersion.VERSION_1_6)

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = URI("http://dl.bintray.com/v170nix/astronomy2")
    }
}

configurations {

}

val shadowJar: ShadowJar by tasks
shadowJar.apply {
    baseName = artifactID
    classifier = null
    dependsOn("classes")
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:1.2.50"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common:1.2.50"))
        exclude(dependency("org.jetbrains:annotations:13.0"))
        exclude(dependency("org.apiguardian:apiguardian-api:1.0.0"))
    }
}

//fun sourceSets(name: String) = the<JavaPluginConvention>().sourceSets.getByName(name)

//val sourcesJar = task<Jar>("sourcesJar") {
//    dependsOn("classes")
//    from(sourceSets("main").allSource)
//    classifier = null
//}

dependencies {
    compile(kotlin("stdlib"))
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.6"
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
//            from(components.getByName("java"))
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

//inline fun Project.artifacts(configuration: KotlinArtifactsHandler.() -> Unit) =
//        KotlinArtifactsHandler(artifacts).configuration()

//class KotlinArtifactsHandler(val artifacts: ArtifactHandler) : ArtifactHandler by artifacts {
//
//    operator fun String.invoke(dependencyNotation: Any): PublishArtifact =
//            artifacts.add(this, dependencyNotation)
//
//    inline operator fun invoke(configuration: KotlinArtifactsHandler.() -> Unit) =
//            configuration()
//}

// java.sourceSets["test"].java.srcDir("src/test/kotlin")

