import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "4.0.3"
//    id("java")
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.3"
}

group = "net.arwix.astronomy2"
val artifactID = "astronomy-core"
version = "0.8.1"

setProperty("targetCompatibility", JavaVersion.VERSION_1_6)
setProperty("sourceCompatibility", JavaVersion.VERSION_1_6)

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
    classifier = ""
    dependsOn("classes")
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
        exclude(dependency("org.jetbrains.kotlinx:atomicfu-common"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("org.apiguardian:apiguardian-api"))
        exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core"))
        exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-common"))
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
    compile ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testCompile("net.arwix.astronomy2:ephemeris-vsop87a:0.7.0")
}

kotlin {
//    experimental.coroutines = Coroutines.ENABLE
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

