plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.github.mihanizzm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.github.mihanizzm.MainKt"
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.dokkaHtml.configure {
    outputDirectory.set(file("build/dokka"))
}
