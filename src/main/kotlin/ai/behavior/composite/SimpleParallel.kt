package net.mcquest.engine.ai.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.*
import net.mcquest.engine.character.NonPlayerCharacter

class SimpleParallel(
    primary: Behavior,
    secondary: Behavior
) : Composite(listOf(primary, secondary)) {
    private val primary
        get() = children[0]

    private val secondary
        get() = children[1]

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val status = primary.tick(character)
        secondary.tick(character)
        return status
    }
}

class SimpleParallelBlueprint(
    private val primary: BehaviorBlueprint,
    private val secondary: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = SimpleParallel(primary.create(), secondary.create())
}

fun deserializeSimpleParallelBlueprint(data: JsonNode) = SimpleParallelBlueprint(
    deserializeBehaviorBlueprint(data["primary"]),
    deserializeBehaviorBlueprint(data["secondary"])
)
