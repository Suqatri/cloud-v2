group = "dev.redicloud.packets"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":database"))
    compileOnly(project(":utils"))
    compileOnly(project(":logging"))

    dependency("org.redisson:redisson:3.21.3")
    dependency("com.google.code.gson:gson:2.10.1")
}