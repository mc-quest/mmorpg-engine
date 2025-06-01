package com.shadowforgedmmo.engine.ai.behavior.decorator

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class LoopForever(child: Behavior) : Decorator(child) {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (true) {
            val status = child.tick(character)

            if (status != BehaviorStatus.SUCCESS) {
                return status
            }
        }
    }
}

class LoopForeverBlueprint(
    private val child: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = LoopForever(child.create())
}

fun deserializeLoopForeverBlueprint(data: JsonNode) = LoopForeverBlueprint(
    deserializeBehaviorBlueprint(data["child"])
)
