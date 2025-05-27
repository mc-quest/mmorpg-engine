package net.mcquest.engine.ai.navigation

import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.util.fromMinestom
import net.mcquest.engine.util.toMinestom
import net.minestom.server.attribute.Attribute

class Navigator(private val character: NonPlayerCharacter) {
    val pathPosition: Vector3?
        get() = navigator.pathPosition?.let(Vector3::fromMinestom)

    private val navigator
        get() = character.entity.navigator

    private var lastStepTimeMillis = 0L

    fun setPathTo(target: Vector3, speed: Double): Boolean {
        character.entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = (speed / 20.0).toFloat()
        return navigator.setPathTo(target.toMinestom())
    }

    fun reset() = navigator.setPathTo(null)

    fun tick() {
        val stepSound = character.blueprint.stepSound
        if (pathPosition != null && character.isOnGround && stepSound != null) {
            val timeMillis = character.runtime.timeMillis
            if (timeMillis - lastStepTimeMillis > 600) { // TODO: delay should depend on speed
                character.emitSound(stepSound)
                lastStepTimeMillis = timeMillis
            }
        }
    }
}
