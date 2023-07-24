package dev.redicloud.repository.server.version.serverversion

import com.google.gson.reflect.TypeToken
import dev.redicloud.api.version.IServerVersion
import dev.redicloud.api.version.IVersionRepository
import dev.redicloud.utils.getTextOfAPIWithFallback
import dev.redicloud.utils.gson.gson

object VersionRepository : IVersionRepository {

    val mcVersionRegex = Regex("(\\d+(\\.\\d+)+)")

    val cachedVersions = mutableListOf<ServerVersion>() //TODO private add

    val versionComparator = compareBy<IServerVersion> { if (it.latest) 1000 else it.protocolId }
        .thenByDescending { it.name.substringBefore('.') }
        .thenByDescending { it.name.substringAfter('.').substringBefore('.') }
        .thenByDescending { it.name.substringAfterLast('.') }
    override fun versions(): MutableList<ServerVersion> = cachedVersions

    override suspend fun loadOnlineVersions() {
        cachedVersions.clear()
        val json = getTextOfAPIWithFallback("api-files/server-versions.json")
        val type = object : TypeToken<ArrayList<ServerVersion>>() {}.type
        val list = gson.fromJson<List<ServerVersion>?>(json, type).toMutableList()
        if (list.none { it.unknown }) {
            list.add(ServerVersion("unknown", -1, emptyArray()))
        }
        if (list.none { it.latest }) {
            list.add(ServerVersion("latest", -1, emptyArray()))
        }
        cachedVersions.addAll(list)
    }

    override suspend fun loadIfNotLoaded() {
        if (cachedVersions.isNotEmpty()) return
        loadOnlineVersions()
    }

    override fun parse(s: String, strict: Boolean): ServerVersion? {
        val t = if (strict) s else s.lowercase().split("-")[0]
        return versions().firstOrNull { it.name.lowercase() == t }
    }

}