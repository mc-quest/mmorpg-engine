package net.mcquest.engine.resource

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.base.CaseFormat
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.mcquest.engine.character.CharacterBlueprint
import net.mcquest.engine.character.deserializeCharacterBlueprint
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.instance.deserializeInstance
import net.mcquest.engine.map.AreaMap
import net.mcquest.engine.map.MapTexture
import net.mcquest.engine.map.deserializeMap
import net.mcquest.engine.map.deserializeMapTexture
import net.mcquest.engine.model.*
import net.mcquest.engine.music.Song
import net.mcquest.engine.music.deserializeSong
import net.mcquest.engine.music.deserializeSongAsset
import net.mcquest.engine.quest.Quest
import net.mcquest.engine.quest.deserializeQuest
import net.mcquest.engine.script.loadScriptClasses
import net.mcquest.engine.skill.Skill
import net.mcquest.engine.skill.deserializeSkill
import net.mcquest.engine.sound.deserializeAudioClipAsset
import net.mcquest.engine.zone.Zone
import net.mcquest.engine.zone.deserializeZone
import net.minestom.server.MinecraftServer
import org.python.core.PyType
import org.python.util.PythonInterpreter
import team.unnamed.hephaestus.ModelDataCursor
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File
import kotlin.reflect.KClass

class ResourceLoader(private val root: File) {
    private val objectMapper = ObjectMapper(YAMLFactory())

    fun loadAll(): Resources {
        val server = MinecraftServer.init()
        val interpreter = PythonInterpreter()
        loadScriptClasses(interpreter)
        val config = loadConfig()
        val instancesById = loadInstances()
        val questsById = loadQuests(instancesById)
        val musicById = loadMusic()
        val blockbenchModelsById = loadModels()
        val blockbenchItemModelsById = loadBlockbenchItemModels()
        val skinsById = loadSkins()
        val characterBlueprintsById = loadCharacterBlueprints(
            interpreter,
            musicById,
            blockbenchModelsById,
            blockbenchItemModelsById,
            skinsById,
            questsById
        )
        val skillsById = loadSkills(interpreter)
        val playerClassesById = loadPlayerClasses(skillsById)
        val mapTexturesById = loadMapTextures()
        val mapsById = loadMaps(mapTexturesById)
        val (zones, spawners) = loadZones(
            instancesById,
            musicById,
            mapsById,
            characterBlueprintsById
        )
        return Resources(
            server,
            config,
            instancesById.values,
            questsById.values,
            musicById.values,
            blockbenchModelsById.values,
            blockbenchItemModelsById.values,
            characterBlueprintsById.values,
            zones.values,
            spawners,
            interpreter
        )
    }

    fun loadConfig() = loadYamlResource("config.yaml", ::deserializeConfig)

    fun loadInstances() = loadIdentifiedYamlResources("instances") { id, data ->
        deserializeInstance(id, data, root)
    }

    fun loadQuests(instancesById: Map<String, Instance>) =
        loadIdentifiedYamlResources("quests") { id, data ->
            deserializeQuest(id, data, instancesById)
        }

    fun loadMusic() = loadIdentifiedResources("music", ::deserializeSong)

    fun loadMusicAssets() = loadIdentifiedResources("music", ::deserializeSongAsset)

    fun loadAudioClipAssets() = loadIdentifiedResources("audio_clips", ::deserializeAudioClipAsset)

    fun loadModels(): Map<String, BlockbenchModel> {
        val modelReader = BBModelReader.blockbench(ModelDataCursor(0))
        return loadIdentifiedResources("models") { id, file ->
            deserializeBlockbenchModel(id, file, modelReader)
        }
    }

    fun loadBlockbenchItemModels(): Map<String, BlockbenchItemModel> {
        var customModelData = 0
        return loadIdentifiedResources("item_models") { id, file ->
            val itemModel = BlockbenchItemModel(id, customModelData)
            customModelData++
            itemModel
        }
    }

    fun loadBlockbenchItemModelAssets(): Map<String, BlockbenchItemModelAsset> {
        val modelReader = BBModelReader.blockbench(ModelDataCursor(0))
        return loadIdentifiedResources("item_models") { id, file ->
            deserializeBlockbenchItemModelAsset(id, file, modelReader)
        }
        // TODO: I THINK THIS IS TOTALLY WRONG, WE NEED TO WRITE OUR OWN MODEL READER
    }

    fun loadSkins() = loadIdentifiedYamlResources("skins", ::deserializeSkin)

