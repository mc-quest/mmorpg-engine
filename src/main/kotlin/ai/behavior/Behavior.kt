package com.shadowforgedmmo.engine.ai.behavior

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.composite.*
import com.shadowforgedmmo.engine.ai.behavior.decorator.*
import com.shadowforgedmmo.engine.ai.behavior.task.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

abstract class Behavior {
    var status: BehaviorStatus = BehaviorStatus.UNINITIALIZED
        private set

    fun tick(character: NonPlayerCharacter): BehaviorStatus {
        if (status != BehaviorStatus.RUNNING) start(character)
        status = update(character)
        if (status != BehaviorStatus.RUNNING) stop(character)
        return status
    }

    fun abort(character: NonPlayerCharacter) {
        status = BehaviorStatus.FAILURE
        stop(character)
    }

    protected open fun start(character: NonPlayerCharacter) = Unit

    protected abstract fun update(character: NonPlayerCharacter): BehaviorStatus

    protected open fun stop(character: NonPlayerCharacter) = Unit
}

abstract class Composite(protected val children: List<Behavior>) : Behavior() {
    override fun stop(character: NonPlayerCharacter) {
        for (child in children) {
            if (child.status == BehaviorStatus.RUNNING) {
                child.abort(character)
            }
        }
    }
}

abstract class Decorator(protected val child: Behavior) : Behavior() {
    override fun stop(character: NonPlayerCharacter) {
        if (child.status == BehaviorStatus.RUNNING) {
            child.abort(character)
        }
    }
}

abstract class Task : Behavior()

abstract class BehaviorBlueprint {
    abstract fun create(): Behavior
}

enum class BehaviorStatus {
    UNINITIALIZED,
    RUNNING,
    SUCCESS,
    FAILURE
}

fun deserializeBehaviorBlueprint(data: JsonNode): BehaviorBlueprint =
    when (data["type"].asText()) {
        "sequence" -> deserializeSequenceBlueprint(data)
        "selector" -> deserializeSelectorBlueprint(data)
        "simple_parallel" -> deserializeSimpleParallelBlueprint(data)
        "random_selector" -> deserializeRandomSelectorBlueprint(data)
        "inverter" -> deserializeInverterBlueprint(data)
        "loop" -> deserializeLoopBlueprint(data)
        "loop_forever" -> deserializeLoopForeverBlueprint(data)
        "wait" -> deserializeWaitBlueprint(data)
        "cooldown" -> deserializeCooldownReadyBlueprint(data)
        "go_to_random_position" -> deserializeGoToRandomPositionBlueprint(data)
        "set_velocity" -> deserializeSetVelocityBlueprint(data)
        "find_closest_target" -> deserializeFindClosestTargetBlueprint(data)
        "follow_target" -> deserializeFollowTargetBlueprint(data)
        "face_target" -> deserializeFaceTargetBlueprint(data)
        "look_at_target" -> deserializeLookAtTargetBlueprint(data)
        "target_is_in_range" -> deserializeTargetIsInRangeBlueprint(data)
        "target_is_to_right" -> deserializeTargetIsToRightBlueprint(data)
        "box_attack" -> deserializeBoxAttackBlueprint(data)
        "was_damaged" -> deserializeWasDamagedBlueprint(data)
        "emit_sound" -> deserializeEmitSoundBlueprint(data)
        "emit_particle" -> deserializeEmitParticleBlueprint(data)
        "play_animation" -> deserializePlayAnimationBlueprint(data)
        "follow_path" -> deserializeFollowPathBlueprint(data)
        else -> throw IllegalArgumentException()
    }
