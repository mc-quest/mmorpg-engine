package net.mcquest.engine.ai.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.*
import net.mcquest.engine.character.NonPlayerCharacter

class Sequence(children: List<Behavior>) : Composite(children) {
    private var currentChild = 0

    override fun start(character: NonPlayerCharacter) {
        currentChild = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (currentChild < children.size) {
            val status = children[currentChild].tick(character)
            if (status != BehaviorStatus.SUCCESS) {
                return status
            }
            currentChild++
        }
        return BehaviorStatus.SUCCESS
    }

    override fun stop(character: NonPlayerCharacter) {
        if (currentChild < children.size) {
            val child = children[currentChild]
            if (child.status == BehaviorStatus.RUNNING) {
                child.abort(character)
            }
        }
    }
}

class SequenceBlueprint(
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = Sequence(children.map(BehaviorBlueprint::create))
}

fun deserializeSequenceBlueprint(data: JsonNode) = SequenceBlueprint(
    data["children"].map(::deserializeBehaviorBlueprint)
)
