plugins {
    java
}

group = "de.mc-anura.realistic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://libraries.minecraft.net/") }
    maven { url = uri("https://repo.md-5.net/content/repositories/snapshots/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly(files("../libs/LogBlock.jar"))
    implementation(project(":AnuraCore"))
    implementation(project(":AnuraFreebuild"))
}
