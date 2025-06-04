package com.shadowforgedmmo.engine.entity

import com.shadowforgedmmo.engine.model.Skin
import net.minestom.server.entity.*
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket

class EntityHuman(private val skin: Skin) : EntityCreature(EntityType.PLAYER) {
    override fun updateNewViewer(player: Player) {
        val properties = listOf(
            PlayerInfoUpdatePacket.Property(
                "textures",
                skin.textures,
                skin.signature
            )
        )
        val entry = PlayerInfoUpdatePacket.Entry(
            uuid,
            "",
            properties,
            false,
            0,
            GameMode.ADVENTURE,
            null,
            null,
            0
        )
        player.sendPacket(
            PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                entry
            )
        )

        // Spawn the player entity
        super.updateNewViewer(player)

        // Enable skin layers
        player.sendPackets(
            EntityMetaDataPacket(
                entityId,
                mapOf(17 to Metadata.Byte(127.toByte()))
            )
        )
    }

    override fun updateOldViewer(player: Player) {
        super.updateOldViewer(player)

        player.sendPacket(PlayerInfoRemovePacket(uuid))
    }
}
