package com.shadowforgedmmo.engine.quest

import com.google.common.collect.ArrayListMultimap
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.item.QuestItem

class QuestObjectiveManager {
    private val slayObjectivesByCharacterBlueprintId =
        ArrayListMultimap.create<String, Pair<Quest, Int>>()
    private val collectItemObjectivesByItemBlueprintId =
        ArrayListMultimap.create<String, Pair<Quest, Int>>()

    fun registerSlayObjective(
        quest: Quest,
        objectiveIndex: Int,
        characterBlueprintId: String
    ) {
        slayObjectivesByCharacterBlueprintId.put(
            characterBlueprintId,
            quest to objectiveIndex
        )
    }

    fun registerItemCollectObjective(
        quest: Quest,
        objectiveIndex: Int,
        itemBlueprintId: String
    ) {
        collectItemObjectivesByItemBlueprintId.put(
            itemBlueprintId,
            quest to objectiveIndex
        )
    }

    fun handleCharacterDeath(killer: PlayerCharacter, killed: NonPlayerCharacter) {
        slayObjectivesByCharacterBlueprintId[killed.blueprint.id]
            .firstOrNull() { (quest, objectiveIndex) ->
                killer.questTracker.isInProgress(quest, objectiveIndex)
            }?.let { (quest, objectiveIndex) ->
                killer.questTracker.incrementProgress(quest, objectiveIndex)
            }
    }

    fun handleItemPickup(player: PlayerCharacter, item: QuestItem) {
        collectItemObjectivesByItemBlueprintId[item.id]
            .first { (quest, objectiveIndex) ->
                player.questTracker.isInProgress(quest, objectiveIndex)
            }.let { (quest, objectiveIndex) ->
                player.questTracker.incrementProgress(quest, objectiveIndex)
            }
    }
}
