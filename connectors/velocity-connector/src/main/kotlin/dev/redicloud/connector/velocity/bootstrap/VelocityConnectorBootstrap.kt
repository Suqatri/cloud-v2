package dev.redicloud.connector.velocity.bootstrap

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.redicloud.connector.velocity.VelocityConnector
import dev.redicloud.libloader.boot.Bootstrap
import dev.redicloud.libloader.boot.loaders.URLClassLoaderJarLoader
import java.net.URLClassLoader
import kotlin.system.exitProcess


@Plugin(
    id = "redicloud-connector-velocity",
    name = "redicloud-connector-velocity",
    version = "2.0.0-SNAPSHOT",
    url = "https://redicloud.dev",
    authors = ["RediCloud"]
)
class VelocityConnectorBootstrap @Inject constructor(val proxyServer: ProxyServer) {

    private var connector: VelocityConnector? = null

    init {
        try {
            Bootstrap().apply(URLClassLoaderJarLoader(this.javaClass.classLoader as URLClassLoader))
            connector = VelocityConnector(this, proxyServer)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        connector?.onEnable()
    }

    @Subscribe(order = PostOrder.LAST)
    fun onShutdown(event: ProxyShutdownEvent) {
        if (connector == null) exitProcess(0)
        connector?.onDisable()
    }

}