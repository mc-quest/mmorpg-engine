package net.mcquest.engine.ai.behavior

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.composite.*
import net.mcquest.engine.ai.behavior.decorator.*
import net.mcquest.engine.ai.behavior.task.*
import net.mcquest.engine.character.NonPlayerCharacter

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
        "active_selector" -> deserializeActiveSelectorBlueprint(data)
        "simple_parallel" -> deserializeSimpleParallelBlueprint(data)
        "random_selector" -> deserializeRandomSelectorBlueprint(data)
        "inverter" -> deserializeInverterBlueprint(data)
        "loop" -> deserializeLoopBlueprint(data)
        "loop_forever" -> deserializeLoopForever(data)
        "wait" -> deserializeWaitBlueprint(data)
        "cooldown" -> deserializeCooldownReadyBlueprint(data)
        "go_to_random_position" -> deserializeGoToRandomPositionBlueprint(data)
        "set_velocity" -> deserializeSetVelocity(data)
        "find_closest_target" -> deserializeFindClosestTarget(data)
        "follow_target" -> deserializeFollowTarget(data)
        "face_target" -> deserializeFaceTargetBlueprint(data)
        "look_at_target" -> deserializeLookAtTargetBlueprint(data)
        "target_is_within_distance" -> deserializeTargetIsWithinDistance(data)
        "target_is_to_right" -> deserializeTargetIsToRightBlueprint(data)
        "melee_attack" -> deserializeMeleeAttack(data)
        "emit_sound" -> deserializeEmitSound(data)
        "play_animation" -> deserializePlayAnimation(data)
        else -> throw IllegalArgumentException()
    }
