package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.globalEventHandler
import net.minestom.server.event.player.PlayerDisconnectEvent

class CharacterEvents(runtime: Runtime) {
    fun start() {
        globalEventHandler.addListener(PlayerDisconnectEvent::class.java, ::handlePlayerDisconnect)
    }

    fun handlePlayerDisconnect(event: PlayerDisconnectEvent) =
        GameObject.fromEntity(event.player)?.let(GameObject::remove)
}
