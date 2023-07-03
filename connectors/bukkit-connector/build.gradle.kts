group = "dev.redicloud.connector.bukkit"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
}

dependencies {
    shade(project(":api"))
    shade(project(":services:base-service"))
    shade(project(":repositories:node-repository"))
    shade(project(":repositories:service-repository"))
    shade(project(":repositories:server-repository"))
    shade(project(":repositories:file-template-repository"))
    shade(project(":repositories:configuration-template-repository"))
    shade(project(":repositories:server-version-repository"))
    shade(project(":repositories:java-version-repository"))
    shade(project(":database"))
    shade(project(":utils"))
    shade(project(":events"))
    shade(project(":packets"))
    shade(project(":commands:command-api"))
    shade(project(":logging"))
    shade(project(":console"))
    shade(project(":tasks"))
    shade("dev.redicloud.libloader:libloader-bootstrap:${Versions.libloaderBootstrap}")

    compileOnly("org.spigotmc:spigot-api:${Versions.minecraftVersion}")
}