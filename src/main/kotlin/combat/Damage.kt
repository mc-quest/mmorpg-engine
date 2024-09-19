package net.mcquest.engine.combat

import com.fasterxml.jackson.databind.JsonNode

class Damage(val type: DamageType, val amount: Double)

enum class DamageType {
    PHYSICAL
}

fun deserializeDamage(data: JsonNode) = Damage(
    DamageType.valueOf(data["type"].asText().uppercase()),
    data["amount"].asDouble()
)
