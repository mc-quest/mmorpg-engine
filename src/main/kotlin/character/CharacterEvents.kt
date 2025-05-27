package net.mcquest.engine.character

import net.mcquest.engine.gameobject.GameObject
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.globalEventHandler
import net.minestom.server.event.player.PlayerDisconnectEvent

class CharacterEvents(runtime: Runtime) {
    fun start() {
        globalEventHandler.addListener(PlayerDisconnectEvent::class.java, ::handlePlayerDisconnect)
    }

    fun handlePlayerDisconnect(event: PlayerDisconnectEvent) =
        GameObject.fromEntity(event.player)?.let(GameObject::remove)
}
