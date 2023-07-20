package dev.redicloud.service.node.commands

import dev.redicloud.api.commands.*
import dev.redicloud.api.modules.ModuleLifeCycle
import dev.redicloud.console.commands.ConsoleActor
import dev.redicloud.modules.ModuleHandler
import dev.redicloud.modules.repository.ModuleWebRepository
import dev.redicloud.service.base.utils.ClusterConfiguration
import dev.redicloud.utils.defaultScope
import dev.redicloud.utils.gson.gson
import dev.redicloud.utils.toSymbol
import kotlinx.coroutines.launch

@Command("module")
@CommandDescription("Manage all modules")
class ModuleCommand(
    private val moduleHandler: ModuleHandler,
    private val clusterConfiguration: ClusterConfiguration
) : ICommand {

    @CommandSubPath("list")
    @CommandDescription("List all modules")
    fun list(actor: ConsoleActor) {
        val modules = moduleHandler.getModuleDatas()
        actor.sendHeader("Modules")
        actor.sendMessage("")
        actor.sendMessage("Loaded §8(%hc%${modules.filter { it.lifeCycle == ModuleLifeCycle.LOAD }.size}§8)")
        modules.filter { it.lifeCycle == ModuleLifeCycle.LOAD }.forEach {
            actor.sendMessage("§8-%hc%${it.description.name}%tc% §8(%tc%${it.description.version}§8)")
            actor.sendMessage("\t§8➥ %tc%${it.description.description} §8| %tc%${it.description.website}")
        }
        actor.sendMessage("")
        actor.sendMessage("Unloaded §8(%hc%${modules.filter { it.lifeCycle == ModuleLifeCycle.UNLOAD }.size}§8)")
        modules.filter { it.lifeCycle == ModuleLifeCycle.UNLOAD }.forEach {
            actor.sendMessage("§8-%hc%${it.description.name}%tc% §8(%tc%${it.description.version}§8)")
            actor.sendMessage("\t§8➥ %tc%${it.description.description} §8| %tc%${it.description.website}")
        }
        actor.sendMessage("")
        actor.sendHeader("Modules")
    }

    @CommandSubPath("load <id>")
    @CommandDescription("Load a module")
    fun load(
        actor: ConsoleActor,
        @CommandParameter("id") id: String
    ) {
        moduleHandler.detectModules()
        val description = moduleHandler.getModuleDescription(id)
        if (description == null) {
            actor.sendMessage("§cModule with id $id not found!")
            return
        }
        val data = moduleHandler.getModuleDatas().firstOrNull { it.description.id == id }
        if (data != null && data.loaded) {
            actor.sendMessage("§cModule with id $id is already loaded!")
            return
        }
        actor.sendMessage("Loading module %hc%${description.id}%tc%...")
        moduleHandler.loadModule(description.cachedFile!!)
    }

    @CommandSubPath("unload <id>")
    @CommandDescription("Unload a module")
    fun unload(
        actor: ConsoleActor,
        @CommandParameter("id") id: String
    ) {
        moduleHandler.detectModules()
        val data = moduleHandler.getModuleDatas().firstOrNull { it.description.id == id }
        if (data == null || !data.loaded) {
            actor.sendMessage("§cModule with id $id not found!")
            return
        }
        actor.sendMessage("Unloading module %hc%${data.id}%tc%...")
        moduleHandler.unloadModule(data)
    }

    @CommandSubPath("reload <id>")
    @CommandDescription("Reload a module")
    fun reload(
        actor: ConsoleActor,
        @CommandParameter("id") id: String
    ) {
        moduleHandler.detectModules()
        val data = moduleHandler.getModuleDatas().firstOrNull { it.description.id == id }
        if (data == null || !data.loaded) {
            actor.sendMessage("§cModule with id $id not found!")
            return
        }
        if (data.tasks.none { it.lifeCycle == ModuleLifeCycle.RELOAD }) {
            actor.sendMessage("§cModule with id $id can't be reloaded!")
            return
        }
        actor.sendMessage("Reloading module %hc%${data.id}%tc%...")
        moduleHandler.unloadModule(data)
        moduleHandler.loadModule(data.description.cachedFile!!)
    }

    @CommandSubPath("info <id>")
    @CommandDescription("Get info about a module")
    fun info(
        actor: ConsoleActor,
        @CommandParameter("id") id: String
    ) = defaultScope.launch {
        moduleHandler.detectModules()
        val data = moduleHandler.getModuleDatas().firstOrNull { it.description.id == id }
        if (data == null) {
            actor.sendMessage("§cModule with id $id not found!")
            return@launch
        }
        val targetRepository = moduleHandler.getRepository(data.id)
        actor.sendHeader("Module Info")
        actor.sendMessage("Name§8: %hc%${data.description.name}")
        actor.sendMessage("ID§8: %hc%${data.description.id}")
        actor.sendMessage("Repository§8: %hc%${targetRepository?.repoUrl ?: "None"}")
        actor.sendMessage("Update available§8: %hc%${(targetRepository?.isUpdateAvailable(data.description) ?: false).toSymbol()}")
        actor.sendMessage("Loaded§8: %hc%${data.loaded.toSymbol()}")
        actor.sendMessage("Version§8: %hc%${data.description.version}")
        actor.sendMessage("Description§8: %hc%${data.description.description}")
        actor.sendMessage("Website§8: %hc%${data.description.website}")
        actor.sendMessage("Author§8: %hc%${data.description.authors.joinToString("§8, %hc%")}")
        actor.sendMessage("Supported services§8: %hc%${data.description.mainClasses.keys.joinToString("§8, %hc%") { it.name }}")
        actor.sendHeader("Module Info")
    }

    @CommandSubPath("repository list")
    @CommandDescription("List all repositories")
    fun repositoriesList(
        actor: ConsoleActor
    ) {
        val repositoryUrls = clusterConfiguration.getList<String>("module-repositories")
        if (repositoryUrls.isEmpty()) {
            actor.sendMessage("§cNo repositories found!")
            return
        }
        actor.sendHeader("Repositories")
        actor.sendMessage("")
        repositoryUrls.forEach {
            actor.sendMessage("§8- %hc%$it")
        }
        actor.sendMessage("")
        actor.sendHeader("Repositories")
    }

    @CommandSubPath("repository add <url>")
    @CommandDescription("Add a repository")
    fun repositoriesAdd(
        actor: ConsoleActor,
        @CommandParameter("url") url: String
    ) {
        val repositoryUrls = clusterConfiguration.getList<String>("module-repositories").toMutableList()
        if (repositoryUrls.any { it.lowercase() == url.lowercase() }) {
            actor.sendMessage("§cRepository with url $url already exists!")
            return
        }
        try {
            ModuleWebRepository(url).repoUrl
            repositoryUrls.add(url)
            clusterConfiguration.set("module-repositories", gson.toJson(repositoryUrls))
            actor.sendMessage("§aRepository with url $url added!")
        }catch (e: Exception) {
            actor.sendMessage("§cRepository with url $url is not a valid repository!")
        }
    }

    @CommandSubPath("repository remove <url>")
    @CommandDescription("Remove a repository")
    fun repositoriesRemove(
        actor: ConsoleActor,
        @CommandParameter("url") url: String
    ) {
        val repositoryUrls = clusterConfiguration.getList<String>("module-repositories").toMutableList()
        if (!repositoryUrls.none { it.lowercase() == url.lowercase() }) {
            actor.sendMessage("§cRepository with url $url not found!")
            return
        }
        repositoryUrls.removeIf { it.lowercase() == url.lowercase() }
        clusterConfiguration.set("module-repositories", gson.toJson(repositoryUrls))
        actor.sendMessage("§aRepository with url $url removed!")
    }

}