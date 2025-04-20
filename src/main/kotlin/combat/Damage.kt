package net.mcquest.engine.combat

import com.fasterxml.jackson.databind.JsonNode

class Damage(val damage: Map<DamageType, Double>)

enum class DamageType {
    PHYSICAL,
    ARCANE,
    FIRE,
    FROST,
    NATURE,
    SHADOW,
    HOLY
}

fun deserializeDamage(data: JsonNode): Damage =
    if (data.isNumber) {
        Damage(mapOf(DamageType.PHYSICAL to data.asDouble()))
    } else if (data.isObject) {
        Damage(data.fields().asSequence().associate { (type, amount) ->
            DamageType.valueOf(type.uppercase()) to amount.asDouble()
        })
    } else {
        throw IllegalArgumentException()
    }
