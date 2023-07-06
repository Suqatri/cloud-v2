package dev.redicloud.service.base

import dev.redicloud.service.base.packets.CloudServiceShutdownPacket
import dev.redicloud.service.base.packets.CloudServiceShutdownResponse
import dev.redicloud.commands.api.CommandArgumentParser
import dev.redicloud.commands.api.ICommandSuggester
import dev.redicloud.repository.node.NodeRepository
import dev.redicloud.database.DatabaseConnection
import dev.redicloud.database.codec.GsonCodec
import dev.redicloud.database.config.DatabaseConfiguration
import dev.redicloud.event.EventManager
import dev.redicloud.logging.LogManager
import dev.redicloud.packets.PacketListener
import dev.redicloud.packets.PacketManager
import dev.redicloud.repository.java.version.JavaVersion
import dev.redicloud.repository.java.version.JavaVersionRepository
import dev.redicloud.repository.node.CloudNode
import dev.redicloud.repository.player.PlayerRepository
import dev.redicloud.repository.server.CloudServer
import dev.redicloud.repository.server.ServerRepository
import dev.redicloud.repository.server.version.CloudServerVersion
import dev.redicloud.repository.server.version.CloudServerVersionRepository
import dev.redicloud.repository.server.version.CloudServerVersionType
import dev.redicloud.repository.server.version.CloudServerVersionTypeRepository
import dev.redicloud.repository.server.version.handler.IServerVersionHandler
import dev.redicloud.repository.server.version.utils.ServerVersion
import dev.redicloud.repository.template.configuration.ConfigurationTemplate
import dev.redicloud.repository.template.configuration.ConfigurationTemplateRepository
import dev.redicloud.repository.template.file.FileTemplate
import dev.redicloud.repository.template.file.AbstractFileTemplateRepository
import dev.redicloud.service.base.packets.ServicePingPacket
import dev.redicloud.service.base.packets.ServicePingResponse
import dev.redicloud.service.base.packets.listener.CloudServiceShutdownPacketListener
import dev.redicloud.service.base.parser.*
import dev.redicloud.service.base.suggester.*
import dev.redicloud.service.base.utils.ClusterConfiguration
import dev.redicloud.tasks.CloudTaskManager
import dev.redicloud.utils.defaultScope
import dev.redicloud.utils.ioScope
import dev.redicloud.utils.service.ServiceId
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.redisson.api.RBucket
import kotlin.system.exitProcess

