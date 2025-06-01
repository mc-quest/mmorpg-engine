package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.entity.Hologram
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.util.toMinestom
import java.util.*
import kotlin.math.pow

private const val SHOW_DISTANCE = 32.0
private const val HIDE_DISTANCE = 36.0

class CharacterNameplate(
    private val character: Character
) {
    private val namesByStance: MutableMap<Stance, Hologram> = EnumMap(Stance::class.java)
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

    fun updateHealthBar() {
        healthBar.text = character.healthBarText()
    }

    fun spawn() {
        namesByStance.forEach { (stance, name) ->
            name.text = character.displayNameWithLevel(stance)
            name.isAutoViewable = false
            name.setInstance(
                character.instance.instanceContainer,
                namePosition().toMinestom()
            ).join()
        }

        healthBar.text = character.healthBarText()
        healthBar.setInstance(
            character.instance.instanceContainer,
            healthBarPosition().toMinestom()
        ).join()
    }

    fun tick() {
        updatePosition()
        updateViewers()
    }

    private fun updatePosition() {
        val namePosition = namePosition().toMinestom()
        val characterInstanceContainer = character.instance.instanceContainer
        if (characterInstanceContainer != healthBar.instance) {
            namesByStance.values.forEach {
                it.setInstance(characterInstanceContainer, namePosition).join()
            }
            healthBar.setInstance(characterInstanceContainer, healthBarPosition().toMinestom()).join()
        } else {
            namesByStance.values.forEach {
                it.teleport(namePosition).join()
            }
            healthBar.teleport(healthBarPosition().toMinestom()).join()
        }
    }

    private fun updateViewers() {
        viewers.filter {
            it.removed || it.instance != character.instance || Position.sqrDistance(
                it.position,
                character.position
            ) > HIDE_DISTANCE.pow(2)
        }.forEach(::removeViewer)

        character.instance.getNearbyObjects<PlayerCharacter>(
            character.position.toVector3(),
            SHOW_DISTANCE
        )
            .filterNot(viewers::contains)
            .filter { it != character }
            .forEach(::addViewer)
    }

    private fun addViewer(viewer: PlayerCharacter) {
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

    private fun namePosition() =
        character.position + Vector3.UP * (character.height + 0.25)

    private fun healthBarPosition() =
        character.position + Vector3.UP * character.height
}
