package com.shadowforgedmmo.engine.zone

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.time.secondsToMillis

data class WeatherCycle(val weatherEntries: List<WeatherEntry>)

data class WeatherEntry(val durationMillis: Long, val weather: Weather)

data class Weather(val rain: Float, val thunder: Float) {
    init {
        require(rain in 0.0F..1.0F)
        require(thunder in 0.0F..1.0F)
    }

    companion object {
        val CLEAR = Weather(0.0F, 0.0F)
        val RAIN = Weather(1.0F, 0.0F)
        val THUNDER = Weather(1.0F, 1.0F)
    }
}

fun deserializeWeatherCycle(data: JsonNode) =
    WeatherCycle(data.map(::deserializeWeatherEntry))

private fun deserializeWeatherEntry(data: JsonNode) = WeatherEntry(
    secondsToMillis(data["duration"].asDouble()),
    deserializeWeather(data["weather"])
)

private fun deserializeWeather(data: JsonNode) =
    if (data.isTextual) {
        when (data.asText()) {
            "clear" -> Weather.CLEAR
            "rain" -> Weather.RAIN
            "thunder" -> Weather.THUNDER
            else -> throw IllegalArgumentException()
        }
    } else {
        Weather(data["rain"].floatValue(), data["thunder"].floatValue())
    }
