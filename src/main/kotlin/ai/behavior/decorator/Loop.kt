package com.shadowforgedmmo.engine.ai.behavior.decorator

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class Loop(child: Behavior, private val count: Int) : Decorator(child) {
    private var i = 0

    override fun start(character: NonPlayerCharacter) {
        i = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (i < count) {
            val status = child.tick(character)
            if (status != BehaviorStatus.SUCCESS) {
                return status
            }
            i++
        }
        return BehaviorStatus.SUCCESS
    }
}

class LoopBlueprint(
    private val child: BehaviorBlueprint,
    private val count: Int
) : BehaviorBlueprint() {
    override fun create() = Loop(child.create(), count)
}

fun deserializeLoopBlueprint(data: JsonNode) = LoopBlueprint(
    deserializeBehaviorBlueprint(data["child"]),
    data["count"].asInt()
)
