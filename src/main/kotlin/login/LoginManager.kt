package net.mcquest.engine.login

import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.character.PlayerCharacterSpawner
import net.mcquest.engine.math.Position
import net.mcquest.engine.persistence.PlayerCharacterData
import net.mcquest.engine.persistence.QuestTrackerData
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.globalEventHandler
import net.mcquest.engine.util.toMinestom
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import java.util.*

class LoginManager(val runtime: Runtime) {
    private val instanceContainer: InstanceContainer = InstanceContainer(
        UUID.randomUUID(), DimensionType.OVERWORLD
    )

    fun start() {
        MinecraftServer.getInstanceManager().registerInstance(instanceContainer)

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::handlePlayerConfigure)
        globalEventHandler.addListener(PlayerSpawnEvent::class.java, ::handlePlayerSpawn)
    }

    private fun handlePlayerConfigure(event: AsyncPlayerConfigurationEvent) {
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
        if (event.instance != instanceContainer) return

        val data = PlayerCharacterData(
            "eladrador",
            Position(2864.0, 73.0, 3598.0),
            5.0,
            5.0,
            QuestTrackerData(),
            "ashen_tangle"
        )
        event.player.respawnPoint = data.position.toMinestom()
        val instance = runtime.instancesById.getValue(data.instanceId)
        val spawner = PlayerCharacterSpawner(data.position, event.player, data)
        val pc = instance.spawn(spawner, runtime) as PlayerCharacter
    }
}
