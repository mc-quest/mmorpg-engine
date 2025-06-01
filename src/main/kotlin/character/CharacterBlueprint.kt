package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.deserializeBehaviorBlueprint
import com.shadowforgedmmo.engine.loot.LootTable
import com.shadowforgedmmo.engine.loot.deserializeLootTable
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.model.Skin
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.script.parseScriptId
import com.shadowforgedmmo.engine.sound.deserializeSound
import com.shadowforgedmmo.engine.time.secondsToDuration
import java.time.Duration

class CharacterBlueprint(
    val id: String,
    val name: String,
    val level: Int,
    val maxHealth: Double,
    val mass: Double,
    val experiencePoints: Int,
    val model: CharacterModel,
    val bossFight: BossFightBlueprint?,
    val behavior: BehaviorBlueprint?,
    val stances: Stances,
    val stepSound: Sound?,
    val speakSound: Sound?,
    val hurtSound: Sound?,
    val deathSound: Sound?,
    val interactions: List<Interaction>,
    val lootTable: LootTable?,
    val scriptId: String?,
    val removalDelay: Duration
)

fun deserializeCharacterBlueprint(
    id: String,
    data: JsonNode,
    musicById: Map<String, Song>,
    blockbenchModelsById: Map<String, BlockbenchModel>,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>,
    skinsById: Map<String, Skin>,
    questsById: Map<String, Quest>
) = CharacterBlueprint(
    id,
    data["name"].asText(),
    data["level"].asInt(),
    data["max_health"]?.asDouble() ?: 1.0,
    data["mass"]?.asDouble() ?: 70.0,
    data["experience_points"]?.asInt() ?: 0,
    deserializeCharacterModel(
        data["model"],
        blockbenchModelsById,
        blockbenchItemModelsById,
        skinsById
    ),
    data["boss_fight"]?.let { deserializeBossFightBlueprint(it, musicById) },
    data["behavior"]?.let { deserializeBehaviorBlueprint(it) },
    data["stance"]?.let { deserializeStances(it) }
        ?: Stances(Stance.FRIENDLY, emptyList(), emptyList(), emptyList()),
    data["step_sound"]?.let(::deserializeSound),
    data["speak_sound"]?.let(::deserializeSound),
    data["hurt_sound"]?.let(::deserializeSound),
    data["death_sound"]?.let(::deserializeSound),
    data["interactions"]?.map { deserializeInteraction(it, questsById) }
        ?: emptyList(),
    data["loot"]?.let(::deserializeLootTable),
    data["script"]?.let { parseScriptId(it.asText()) },
    data["removal_delay"]?.let { secondsToDuration(it.asDouble()) }
        ?: Duration.ofMillis(1000)
)

fun parseCharacterBlueprintId(id: String) = parseId(id, "characters")
