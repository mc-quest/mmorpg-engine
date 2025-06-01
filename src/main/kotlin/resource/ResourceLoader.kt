package com.shadowforgedmmo.engine.resource

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.deserializeCharacterBlueprint
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.deserializeInstance
import com.shadowforgedmmo.engine.map.AreaMap
import com.shadowforgedmmo.engine.map.MapTexture
import com.shadowforgedmmo.engine.map.deserializeMap
import com.shadowforgedmmo.engine.map.deserializeMapTexture
import com.shadowforgedmmo.engine.model.*
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.music.deserializeSong
import com.shadowforgedmmo.engine.music.deserializeSongAsset
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.quest.deserializeQuest
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.skill.deserializeSkill
import com.shadowforgedmmo.engine.sound.deserializeSoundAsset
import com.shadowforgedmmo.engine.zone.Zone
import com.shadowforgedmmo.engine.zone.deserializeZone
import net.minestom.server.MinecraftServer
import team.unnamed.hephaestus.ModelDataCursor
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File

class ResourceLoader(private val root: File) {
    private val objectMapper = ObjectMapper(YAMLFactory())

    fun loadAll(): Resources {
        val server = MinecraftServer.init()
        val config = loadConfig()
        val questsById = loadQuests()
        val musicById = loadMusic()
        val blockbenchModelsById = loadModels()
        val blockbenchItemModelsById = loadBlockbenchItemModels()
        val skinsById = loadSkins()
        val characterBlueprintsById = loadCharacterBlueprints(
            musicById,
            blockbenchModelsById,
            blockbenchItemModelsById,
            skinsById,
            questsById
        )
        val skillsById = loadSkills()
        val playerClassesById = loadPlayerClasses(skillsById)
        val mapTexturesById = loadMapTextures()
        val mapsById = loadMaps(mapTexturesById)
        val zonesAndSpawnersByZoneId = loadZones(
            musicById,
            mapsById,
            characterBlueprintsById
        )
        val zonesById = zonesAndSpawnersByZoneId.entries.associate {
            it.key to it.value.first
        }
        val instancesById = loadInstances(zonesAndSpawnersByZoneId)

        return Resources(
            server,
            config,
            instancesById.values,
            questsById.values,
            musicById.values,
            blockbenchModelsById.values,
            blockbenchItemModelsById.values,
            characterBlueprintsById.values,
            zonesById.values,
            root.resolve("scripts")
        )
    }

    fun loadConfig() = loadYamlResource("config.yaml", ::deserializeConfig)

    fun loadInstances(
        zonesAndSpawnersByZoneId: Map<String, Pair<Zone, Collection<GameObjectSpawner>>>
    ) = loadIdentifiedYamlResources("instances") { id, data ->
        deserializeInstance(id, data, root, zonesAndSpawnersByZoneId)
    }

    fun loadQuests() = loadIdentifiedYamlResources("quests", ::deserializeQuest)

    fun loadMusic() = loadIdentifiedResources("music", ::deserializeSong)

    fun loadMusicAssets() = loadIdentifiedResources("music", ::deserializeSongAsset)

    fun loadSoundAssets() = loadIdentifiedResources("sounds", ::deserializeSoundAsset)

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
        musicById: Map<String, Song>,
        blockbenchModelsById: Map<String, BlockbenchModel>,
        blockbenchItemModelsById: Map<String, BlockbenchItemModel>,
        skinsById: Map<String, Skin>,
        questsById: Map<String, Quest>
    ) = loadIdentifiedYamlResources("characters") { id, data ->
        deserializeCharacterBlueprint(
            id,
            data,
            musicById,
            blockbenchModelsById,
            blockbenchItemModelsById,
            skinsById,
            questsById
        )
    }

    fun loadSkills() = loadIdentifiedYamlResources("skills", ::deserializeSkill)

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
        musicById: Map<String, Song>,
        mapsById: Map<String, AreaMap>,
        characterBlueprintsById: Map<String, CharacterBlueprint>
    ) = loadIdentifiedYamlResources("zones") { id, data ->
        deserializeZone(
            id,
            data,
            mapsById,
            musicById,
            characterBlueprintsById
        )
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
    ): Map<String, T> = dir.walk()
        .filter(File::isFile)
        .sorted()
        .mapNotNull { file ->
            val id = file.relativeTo(dir).path.substringBefore(".").replace(File.separatorChar, '.')
            val resource = try {
                deserialize(id, file)
            } catch (e: Exception) {
                val prefix = dir.relativeTo(root).path
                System.err.println("Error loading resource $prefix:$id")
                null
            }
            resource?.let { id to it }
        }
        .toMap()

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
    return prefix to id
}
