package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.mcquest.engine.combat.getNearbyPlayerCharacters
import net.mcquest.engine.math.Position
import net.mcquest.engine.music.Song
import net.mcquest.engine.music.parseSongId

private const val LEAVE_OFFSET = 5.0

class BossFight(
    private val radius: Double,
    private val music: Song
) {
    private val viewers = mutableSetOf<PlayerCharacter>()
    private val bossBar = BossBar.bossBar(
        Component.empty(),
        1.0F,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_12
    )

    fun init(character: NonPlayerCharacter) {
        bossBar.name(character.displayNameWithLevel(Stance.HOSTILE))
    }

    fun tick(character: NonPlayerCharacter) {
        bossBar.progress((character.health / character.maxHealth).toFloat())

        viewers.filter {
            it.removed || Position.sqrDistance(
                it.position,
                character.position
            ) > (radius + LEAVE_OFFSET) * (radius + LEAVE_OFFSET)
        }.forEach(::removeViewer)

        getNearbyPlayerCharacters(
            character.runtime.gameObjectManager,
            character.instance,
            character.position.toVector3(),
            radius
        )
            .filterNot(viewers::contains)
            .filter { character.getStance(it) == Stance.HOSTILE }
            .forEach(::addViewer)
    }

    private fun addViewer(viewer: PlayerCharacter) {
        viewers.add(viewer)
        bossBar.addViewer(viewer.entity)
        viewer.musicPlayer.setSong(music)
    }

    private fun removeViewer(viewer: PlayerCharacter) {
        viewers.remove(viewer)
        bossBar.removeViewer(viewer.entity)
        viewer.musicPlayer.setSong(viewer.zone.music)
    }

    fun remove() {
        viewers.toList().forEach(::removeViewer)
    }
}

class BossFightBlueprint(
    private val radius: Double,
    private val music: Song
) {
    fun create() = BossFight(radius, music)
}

fun deserializeBossFightBlueprint(data: JsonNode, musicById: Map<String, Song>) =
    BossFightBlueprint(
        data["radius"].asDouble(),
        musicById.getValue(parseSongId(data["music"].asText()))
    )
