package net.mcquest.engine.resource

import com.fasterxml.jackson.databind.JsonNode
import java.net.URI

class Config(
    val name: String,
    val resourcePackUri: URI,
    val resourcePackHash: String
)

fun deserializeConfig(data: JsonNode): Config {
    return Config(
        data["name"].asText(),
        URI(data["resource_pack_uri"].asText()),
        data["resource_pack_hash"]?.asText() ?: ""
    )
}
