package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.playerclass.PlayerClass

class QuestRewards(
    val experience: Int,
    val currency: Int,
    val items: List<QuestRewardItem>
)

class QuestRewardItem(
    val item: Item,
    val quantity: Int,
    val playerClasses: Collection<PlayerClass>?
)

fun deserializeQuestRewards(
    data: JsonNode,
    itemsById: Map<String, Item>,
    playerClassesById: Map<String, PlayerClass>
) = QuestRewards(
    data["experience"]?.asInt() ?: 0,
    data["currency"]?.asInt() ?: 0,
    data["items"]?.map {
        deserializeQuestRewardItem(
            it,
            itemsById,
            playerClassesById
        )
    } ?: emptyList()
)

fun deserializeQuestRewardItem(
    data: JsonNode,
    itemsById: Map<String, Item>,
    playerClassesById: Map<String, PlayerClass>
) = QuestRewardItem(
    itemsById.getValue(data["item"].asText()),
    data["quantity"].asInt(),
    data["player_classes"]?.map { playerClassesById.getValue(it.asText()) }
)
