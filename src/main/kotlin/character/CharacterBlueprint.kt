package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.deserializeBehaviorBlueprint
import net.mcquest.engine.loot.LootTable
import net.mcquest.engine.loot.deserializeLootTable
import net.mcquest.engine.model.BlockbenchItemModel
import net.mcquest.engine.model.BlockbenchModel
import net.mcquest.engine.model.Skin
import net.mcquest.engine.music.Song
import net.mcquest.engine.quest.Quest
import net.mcquest.engine.resource.loadScript
import net.mcquest.engine.resource.parseId
import net.mcquest.engine.script.NonPlayerCharacter as ScriptNonPlayerCharacter
import net.mcquest.engine.sound.deserializeSound
import net.mcquest.engine.time.secondsToDuration
import org.python.core.PyType
import org.python.util.PythonInterpreter
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
    val hurtSound: Sound?,
    val deathSound: Sound?,
    val speakSound: Sound?,
    val interactions: List<Interaction>,
    val lootTable: LootTable?,
    val script: PyType,
    val removalDelay: Duration
)

fun deserializeCharacterBlueprint(
    id: String,
    data: JsonNode,
    interpreter: PythonInterpreter,
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
    data["hurt_sound"]?.let(::deserializeSound),
    data["death_sound"]?.let(::deserializeSound),
    data["speak_sound"]?.let(::deserializeSound),
    data["interactions"]?.map { deserializeInteraction(it, questsById) }
        ?: emptyList(),
    data["loot"]?.let(::deserializeLootTable),
    loadScript(ScriptNonPlayerCharacter::class, data["script"]?.asText(), id, interpreter),
    data["removal_delay"]?.let { secondsToDuration(it.asDouble()) }
        ?: Duration.ofMillis(1000)
)

fun parseCharacterBlueprintId(id: String) = parseId(id, "characters")
