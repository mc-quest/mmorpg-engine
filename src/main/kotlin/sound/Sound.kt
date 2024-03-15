package net.mcquest.engine.sound

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

fun deserializeSound(data: JsonNode) = Sound.sound(
    Key.key(data["clip"].asText()),
    Sound.Source.valueOf(data["source"].asText().uppercase()),
    data["volume"]?.floatValue() ?: 1.0F,
    data["pitch"]?.floatValue() ?: 1.0F
)
