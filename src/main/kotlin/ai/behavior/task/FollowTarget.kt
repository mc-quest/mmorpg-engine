package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Position

private const val PATH_UPDATE_PERIOD_MILLIS = 500L

class FollowTarget(
    private val acceptanceRadius: Double,
    private val maxDistance: Double,
    private val speed: Double
) : Task() {
    private var lastPathUpdateMillis = 0L

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target

        if (target == null ||
            target.isInvisible ||
            target.instance != character.instance
        ) {
            // TODO: consider updating target every tick in NonPlayerCharacter
            //  based on whether target is dead, invisible, or in another instance
            character.navigator.reset()
            return BehaviorStatus.FAILURE
        }

        val sqrDistance = Position.sqrDistance(
            character.position,
            target.position
        )

        if (sqrDistance <= acceptanceRadius * acceptanceRadius) {
            character.navigator.reset()
            character.lookAt(target)
            return BehaviorStatus.SUCCESS
        }

        if (sqrDistance > maxDistance * maxDistance) {
            character.navigator.reset()
            return BehaviorStatus.FAILURE
        }

        if (character.runtime.timeMillis - lastPathUpdateMillis >=
            PATH_UPDATE_PERIOD_MILLIS
        ) {
            character.navigator.setPathTo(target.position.toVector3(), speed)
            lastPathUpdateMillis = character.runtime.timeMillis
        }

        return BehaviorStatus.RUNNING
    }
}

class FollowTargetBlueprint(
    private val acceptanceRadius: Double,
    private val maxDistance: Double,
    private val speed: Double
) : BehaviorBlueprint() {
    override fun create() = FollowTarget(acceptanceRadius, maxDistance, speed)
}

fun deserializeFollowTargetBlueprint(data: JsonNode) = FollowTargetBlueprint(
    data["acceptance_radius"].asDouble(),
    data["max_distance"].asDouble(),
    data["speed"].asDouble()
)
