package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.music.parseSongId

private const val LEAVE_OFFSET = 5.0

class BossFight(
    val character: NonPlayerCharacter,
    val radius: Double,
    val music: Song
) {
    private val viewers = mutableSetOf<PlayerCharacter>()
    private val bossBar = BossBar.bossBar(
        Component.empty(),
        1.0F,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_12
    )

    fun init() {
        bossBar.name(character.displayNameWithLevel(Stance.HOSTILE))
    }

    fun tick() {
        bossBar.progress((character.health / character.maxHealth).toFloat())

        viewers.filter {
            it.removed || Position.sqrDistance(
                it.position,
                character.position
            ) > (radius + LEAVE_OFFSET) * (radius + LEAVE_OFFSET)
        }.forEach(::removeViewer)

        character.instance
            .getNearbyObjects<PlayerCharacter>(character.position.toVector3(), radius)
            .filterNot(viewers::contains)
            .filter { character.getStance(it) == Stance.HOSTILE }
            .forEach(::addViewer)
    }

    private fun addViewer(viewer: PlayerCharacter) {
        viewers.add(viewer)
        bossBar.addViewer(viewer.entity)
        viewer.bossFights.add(this)
        viewer.updateMusic()
    }

    private fun removeViewer(viewer: PlayerCharacter) {
        viewers.remove(viewer)
        bossBar.removeViewer(viewer.entity)
        viewer.bossFights.remove(this)
        viewer.updateMusic()
    }

    fun remove() {
        viewers.toList().forEach(::removeViewer)
    }
}

class BossFightBlueprint(
    private val radius: Double,
    private val music: Song
) {
    fun create(character: NonPlayerCharacter) = BossFight(character, radius, music)
}

fun deserializeBossFightBlueprint(data: JsonNode, musicById: Map<String, Song>) =
    BossFightBlueprint(
        data["radius"].asDouble(),
        musicById.getValue(parseSongId(data["music"].asText()))
    )
