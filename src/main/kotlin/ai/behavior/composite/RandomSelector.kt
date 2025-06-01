package com.shadowforgedmmo.engine.ai.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.weightedRandomIndex

class RandomSelector(
    private val weights: List<Double>,
    children: List<Behavior>
) : Composite(children) {
    private var currentChild = 0

    init {
        require(weights.all { it >= 0.0 })
        require(children.size == weights.size)
    }

    override fun start(character: NonPlayerCharacter) {
        currentChild = weightedRandomIndex(weights)
    }

    override fun update(character: NonPlayerCharacter) =
        children[currentChild].tick(character)
}

class RandomSelectorBlueprint(
    private val weights: List<Double>,
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = RandomSelector(
        weights,
        children.map(BehaviorBlueprint::create)
    )
}

fun deserializeRandomSelectorBlueprint(data: JsonNode) = RandomSelectorBlueprint(
    data["weights"].map(JsonNode::asDouble),
    data["children"].map(::deserializeBehaviorBlueprint)
)
