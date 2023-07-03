group = "dev.redicloud.service.base"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":database"))
    compileOnly(project(":packets"))
    compileOnly(project(":utils"))
    compileOnly(project(":repositories:service-repository"))
    compileOnly(project(":repositories:node-repository"))
    compileOnly(project(":repositories:server-repository"))
    compileOnly(project(":repositories:file-template-repository"))
    compileOnly(project(":repositories:configuration-template-repository"))
    compileOnly(project(":repositories:server-version-repository"))
    compileOnly(project(":repositories:java-version-repository"))
    compileOnly(project(":events"))
    compileOnly(project(":tasks"))
    compileOnly(project(":console"))
    compileOnly(project(":logging"))
    compileOnly(project(":commands:command-api"))


    dependency("org.redisson:redisson:3.21.3")
    dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}