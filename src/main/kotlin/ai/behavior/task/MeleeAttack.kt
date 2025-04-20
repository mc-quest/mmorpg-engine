package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.Character
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.character.Stance
import net.mcquest.engine.combat.Damage
import net.mcquest.engine.combat.deserializeDamage
import net.mcquest.engine.math.BoundingBox3
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.deserializeVector3
import net.mcquest.engine.sound.deserializeSound

class MeleeAttack(
    private val damage: Damage,
    private val hitboxOffset: Vector3,
    private val hitboxWidth: Double,
    private val hitboxHeight: Double,
    private val knockback: Vector3,
    private val hitSound: Sound?,
    private val missSound: Sound?
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val center = character.position.toVector3() + character.position.localToGlobalDirection(hitboxOffset)
        val halfExtents = Vector3(hitboxWidth, hitboxHeight, hitboxWidth) / 2.0
        val hits = character.instance.getObjectsInBox<Character>(BoundingBox3.from(center, halfExtents))
            .filter { it != character && it.isAlive && character.getStance(it) == Stance.HOSTILE }

        if (hits.isEmpty()) {
            missSound?.let { character.instance.playSound(center, it) }
        } else {
            hitSound?.let { character.instance.playSound(center, it) }

            val globalKnockback = character.position.localToGlobalDirection(knockback)
            hits.forEach {
                it.damage(damage, character)
                it.applyImpulse(globalKnockback)
            }
        }

        return BehaviorStatus.SUCCESS
    }
}

class MeleeAttackBlueprint(
    private val damage: Damage,
    private val hitboxOffset: Vector3,
    private val hitboxWidth: Double,
    private val hitboxHeight: Double,
    private val knockback: Vector3,
    private val hitSound: Sound?,
    private val missSound: Sound?
) : BehaviorBlueprint() {
    override fun create() = MeleeAttack(
        damage,
        hitboxOffset,
        hitboxWidth,
        hitboxHeight,
        knockback,
        hitSound,
        missSound
    )
}

fun deserializeMeleeAttackBlueprint(data: JsonNode) = MeleeAttackBlueprint(
    deserializeDamage(data["damage"]),
    deserializeVector3(data["hitbox_offset"]),
    data["hitbox_width"].asDouble(),
    data["hitbox_height"].asDouble(),
    deserializeVector3(data["knockback"]),
    data["hit_sound"]?.let(::deserializeSound),
    data["miss_sound"]?.let(::deserializeSound)
)
