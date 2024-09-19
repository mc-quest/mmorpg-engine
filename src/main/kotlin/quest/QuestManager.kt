package net.mcquest.engine.quest

import com.google.common.collect.ArrayListMultimap
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.item.QuestItem
import net.mcquest.engine.runtime.Runtime

class QuestManager(quests: Collection<Quest>) {
    private val questsById = quests.associateBy(Quest::id)
    private val slayObjectivesByCharacterBlueprintId =
        ArrayListMultimap.create<String, Pair<Quest, Int>>()
    private val collectItemObjectivesByItemBlueprintId =
        ArrayListMultimap.create<String, Pair<Quest, Int>>()

    fun start(runtime: Runtime) {
        for (quest in questsById.values) {
            quest.start(runtime)
        }
    }

    fun getQuest(id: String) = questsById.getValue(id)

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
            .first { (quest, objectiveIndex) ->
                killer.questTracker.isInProgress(quest, objectiveIndex)
            }.let { (quest, objectiveIndex) ->
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
