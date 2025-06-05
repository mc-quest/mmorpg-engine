package com.shadowforgedmmo.engine.character

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.fromMinestom
import com.shadowforgedmmo.engine.util.toMinestom
import net.minestom.server.entity.LivingEntity
import team.unnamed.hephaestus.minestom.ModelEntity
import kotlin.math.ceil
import com.shadowforgedmmo.engine.script.Character as ScriptCharacter

abstract class Character(
    spawner: GameObjectSpawner,
    instance: Instance,
    runtime: Runtime,
    val summoner: Character? = null
) : GameObject(spawner, instance, runtime) {
    abstract override val entity: LivingEntity
    abstract val name: String
    abstract val level: Int
    abstract val maxHealth: Double
    abstract var health: Double
    abstract val mass: Double
    abstract val handle: ScriptCharacter
    val nameplate = CharacterNameplate(this)

    val isAlive
        get() = health > 0

    var isInvisible
        get() = entity.isInvisible
        set(value) {
            entity.isInvisible = value
        }

    val isOnGround
        get() = entity.isOnGround

    var velocity
        get() = Vector3.fromMinestom(entity.velocity)
        set(value) {
            entity.velocity = value.toMinestom()
        }

    val height
        get() = entity.boundingBox.height()

    val eyeHeight
        get() = entity.eyeHeight

    val eyePosition
        get() = position.toVector3() + Vector3.UP * eyeHeight

    val rootSummoner: Character?
        get() = summoner?.let { it.rootSummoner ?: it }

    override fun spawn() {
        super.spawn()
        nameplate.spawn()
    }

    override fun despawn() {
        super.despawn()
        nameplate.despawn()
    }

    override fun tick() {
        super.tick()
        nameplate.tick()
    }

    // TODO:  should this go in GameObject?
    fun lookAt(position: Vector3) {
        entity.lookAt(position.toMinestom())
    }

    fun lookAt(character: Character) = lookAt(character.eyePosition)

    fun playAnimation(animation: String) {
        val entity = this.entity
        if (entity is ModelEntity) {
            if (animation in entity.model().animations())
                entity.playAnimation(animation)
        } else {
            if (animation == ANIMATION_SWING_MAIN_HAND) entity.swingMainHand()
            else if (animation == ANIMATION_SWING_OFF_HAND) entity.swingOffHand()
        }
    }

    fun emitSound(sound: Sound, localOffset: Vector3 = Vector3.ZERO) {
        val globalOffset = position.localToGlobalDirection(localOffset)
        instance.playSound(position.toVector3() + globalOffset, sound)
    }

    open fun speak(dialogue: Component, to: PlayerCharacter) = to.sendMessage(
        Component.empty().append(
            Component.text("[", NamedTextColor.GRAY)
                .append(displayName(getStance(to)))
                .append(Component.text("]: ", NamedTextColor.GRAY))
        ).append(dialogue)
    )

    abstract fun getStance(toward: Character): Stance

    open fun damage(damage: Damage, source: Character) {
        if (!isAlive) return
        // TODO: Factor in resistances
        health = maxOf(0.0, health - damage.damage.values.sum())
        nameplate.updateHealthBar()
        if (health == 0.0) die()
    }

    protected abstract fun die()

    fun applyImpulse(impulse: Vector3) {
        velocity += impulse / mass
    }

    fun displayName(stance: Stance) = Component.text(name, stance.color)

    fun displayNameWithLevel(stance: Stance) =
        Component.text("[", NamedTextColor.GRAY)
            .append(Component.text("Lv. $level", NamedTextColor.GOLD))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .append(displayName(stance))

    fun healthBarText(): Component {
        val numBars = 20
        val ratio = health / maxHealth
        val numRedBars = ceil(numBars * ratio).toInt()
        val numGrayBars = numBars - numRedBars
        return Component.text("[", NamedTextColor.GRAY)
            .append(Component.text("|".repeat(numRedBars), NamedTextColor.RED))
            .append(Component.text("|".repeat(numGrayBars), NamedTextColor.GRAY))
            .append(Component.text("]", NamedTextColor.GRAY))
    }
}
