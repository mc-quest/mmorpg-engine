package net.mcquest.engine.persistence

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

interface PersistenceService {
    fun load(uuid: UUID, slot: Int): PlayerCharacterData?

    fun save(uuid: UUID, slot: Int, data: PlayerCharacterData)
}

fun deserializePersistenceService(data: JsonNode): PersistenceService
    = when(data["type"].asText()) {
        "in_memory" -> deserializeInMemoryPersistenceService(data)
        else -> throw IllegalArgumentException("Unknown persistence service type")
    }
