package com.shadowforgedmmo.engine.ai.behavior.decorator

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class Inverter(child: Behavior) : Decorator(child) {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        return when (child.tick(character)) {
            BehaviorStatus.SUCCESS -> BehaviorStatus.FAILURE
            BehaviorStatus.FAILURE -> BehaviorStatus.SUCCESS
            else -> status
        }
    }
}

class InverterBlueprint(
    private val child: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = Inverter(child.create())
}

fun deserializeInverterBlueprint(data: JsonNode) = InverterBlueprint(
    deserializeBehaviorBlueprint(data["child"])
)
