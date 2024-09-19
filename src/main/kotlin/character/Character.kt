package net.mcquest.engine.character

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.mcquest.engine.collision.Collider
import net.mcquest.engine.combat.Damage
import net.mcquest.engine.gameobject.GameObject
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.fromMinestom
import net.mcquest.engine.util.toMinestom
import net.minestom.server.entity.LivingEntity
import team.unnamed.hephaestus.minestom.ModelEntity
import java.lang.Math.toRadians
import kotlin.math.ceil
import net.mcquest.engine.script.Character as ScriptCharacter

abstract class Character(
    runtime: Runtime,
    spawner: GameObjectSpawner,
    val summoner: Character? = null
) : GameObject(runtime, spawner) {
    abstract val name: String
    abstract val level: Int
    abstract val maxHealth: Double
    abstract var health: Double
    abstract val mass: Double
    abstract val entity: LivingEntity
    abstract val hitbox: CharacterHitbox
    abstract val handle: ScriptCharacter
    val nameplate = CharacterNameplate()
    var entityTeleporting = false
        protected set

    override var position
        get() = super.position
        set(value) {
            super.position = value
            hitbox.position = value.toVector3()
            nameplate.updatePosition(this)
        }

    val previousPosition
        get() = Position.fromMinestom(entity.previousPosition)

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

    override fun setInstance(instance: Instance, position: Position) {
        super.setInstance(instance, position)
        entityTeleporting = true
        entity.setInstance(
            instance.instanceContainer,
            position.toMinestom()
        ).thenRun {
            entity.teleport(position.toMinestom())
            entityTeleporting = false
        }
        hitbox.setInstance(instance, position.toVector3())
        nameplate.updateInstance(this)
    }

    override fun spawn() {
        nameplate.spawn(this)
        runtime.collisionManager.add(hitbox)
    }

    override fun despawn() {
        entity.remove()
        nameplate.despawn()
        hitbox.remove()
    }

    override fun tick() {
        if (!entityTeleporting)
            position = Position.fromMinestom(entity.position)
        nameplate.tick(this)
    }

    fun lookAt(position: Vector3) {
        entity.lookAt(position.toMinestom())
        this.position = Position.fromMinestom(entity.position)
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
        val globalOffset = localOffset.rotateAroundY(toRadians(-position.yaw))
        runtime.soundManager.playSound(
            instance,
            (position.toVector3() + globalOffset),
            sound
        )
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
        health = maxOf(0.0, health - damage.amount)
        nameplate.updateHealthBar(this)
        if (health == 0.0) die()
    }

    protected abstract fun die()

    fun applyImpulse(impulse: Vector3) {
        velocity += impulse / mass
    }

    abstract fun interact(pc: PlayerCharacter)

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

class CharacterHitbox(val character: Character) : Collider(
    character.instance,
    character.position.toVector3(),
    Vector3.fromMinestom(character.entity.boundingBox)
)
