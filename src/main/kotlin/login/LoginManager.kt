package net.mcquest.engine.login

import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.globalEventHandler
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import java.util.*

class LoginManager {
    private val instanceContainer: InstanceContainer = InstanceContainer(
        UUID.randomUUID(), DimensionType.OVERWORLD
    )

    fun start(runtime: Runtime) {
        MinecraftServer.getInstanceManager().registerInstance(instanceContainer)

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            handlePlayerConfigure(runtime, event)
        }
        globalEventHandler.addListener(PlayerSpawnEvent::class.java, ::handlePlayerSpawn)
    }

    private fun handlePlayerConfigure(runtime: Runtime, event: AsyncPlayerConfigurationEvent) {
        event.spawningInstance = instanceContainer
        event.player.gameMode = GameMode.ADVENTURE
        event.player.sendResourcePacks(
            ResourcePackRequest.addingRequest(
                ResourcePackInfo.resourcePackInfo(
                    UUID.randomUUID(),
                    runtime.config.resourcePackUri,
                    runtime.config.resourcePackHash
                )
            )
        )
    }

    private fun handlePlayerSpawn(event: PlayerSpawnEvent) {
        event.player.isAllowFlying = true // TODO: remove this
        if (event.instance != instanceContainer) return
    }
}
