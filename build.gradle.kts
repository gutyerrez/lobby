plugins {
    kotlin("jvm") version "1.5.10"

    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "net.hyren"
version = "0.1-ALPHA"

repositories {
    mavenCentral()

    maven("https://repository.hyren.net/") {
        credentials {
            username = System.getenv("MAVEN_USERNAME")
            password = System.getenv("MAVEN_PASSWORD")
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}.jar")
    }
}

dependencies {
    // kotlin
    compileOnly(kotlin("stdlib"))

    // paer-spigot-server
    compileOnly("org.github.paperspigot:paper-spigot-server:1.8.8-R0.1-SNAPSHOT")

    // minecraft-server
    compileOnly("net.hyren:minecraft-server:1.8.8-SNAPSHOT")

    // waterfall chat
    compileOnly("io.github.waterfallmc:waterfall-chat:1.16-R0.5-SNAPSHOT")

    // exposed
    compileOnly("org.jetbrains.exposed:exposed-dao:0.31.1")
    compileOnly("org.jetbrains.exposed:exposed-jodatime:0.31.1")

    // redis
    compileOnly("redis.clients:jedis:3.3.0")

    // eventbus
    compileOnly("org.greenrobot:eventbus:3.2.0")

    // caffeine
    compileOnly("com.github.ben-manes.caffeine:caffeine:2.8.5")

    // core
    compileOnly("net.hyren:core-shared:0.1-ALPHA")
    compileOnly("net.hyren:core-spigot:0.1-ALPHA")
}
