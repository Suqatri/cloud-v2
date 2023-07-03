package dev.redicloud.cluster.file.filter

import dev.redicloud.cluster.file.FileNodeRepository
import dev.redicloud.cluster.file.event.FileNodeConnectedEvent
import dev.redicloud.cluster.file.event.FileNodeDisconnectedEvent
import dev.redicloud.event.EventManager
import dev.redicloud.service.base.events.node.NodeSuspendedEvent
import kotlinx.coroutines.runBlocking

class IPFilter(
    val eventManager: EventManager,
    val fileNodeRepository: FileNodeRepository
) {

    private val allowedIpCache = mutableListOf<String>()
    private val onNodeConnect = eventManager.listen<FileNodeConnectedEvent> {
        runBlocking {
            val fileNode = fileNodeRepository.getFileNode(it.serviceId) ?: return@runBlocking
            val ipAddress = fileNode.hostname
            allowedIpCache.add(ipAddress)
        }
    }
    private val onNodeDisconnect = eventManager.listen<FileNodeDisconnectedEvent> {
        runBlocking {
            val fileNode = fileNodeRepository.getFileNode(it.serviceId) ?: return@runBlocking
            val ipAddress = fileNode.hostname
            allowedIpCache.remove(ipAddress)
        }
    }
    private val onNodeSuspend = eventManager.listen<NodeSuspendedEvent> {
        runBlocking {
            val fileNode = fileNodeRepository.getFileNode(it.serviceId) ?: return@runBlocking
            val ipAddress = fileNode.hostname
            allowedIpCache.remove(ipAddress)
        }
    }

    init {
        runBlocking {
            System.getProperty("redicloud.filter.ip.bypass", "127.0.0.1;0.0.0.0").split(";").forEach {
                allowedIpCache.add(it)
            }
            fileNodeRepository.getAll().forEach { allowedIpCache.add(it.hostname) }
        }
    }

    fun canConnect(remoteAddress: String): Boolean = allowedIpCache.contains(remoteAddress)

}