package net.mcquest.engine.character

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.mcquest.engine.combat.Damage
import net.mcquest.engine.item.Inventory
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.music.MusicPlayer
import net.mcquest.engine.persistence.PlayerCharacterData
import net.mcquest.engine.quest.QuestTracker
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.skill.SkillTracker
import net.mcquest.engine.script.PlayerCharacter as ScriptPlayerCharacter
import net.mcquest.engine.time.secondsToTicks
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.util.toMinestom
import net.minestom.server.attribute.Attribute
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.HitAnimationPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import java.time.Duration
import kotlin.math.ceil

class PlayerCharacter(
    runtime: Runtime,
    spawner: PlayerCharacterSpawner,
    override val entity: Player,
    data: PlayerCharacterData
) : Character(runtime, spawner) {
    override val name
        get() = entity.username
    override var level = 1 // TODO: update nameplate on set
    var experiencePoints = 0 // TODO
        private set

    override var maxHealth = data.maxHealth
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar(this)
        }

    override var health = data.health
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar(this)
        }

    override val mass = 70.0
    var maxMana = 1.0
    var mana = 1.0
    override val hitbox = CharacterHitbox(this)
    override val handle = ScriptPlayerCharacter(this)
    val skillTracker = SkillTracker(this)
    val questTracker = QuestTracker(this, data.questTrackerData)
    val inventory = Inventory(this)
    val musicPlayer = MusicPlayer(this)

    override var position
        get() = super.position
        set(value) {
            super.position = value
            runtime.zoneManager.updateZone(this)
        }

    // val skillTracker = SkillTracker(this, data.skillTrackerData)

    var zone = runtime.zoneManager.getZone(data.zoneId)
        set(value) {
            field = value
            enterZone()
        }

    override fun spawn() {
        entityTeleporting = true
        entity.setInstance(
            instance.instanceContainer,
            position.toMinestom()
        ).thenRun {
            entity.teleport(position.toMinestom())
            entityTeleporting = false
            enterZone()
        }
        questTracker.start()
        entity.setHeldItemSlot(8)
        super.spawn()
    }

    override fun tick() {
        super.tick()
        skillTracker.tick()
        entity.sendActionBar(actionBar())
    }

    override fun getStance(toward: Character) =
        if (toward is PlayerCharacter)
            Stance.FRIENDLY
        else
            toward.getStance(this)

    override fun damage(damage: Damage, source: Character) {
        super.damage(damage, source)
        entity.sendPacketToViewersAndSelf(
            HitAnimationPacket(entity.entityId, position.yaw.toFloat())
        )
    }

    override fun die() {
        disableMovement()
        hitbox.remove()

        entity.showTitle(
            Title.title(
                Component.text("YOU DIED", NamedTextColor.RED),
                Component.empty()
            )
        )

        entity.addEffect(Potion(PotionEffect.BLINDNESS, 1, secondsToTicks(4.0)))

        schedulerManager.buildTask(::respawn)
            .delay(Duration.ofSeconds(3))
            .schedule()
    }

    private fun respawn() {
        health = maxHealth
        mana = maxMana
        val respawnPosition = position // TODO
        entity.teleport(respawnPosition.toMinestom())
        runtime.collisionManager.add(hitbox)
        enableMovement()
    }

    fun addExperiencePoints(points: Int) {
        experiencePoints += points
    }

    fun sendMessage(message: Component) = entity.sendMessage(message)

    private fun updateHealthBar() {
        entity.health = maxOf((20.0 * (health / maxHealth)).toFloat(), 1.0F)
    }

    private fun actionBar() =
        Component.text(
            "❤ ${ceil(health).toInt()}/${ceil(maxHealth).toInt()}",
            NamedTextColor.RED
        ).append(
            Component.text(
                "     "
            )
        ).append(
            Component.text(
                "❈ ${ceil(mana).toInt()}/${ceil(maxMana).toInt()}",
                NamedTextColor.AQUA
            )
        )

    private fun enterZone() {
        val title = Title.title(zone.displayName, zone.levelText)
        entity.showTitle(title)
        // TODO: if (not in boss fight)
        musicPlayer.setSong(zone.music)
    }

    override fun interact(pc: PlayerCharacter) = Unit

    fun playSound(sound: Sound) = entity.playSound(sound)

    fun playSound(sound: Sound, from: Vector3) =
        entity.playSound(sound, from.toMinestom())

    fun disableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.0F
    }

    fun enableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1F
    }
}