abstract class BaseService(
    databaseConfiguration: DatabaseConfiguration,
    _databaseConnection: DatabaseConnection?,
    val serviceId: ServiceId
) {

    companion object {
        val LOGGER = LogManager.logger(BaseService::class)
        var SHUTTINGDOWN = false
    }

    val databaseConnection: DatabaseConnection

    val nodeRepository: NodeRepository
    val serverVersionRepository: CloudServerVersionRepository
    abstract val fileTemplateRepository: AbstractFileTemplateRepository
    abstract val serverVersionTypeRepository: CloudServerVersionTypeRepository
    abstract val serverRepository: ServerRepository
    val configurationTemplateRepository: ConfigurationTemplateRepository
    val javaVersionRepository: JavaVersionRepository
    val playerRepository: PlayerRepository

    val packetManager: PacketManager
    val eventManager: EventManager
    val taskManager: CloudTaskManager
    val clusterConfiguration: ClusterConfiguration

    init {
        runBlocking {
            ServerVersion.loadIfNotLoaded()
        }
        databaseConnection = if (_databaseConnection != null && _databaseConnection.isConnected()) {
            _databaseConnection
        } else {
            DatabaseConnection(databaseConfiguration, serviceId, GsonCodec())
        }
        try {
            if (!databaseConnection.isConnected()) databaseConnection.connect()
        } catch (e: Exception) {
            LOGGER.severe("Failed to connect to database", e)
            exitProcess(-1)
        }

        clusterConfiguration = ClusterConfiguration(databaseConnection)

        packetManager = PacketManager(databaseConnection, serviceId)
        eventManager = EventManager("base-event-manager", packetManager)
        taskManager = CloudTaskManager(eventManager, packetManager)

        playerRepository = PlayerRepository(databaseConnection, eventManager)
        javaVersionRepository = JavaVersionRepository(serviceId, databaseConnection)
        nodeRepository = NodeRepository(databaseConnection, serviceId, packetManager, eventManager)
        serverVersionRepository = CloudServerVersionRepository(databaseConnection)
        configurationTemplateRepository = ConfigurationTemplateRepository(databaseConnection)
        this.registerPackets()
        this.registerPacketListeners()
    }

    fun registerDefaults() {
        this.registerDefaultParsers()
        this.registerDefaultSuggesters()
    }

    open fun shutdown() {
        SHUTTINGDOWN = true
        runBlocking {
            nodeRepository.shutdownAction.run()
            serverRepository.shutdownAction.run()
            taskManager.getTasks().forEach { it.cancel() }
            packetManager.disconnect()
            databaseConnection.disconnect()
            defaultScope.cancel()
            ioScope.cancel()
        }
    }

    private fun registerDefaultParsers() {
        CommandArgumentParser.PARSERS[CloudNode::class] = CloudNodeParser(this.nodeRepository)
        CommandArgumentParser.PARSERS[CloudServer::class] = CloudServerParser(this.serverRepository)
        CommandArgumentParser.PARSERS[CloudServerVersion::class] = CloudServerVersionParser(this.serverVersionRepository)
        CommandArgumentParser.PARSERS[CloudServerVersionType::class] = CloudServerVersionTypeParser(this.serverVersionTypeRepository)
        CommandArgumentParser.PARSERS[JavaVersion::class] = JavaVersionParser(this.javaVersionRepository)
        CommandArgumentParser.PARSERS[ServerVersion::class] = ServerVersionParser()
        CommandArgumentParser.PARSERS[ConfigurationTemplate::class] = ConfigurationTemplateParser(this.configurationTemplateRepository)
        CommandArgumentParser.PARSERS[IServerVersionHandler::class] = ServerVersionHandlerParser()
        CommandArgumentParser.PARSERS[FileTemplate::class] = FileTemplateParser(this.fileTemplateRepository)
    }

    private fun registerDefaultSuggesters() {
        ICommandSuggester.SUGGESTERS.add(RegisteredCloudNodeSuggester(this.nodeRepository))
        ICommandSuggester.SUGGESTERS.add(ConnectedCloudNodeSuggester(this.nodeRepository))
        ICommandSuggester.SUGGESTERS.add(CloudServerVersionSuggester(this.serverVersionRepository))
        ICommandSuggester.SUGGESTERS.add(CloudServerVersionTypeSuggester(this.serverVersionTypeRepository))
        ICommandSuggester.SUGGESTERS.add(ConfigurationTemplateSuggester(this.configurationTemplateRepository))
        ICommandSuggester.SUGGESTERS.add(JavaVersionSuggester(this.javaVersionRepository))
        ICommandSuggester.SUGGESTERS.add(ServerVersionSuggester())
        ICommandSuggester.SUGGESTERS.add(ServerVersionHandlerSuggester())
        ICommandSuggester.SUGGESTERS.add(FileTemplateSuggester(this.fileTemplateRepository))
        ICommandSuggester.SUGGESTERS.add(CloudServerSuggester(this.serverRepository))
        ICommandSuggester.SUGGESTERS.add(CloudConnectorFileNameSelector())
    }

    private fun registerPackets() {
        packetManager.registerPacket(ServicePingPacket::class)
        packetManager.registerPacket(ServicePingResponse::class)
        packetManager.registerPacket(CloudServiceShutdownPacket::class)
        packetManager.registerPacket(CloudServiceShutdownResponse::class)
    }

    private fun registerPacketListeners() {
        fun register(listener: PacketListener<*>) {
            packetManager.registerListener(listener)
        }
        register(CloudServiceShutdownPacketListener(this))
    }

}