plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "net.mcquest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.unnamed.team/repository/unnamed-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:3de64aafd8")

    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.13.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("team.unnamed:creative-api:1.5.0")
    implementation("team.unnamed:creative-serializer-minecraft:1.5.0")

    implementation("team.unnamed:hephaestus-api:0.9.1-SNAPSHOT")
    implementation("team.unnamed:hephaestus-reader-blockbench:0.9.1-SNAPSHOT")
    implementation("team.unnamed:hephaestus-runtime-minestom:0.9.1-SNAPSHOT")

    implementation("net.kyori:adventure-text-minimessage:4.16.0")

    implementation("org.gagravarr:vorbis-java-core:0.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("net.mcquest.engine.MainKt")
}
