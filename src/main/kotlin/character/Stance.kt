package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class Stances(
    private val default: Stance,
    private val friendlyIds: Collection<String>,
    private val neutralIds: Collection<String>,
    private val hostileIds: Collection<String>
) {
    fun stance(toward: Character): Stance {
        val id = if (toward is NonPlayerCharacter) {
            "characters:${toward.blueprint.id}"
        } else {
            "player"
        }
        return when {
            friendlyIds.contains(id) -> Stance.FRIENDLY
            neutralIds.contains(id) -> Stance.NEUTRAL
            hostileIds.contains(id) -> Stance.HOSTILE
            else -> default
        }
    }
}

enum class Stance(val color: TextColor) {
    FRIENDLY(NamedTextColor.GREEN),
    NEUTRAL(NamedTextColor.YELLOW),
    HOSTILE(NamedTextColor.RED)
}

fun deserializeStances(data: JsonNode) = Stances(
    data["default"]?.let { Stance.valueOf(it.asText().uppercase()) }
        ?: Stance.FRIENDLY,
    data["friendly"]?.map(JsonNode::asText) ?: emptyList(),
    data["neutral"]?.map(JsonNode::asText) ?: emptyList(),
    data["hostile"]?.map(JsonNode::asText) ?: emptyList()
)
