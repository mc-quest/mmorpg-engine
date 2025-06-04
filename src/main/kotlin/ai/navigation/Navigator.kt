package com.shadowforgedmmo.engine.ai.navigation

import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.util.fromMinestom
import com.shadowforgedmmo.engine.util.toMinestom
import net.minestom.server.entity.attribute.Attribute

class Navigator(private val character: NonPlayerCharacter) {
    val pathPosition: Vector3?
        get() = navigator.pathPosition?.let(Vector3::fromMinestom)

    private val navigator
        get() = character.entity.navigator

    private var lastStepTimeMillis = 0L

    fun setPathTo(target: Vector3, speed: Double): Boolean {
        character.entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = speed / 20.0
        return navigator.setPathTo(target.toMinestom())
    }

    fun reset() = navigator.setPathTo(null)

    fun tick() {
        val stepSound = character.blueprint.stepSound
        if (!navigator.isComplete && character.isOnGround && stepSound != null) {
            val timeMillis = character.runtime.timeMillis
            if (timeMillis - lastStepTimeMillis > 600) { // TODO: delay should depend on speed
                character.emitSound(stepSound)
                lastStepTimeMillis = timeMillis
            }
        }
    }
}
