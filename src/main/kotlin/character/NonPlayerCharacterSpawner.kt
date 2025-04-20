package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.deserializePosition
import net.mcquest.engine.runtime.Runtime

class NonPlayerCharacterSpawner(
    position: Position,
    val blueprint: CharacterBlueprint,
    val summoner: Character? = null
) : GameObjectSpawner(position) {
    override fun start(instance: Instance) =
        blueprint.interactions.forEach { it.start(instance, position) }

    override fun spawn(instance: Instance, runtime: Runtime) =
        NonPlayerCharacter(this, instance, runtime)
}

fun deserializeNonPlayerCharacterSpawners(
    data: JsonNode,
    characterBlueprintsById: Map<String, CharacterBlueprint>
): Collection<NonPlayerCharacterSpawner> {
    val blueprint = characterBlueprintsById.getValue(parseCharacterBlueprintId(data["character"].asText()))
    return if (data.has("position")) {
        listOf(NonPlayerCharacterSpawner(deserializePosition(data["position"]), blueprint))
    } else if (data.has("positions")) {
        data["positions"].map { NonPlayerCharacterSpawner(deserializePosition(it), blueprint) }
    } else {
        throw IllegalArgumentException()
    }
}
