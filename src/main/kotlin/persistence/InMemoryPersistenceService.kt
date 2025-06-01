package com.shadowforgedmmo.engine.persistence

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

class InMemoryPersistenceService : PersistenceService {
    private val dataByUuid = mutableMapOf<UUID, Array<PlayerCharacterData?>>()

    override fun load(uuid: UUID, slot: Int): PlayerCharacterData? {
        return dataByUuid[uuid]?.get(slot)
    }

    override fun save(uuid: UUID, slot: Int, data: PlayerCharacterData) {
        dataByUuid.computeIfAbsent(uuid) { arrayOfNulls(4) }[slot] = data
    }
}

fun deserializeInMemoryPersistenceService(data: JsonNode) =
    InMemoryPersistenceService()
