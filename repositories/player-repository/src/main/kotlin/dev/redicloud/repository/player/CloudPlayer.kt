package dev.redicloud.repository.player

import dev.redicloud.repository.server.version.utils.ServerVersion
import dev.redicloud.utils.service.ServiceId
import java.util.UUID

data class CloudPlayer(
    val uniqueId: UUID,
    var name: String,
    var serverId: ServiceId?,
    var proxyId: ServiceId?,
    var lastDisconnect: Long?,
    var lastConnect: Long?,
    var firstConnect: Long?,
    var version: ServerVersion
) {

    val connected: Boolean
        get() = serverId != null

}