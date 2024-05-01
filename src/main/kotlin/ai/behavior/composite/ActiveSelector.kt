package net.mcquest.engine.ai.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.*
import net.mcquest.engine.character.NonPlayerCharacter

class ActiveSelector(children: List<Behavior>) : Composite(children) {
    private var currentChild = 0

    override fun start(character: NonPlayerCharacter) {
        currentChild = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val prevChild = currentChild
        currentChild = 0

        var status = BehaviorStatus.FAILURE

        while (currentChild < children.size) {
            val childStatus = children[currentChild].tick(character)

            if (childStatus != BehaviorStatus.FAILURE) {
                status = childStatus
                break
            }

            currentChild++
        }

        if (prevChild != children.size && currentChild != prevChild) {
            children[prevChild].abort(character)
        }

        return status
    }
}

class ActiveSelectorBlueprint(
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = ActiveSelector(children.map(BehaviorBlueprint::create))
}

fun deserializeActiveSelectorBlueprint(data: JsonNode) =
    ActiveSelectorBlueprint(data["children"].map(::deserializeBehaviorBlueprint))
