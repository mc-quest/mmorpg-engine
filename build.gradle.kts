plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "net.mcquest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.unnamed.team/repository/unnamed-public/")
}

dependencies {
    implementation("dev.hollowcube:minestom-ce:1554487748")
    implementation("team.unnamed:creative-api:1.5.0")
    implementation("team.unnamed:creative-serializer-minecraft:1.5.0")
    implementation("team.unnamed:hephaestus-api:0.6.0-SNAPSHOT")
    implementation("team.unnamed:hephaestus-reader-blockbench:0.6.0-SNAPSHOT")
    implementation("team.unnamed:hephaestus-runtime-minestom-ce:0.6.0-SNAPSHOT")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.mcquest.engine.MainKt")
}
