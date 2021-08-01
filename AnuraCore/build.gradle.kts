plugins {
    java
}

group = "de.mc-anura.core"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://libraries.minecraft.net/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-core:2.14.1")
    compileOnly("com.mojang:authlib:1.5.21")
}
