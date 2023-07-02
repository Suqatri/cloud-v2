group = "dev.redicloud.server.factory"

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":tasks"))
    compileOnly(project(":database"))
    compileOnly(project(":logging"))
    compileOnly(project(":services:base-service"))
    compileOnly(project(":events"))
    compileOnly(project(":repositories:server-repository"))
    compileOnly(project(":repositories:node-repository"))
    compileOnly(project(":repositories:file-template-repository"))
    compileOnly(project(":repositories:configuration-template-repository"))
    compileOnly(project(":repositories:java-version-repository"))
    compileOnly(project(":repositories:server-version-repository"))
    compileOnly(project(":repositories:service-repository"))
}