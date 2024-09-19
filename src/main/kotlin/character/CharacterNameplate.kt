package net.mcquest.engine.character

import net.mcquest.engine.combat.getNearbyPlayerCharacters
import net.mcquest.engine.entity.Hologram
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.util.toMinestom
import java.util.*

private const val SHOW_DISTANCE = 32.0
private const val HIDE_DISTANCE = 36.0

class CharacterNameplate {
    private val namesByStance: MutableMap<Stance, Hologram> =
        EnumMap(Stance::class.java)
    private val healthBar = Hologram()
    private val viewers = mutableSetOf<PlayerCharacter>()

    init {
        for (stance in Stance.entries) {
            val name = Hologram()
            name.isAutoViewable = false
            namesByStance[stance] = name
        }
        healthBar.isAutoViewable = false
    }

    fun updatePosition(character: Character) {
        val namePosition = namePosition(character).toMinestom()
        namesByStance.values.forEach {
            it.teleport(namePosition)
        }
        healthBar.teleport(healthBarPosition(character).toMinestom())
    }

    fun updateInstance(character: Character) {
        val namePosition = namePosition(character).toMinestom()
        namesByStance.values.forEach {
            it.setInstance(character.instance.instanceContainer, namePosition).join()
        }
        healthBar.setInstance(
            character.instance.instanceContainer,
            healthBarPosition(character).toMinestom()
        ).join()
    }

    fun updateNames(character: Character) =
        namesByStance.forEach { (stance, name) ->
            name.text = character.displayNameWithLevel(stance)
        }

    fun updateHealthBar(character: Character) {
        healthBar.text = character.healthBarText()
    }

    fun spawn(character: Character) {
        namesByStance.forEach { (stance, name) ->
            name.text = character.displayNameWithLevel(stance)
            name.isAutoViewable = false
            name.setInstance(
                character.instance.instanceContainer,
                namePosition(character).toMinestom()
            ).join()
        }

        healthBar.text = character.healthBarText()
        healthBar.setInstance(
            character.instance.instanceContainer,
            healthBarPosition(character).toMinestom()
        ).join()
    }

    fun tick(character: Character) {
        viewers.filter {
            it.removed || Position.sqrDistance(
                it.position,
                character.position
            ) > HIDE_DISTANCE * HIDE_DISTANCE
        }.forEach { removeViewer(it) }

        getNearbyPlayerCharacters(
            character.runtime.gameObjectManager,
            character.instance,
            character.position.toVector3(),
            SHOW_DISTANCE
        )
            .filterNot(viewers::contains)
            .filter { it != character }
            .forEach { addViewer(character, it) }
    }

    private fun addViewer(character: Character, viewer: PlayerCharacter) {
        viewers.add(viewer)
        namesByStance.getValue(character.getStance(viewer)).addViewer(viewer.entity)
        healthBar.addViewer(viewer.entity)
    }

    private fun removeViewer(viewer: PlayerCharacter) {
        viewers.remove(viewer)
        namesByStance.values.forEach { it.removeViewer(viewer.entity) }
        healthBar.removeViewer(viewer.entity)
    }

    fun despawn() {
        namesByStance.values.forEach(Hologram::remove)
        healthBar.remove()
    }
}

private fun namePosition(character: Character) =
    character.position + Vector3.UP * (character.height + 0.25)

private fun healthBarPosition(character: Character) =
    character.position + Vector3.UP * character.height
