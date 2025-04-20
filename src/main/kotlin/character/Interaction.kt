package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.quest.Quest
import net.mcquest.engine.quest.parseQuestId

abstract class Interaction {
    abstract fun isAvailable(pc: PlayerCharacter): Boolean

    abstract fun start(instance: Instance, position: Position)

    abstract fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean
}

class StartQuestInteraction(
    private val quest: Quest,
    private val dialogue: List<Component>
) : Interaction() {
    override fun isAvailable(pc: PlayerCharacter) =
        pc.questTracker.isReadyToStart(quest)

    override fun start(instance: Instance, position: Position) {
        instance.questStarts.put(position.toVector2(), quest)
    }

    override fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean {
        if (index < dialogue.size) {
            npc.speak(dialogue[index], pc)
            return true
        } else {
            pc.questTracker.startQuest(quest)
            return false
        }
    }
}

class TurnInQuestInteraction(
    private val quest: Quest,
    private val dialogue: List<Component>
) : Interaction() {
    override fun isAvailable(pc: PlayerCharacter) =
        pc.questTracker.isReadyToTurnIn(quest)

    override fun start(instance: Instance, position: Position) {
        instance.questTurnIns.put(position.toVector2(), quest)
    }

    override fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean {
        if (index < dialogue.size) {
            npc.speak(dialogue[index], pc)
            return true
        } else {
            pc.questTracker.completeQuest(quest)
            return false
        }
    }
}

fun deserializeInteraction(
    data: JsonNode,
    questsById: Map<String, Quest>
): Interaction = when (data["type"].asText()) {
    "start_quest" -> StartQuestInteraction(
        questsById.getValue(parseQuestId(data["quest"].asText())),
        data["dialogue"].map(JsonNode::asText)
            .map(MiniMessage.miniMessage()::deserialize)
    )

    "turn_in_quest" -> TurnInQuestInteraction(
        questsById.getValue(parseQuestId(data["quest"].asText())),
        data["dialogue"].map(JsonNode::asText)
            .map(MiniMessage.miniMessage()::deserialize)
    )

    else -> throw IllegalArgumentException("Unknown interaction type: ${data["type"]}")
}
