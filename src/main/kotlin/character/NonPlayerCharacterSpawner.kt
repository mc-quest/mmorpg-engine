package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.deserializePosition
import net.mcquest.engine.runtime.Runtime

class NonPlayerCharacterSpawner(
    instance: Instance,
    position: Position,
    val blueprint: CharacterBlueprint
) : GameObjectSpawner(instance, position) {
    override fun start(runtime: Runtime) {
        // TODO: quest markers
    }

    override fun spawn(runtime: Runtime) = NonPlayerCharacter(runtime, this)
}

fun deserializeNonPlayerCharacterSpawner(
    data: JsonNode,
    instance: Instance,
    characterBlueprintsById: Map<String, CharacterBlueprint>
) = NonPlayerCharacterSpawner(
    instance,
    deserializePosition(data["position"]),
    characterBlueprintsById.getValue(
        parseCharacterBlueprintId(data["character"].asText())
    )
)
