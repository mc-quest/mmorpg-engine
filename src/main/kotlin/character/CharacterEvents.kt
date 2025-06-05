package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.globalEventHandler
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent

class CharacterEvents(runtime: Runtime) {
    fun start() {
        globalEventHandler.addListener(PlayerEntityInteractEvent::class.java, ::handlePlayerEntityInteract)
        globalEventHandler.addListener(PlayerDisconnectEvent::class.java, ::handlePlayerDisconnect)
    }

    fun handlePlayerEntityInteract(event: PlayerEntityInteractEvent) {
        PlayerCharacter.fromEntity(event.player)?.handleEntityInteract(event)
    }

    fun handlePlayerDisconnect(event: PlayerDisconnectEvent) =
        PlayerCharacter.fromEntity(event.player)?.remove()
}
