package net.mcquest.engine.persistence

import net.mcquest.engine.math.Position

class PlayerCharacterData(
    val instanceId: String,
    val position: Position,
    val maxHealth: Double,
    val health: Double,
    val questTrackerData: QuestTrackerData,
    val zoneId: String
)