    fun loadCharacterBlueprints(
        interpreter: PythonInterpreter,
        musicById: Map<String, Song>,
        blockbenchModelsById: Map<String, BlockbenchModel>,
        blockbenchItemModelsById: Map<String, BlockbenchItemModel>,
        skinsById: Map<String, Skin>,
        questsById: Map<String, Quest>
    ) = loadIdentifiedYamlResources("characters") { id, data ->
        deserializeCharacterBlueprint(
            id,
            data,
            interpreter,
            musicById,
            blockbenchModelsById,
            blockbenchItemModelsById,
            skinsById,
            questsById
        )
    }

    fun loadSkills(interpreter: PythonInterpreter) =
        loadIdentifiedYamlResources("skills") { id, data ->
            deserializeSkill(
                id,
                data,
                interpreter
            )
        }

    fun loadPlayerClasses(skillsById: Map<String, Skill>) =
        loadIdentifiedYamlResources("classes") { id, data ->

        }

    fun loadMapTextures() = loadIdentifiedResources(
        "map_textures",
        ::deserializeMapTexture
    )

    fun loadMaps(mapTexturesById: Map<String, MapTexture>) =
        loadIdentifiedYamlResources("maps") { id, data ->
            deserializeMap(id, data, mapTexturesById)
        }

    fun loadZones(
        instancesById: Map<String, Instance>,
        musicById: Map<String, Song>,
        mapsById: Map<String, AreaMap>,
        characterBlueprintsById: Map<String, CharacterBlueprint>
    ): Pair<Map<String, Zone>, Collection<GameObjectSpawner>> {
        val zonesAndSpawnersById =
            loadIdentifiedYamlResources("zones") { id, data ->
                deserializeZone(
                    id,
                    data,
                    instancesById,
                    mapsById,
                    musicById,
                    characterBlueprintsById
                )
            }
        val zonesById = zonesAndSpawnersById.entries.associate {
            it.key to it.value.first
        }
        val spawners = zonesAndSpawnersById.values.flatMap { it.second }
        return Pair(zonesById, spawners)
    }

    private fun <T> loadIdentifiedResources(
        path: String,
        deserialize: (String, File) -> T
    ) = loadIdentifiedResources(root.resolve(path), deserialize)

    private fun <T> loadIdentifiedYamlResources(
        path: String,
        deserialize: (String, JsonNode) -> T
    ) = loadIdentifiedResources(path) { id, file ->
        val data = readYaml(file)
        deserialize(id, data)
    }

    private fun <T> loadIdentifiedResources(
        dir: File,
        deserialize: (String, File) -> T
    ) = dir.walk().filter(File::isFile).sorted().associate { file ->
        val id = file.relativeTo(dir).path.substringBefore(".").replace(File.separatorChar, '.')
        id to deserialize(id, file)
    }

    private fun <T> loadYamlResource(
        path: String,
        deserialize: (JsonNode) -> T
    ) = loadYamlResource(root.resolve(path), deserialize)

    private fun <T> loadYamlResource(
        file: File,
        deserialize: (JsonNode) -> T
    ) = deserialize(readYaml(file))

    private fun readYaml(file: File): JsonNode = objectMapper.readTree(file)

    private fun <T> loadIdentifiedJsonResources(
        path: String,
        deserialize: (String, JsonElement) -> T
    ) = loadIdentifiedResources(path) { id, file ->
        val data = readJsonData(file)
        deserialize(id, data)
    }

    private fun readJsonData(file: File) = file.reader().use(JsonParser::parseReader)
}

fun parseId(fullId: String, expectedPrefix: String): String {
    val (prefix, id) = splitId(fullId)
    require(prefix == expectedPrefix)
    return id
}

fun splitId(fullId: String): Pair<String, String> {
    val (prefix, id) = fullId.split(":")
    require(prefix.isNotBlank())
    require(id.isNotBlank())
    return Pair(prefix, id)
}

fun <T : Any> loadScript(
    superClass: KClass<T>,
    script: String?,
    id: String,
    interpreter: PythonInterpreter
): PyType {
    val className = idToPythonClassName(id)
    var wrappedScript = """
        class $className(${superClass.simpleName}):
          def __init__(self, handle):
            NonPlayerCharacter.__init__(self, handle)
    """.trimIndent()
    script?.let { wrappedScript += "\n\n${it.prependIndent("  ")}" }
    interpreter.exec(wrappedScript)
    return interpreter.get(className) as PyType
}

fun idToPythonClassName(id: String): String = id.split('.').joinToString("") {
    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, it)
}
