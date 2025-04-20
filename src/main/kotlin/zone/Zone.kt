package net.mcquest.engine.zone

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.mcquest.engine.character.CharacterBlueprint
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.character.deserializeNonPlayerCharacterSpawners
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.loot.LootChestSpawner
import net.mcquest.engine.map.AreaMap
import net.mcquest.engine.map.parseMapId
import net.mcquest.engine.math.Polygon
import net.mcquest.engine.math.deserializePolygon
import net.mcquest.engine.math.deserializePosition
import net.mcquest.engine.music.Song
import net.mcquest.engine.music.parseSongId
import net.mcquest.engine.resource.parseId
import net.mcquest.engine.transition.deserializeTransitionSpawner
import net.mcquest.engine.util.schedulerManager
import java.time.Duration
import net.minestom.server.instance.Weather as MinestomWeather

class Zone(
    val id: String,
    val name: String,
    val type: ZoneType,
    val level: Int,
    val boundary: Polygon,
    val map: AreaMap,
    val music: Song?,
    val weatherCycle: WeatherCycle
) {
    val outerBoundary = boundary.offset(10.0) // TOOD: update offset

    val displayName
        get() = Component.text(name, type.color)

    val levelText
        get() = Component.text("Level $level", NamedTextColor.GOLD)

    val playerCharacters: Set<PlayerCharacter> = setOf()

    @Suppress("UnstableApiUsage")
    var weather =
        if (weatherCycle.weatherEntries.isEmpty()) Weather.CLEAR
        else weatherCycle.weatherEntries[0].weather
        private set(value) {
            field = value
            val packets = MinestomWeather(
                weather.rain,
                weather.thunder
            ).createWeatherPackets()
            playerCharacters.forEach { it.entity.sendPackets(packets) }
        }

    fun init() {
        if (weatherCycle.weatherEntries.size > 1) scheduleWeatherUpdate(1)
    }

    private fun scheduleWeatherUpdate(index: Int) {
        schedulerManager.buildTask {
            weather = weatherCycle.weatherEntries[index].weather
            scheduleWeatherUpdate((index + 1) % weatherCycle.weatherEntries.size)
        }
            .delay(Duration.ofMillis(weatherCycle.weatherEntries[index].durationMillis))
            .schedule()
    }
}

fun deserializeZone(
    id: String,
    data: JsonNode,
    mapsById: Map<String, AreaMap>,
    musicById: Map<String, Song>,
    characterBlueprintsById: Map<String, CharacterBlueprint>
): Pair<Zone, Collection<GameObjectSpawner>> {
    val respawnPoints = data["respawn_points"]?.map(::deserializePosition) ?: emptyList()

    val transitionSpawners = data["transitions"]?.map(::deserializeTransitionSpawner) ?: emptyList()

    val characterSpawners = data["characters"]?.flatMap {
        deserializeNonPlayerCharacterSpawners(it, characterBlueprintsById)
    } ?: emptyList()

    val lootChestSpawners = listOf<LootChestSpawner>() // TODO

    val spawners = transitionSpawners + characterSpawners + lootChestSpawners

    val zone = Zone(
        id,
        data["name"].asText(),
        ZoneType.valueOf(data["type"].asText().uppercase()),
        data["level"].asInt(),
        deserializePolygon(data["boundary"]),
        mapsById.getValue(parseMapId(data["map"].asText())),
        data["music"]?.let { musicById.getValue(parseSongId(it.asText())) },
        data["weather_cycle"]?.let(::deserializeWeatherCycle) ?: WeatherCycle(listOf())
    )

    return Pair(zone, spawners)
}

fun parseZoneId(id: String) = parseId(id, "zones")
