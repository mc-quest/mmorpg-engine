package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Path
import net.mcquest.engine.math.Vector3

private const val ACCEPTANCE_RADIUS = 1.0

class FollowPath(
    path: Path,
    private val loop: Boolean
) : Task() {
    private val path: Path

    init {
//        if (reverse) {
//            this.path = Path(path.corners + path.corners.dropLast(1).reversed())
//        } else {
//            this.path = path
//        }
        this.path = path
    }

    override fun start(character: NonPlayerCharacter) {

    }

    fun nextPointIndex(character: NonPlayerCharacter): Int {
        val position = character.position.toVector3()
        return 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val pathPosition = character.navigator.pathPosition
            ?: return BehaviorStatus.FAILURE
        return if (Vector3.sqrDistance(
                character.position.toVector3(),
                pathPosition
            ) <= ACCEPTANCE_RADIUS * ACCEPTANCE_RADIUS
        )
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.RUNNING
    }

    override fun stop(character: NonPlayerCharacter) {
        character.navigator.reset()
    }
}

class FollowPathBlueprint() : BehaviorBlueprint() {
    override fun create() = FollowPath(Path(listOf()), false)
}

fun deserializeFollowPathBlueprint(data: JsonNode) = FollowPathBlueprint()
