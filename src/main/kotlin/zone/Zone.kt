package com.shadowforgedmmo.engine.zone

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.character.deserializeNonPlayerCharacterSpawners
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.loot.LootChestSpawner
import com.shadowforgedmmo.engine.map.AreaMap
import com.shadowforgedmmo.engine.map.parseMapId
import com.shadowforgedmmo.engine.math.Polygon
import com.shadowforgedmmo.engine.math.deserializePolygon
import com.shadowforgedmmo.engine.math.deserializePosition
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.music.parseSongId
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.transition.deserializeTransitionSpawner
import com.shadowforgedmmo.engine.util.schedulerManager
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
