package net.mcquest.engine.sound

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

fun deserializeSound(data: JsonNode) =
    if (data.isTextual) Sound.sound(
        Key.key(data.asText()),
        Sound.Source.MASTER,
        1.0F,
        1.0F
    )
    else Sound.sound(
        Key.key(data["name"].asText()),
        data["source"]?.asText()?.uppercase()?.let(Sound.Source::valueOf)
            ?: Sound.Source.MASTER,
        data["volume"]?.floatValue() ?: 1.0F,
        data["pitch"]?.floatValue() ?: 1.0F
    )
