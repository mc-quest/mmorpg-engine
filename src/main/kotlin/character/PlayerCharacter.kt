package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.entity.Hologram
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.item.Inventory
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.music.MusicPlayer
import com.shadowforgedmmo.engine.persistence.PlayerCharacterData
import com.shadowforgedmmo.engine.quest.QuestTracker
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.skill.SkillTracker
import com.shadowforgedmmo.engine.time.secondsToTicks
import com.shadowforgedmmo.engine.util.loadJsonResource
import com.shadowforgedmmo.engine.util.schedulerManager
import com.shadowforgedmmo.engine.util.toMinestom
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.HitAnimationPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.timer.TaskSchedule
import java.time.Duration
import kotlin.math.ceil
import com.shadowforgedmmo.engine.script.PlayerCharacter as ScriptPlayerCharacter

val experiencePointsPerLevel = loadJsonResource(
    "data/experience_points_per_level.json",
    Array<Int>::class
).toList() + listOf(Integer.MAX_VALUE)
val experiencePointsPerLevelPrefixSum =
    listOf(0) + experiencePointsPerLevel.dropLast(1).runningReduce { acc, points -> acc + points }
val maxExperiencePoints = experiencePointsPerLevelPrefixSum.last()

class PlayerCharacter(
    spawner: PlayerCharacterSpawner,
    instance: Instance,
    runtime: Runtime,
    override val entity: Player,
    data: PlayerCharacterData
) : Character(spawner, instance, runtime) {
    override val name
        get() = entity.username

    override var level = 1 // TODO INITIALIZE USING DATA
        private set

    var experiencePoints = 0 // TODO INITIALIZE USING DATA
        private set

    override var maxHealth = data.maxHealth
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar()
        }

    override var health = data.health
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar()
        }

    override val mass
        get() = 70.0

    var maxMana = 1.0
    var mana = 1.0
    override val handle = ScriptPlayerCharacter(this)
    val skillTracker = SkillTracker(this)
    val questTracker = QuestTracker(this, data.questTrackerData)
    val inventory = Inventory(this)
    val musicPlayer = MusicPlayer(this)
    var bossFights = mutableSetOf<BossFight>()

    // val skillTracker = SkillTracker(this, data.skillTrackerData)

    var zone = runtime.zonesById.getValue(data.zoneId)
        set(value) {
            field = value
            enterZone()
        }

    override val removeEntityOnDespawn
        get() = false

    override fun spawn() {
        // entityTeleporting = true
        super.spawn()
        updateExperienceBar()
        updateLevelDisplay()
        questTracker.start()
        entity.setHeldItemSlot(8)
        enterZone()
    }

    override fun despawn() {
        super.despawn()
        // TODO: save data
    }

    override fun tick() {
        super.tick()
        skillTracker.tick()
        entity.sendActionBar(actionBar())
        updateZone()
    }

    private fun updateZone() {
        val newZone = instance.zoneAt(position.toVector2())
        if (newZone != null &&
            newZone != zone &&
            (!zone.outerBoundary.contains(position.toVector2()) ||
                    newZone.type.priority > zone.type.priority)
        ) {
            zone = newZone
        }
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
        enableMovement()
    }

    fun addExperiencePoints(points: Int, position: Vector3) {
        experiencePoints = (experiencePoints + points).coerceAtMost(maxExperiencePoints)
        checkForLevelUp()
        updateExperienceBar()
        spawnExperiencePointsNotification(points, position)
    }

    private fun checkForLevelUp() {
        while (experiencePointsIntoLevel() >= experiencePointsForNextLevel()) levelUp()
    }

    private fun levelUp() {
        level++
        updateLevelDisplay()
        // TODO: sounds, particles, and message
    }

    private fun updateLevelDisplay() {
        entity.level = level
    }

    private fun updateExperienceBar() {
        entity.exp = experiencePointsIntoLevel().toFloat() / experiencePointsForNextLevel().toFloat()
    }

    private fun experiencePointsIntoLevel() = experiencePoints - experiencePointsPerLevelPrefixSum[level - 1]

    private fun experiencePointsForNextLevel() = experiencePointsPerLevel[level - 1]

    private fun spawnExperiencePointsNotification(points: Int, position: Vector3) {
        val hologram = Hologram()
        hologram.text = Component.text("+$points XP", NamedTextColor.GREEN)
        hologram.isAutoViewable = false
        hologram.velocity = (Vector3.UP * 0.5).toMinestom()
        hologram.setInstance(instance.instanceContainer, position.toMinestom()).join()
        hologram.addViewer(entity)
        schedulerManager.buildTask(hologram::remove).delay(TaskSchedule.seconds(2)).schedule()
    }

    fun sendMessage(message: Component) = entity.sendMessage(message)

    private fun updateHealthBar() {
        entity.health = (20.0 * (health / maxHealth)).toFloat().coerceAtLeast(1.0F)
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
        if (bossFights.isEmpty()) musicPlayer.setSong(zone.music)
    }

    fun updateMusic() {
        val song = bossFights.maxWithOrNull(
            compareBy<BossFight> {
                it.character.level
            }.thenBy {
                it.character.name
            }
        )?.music ?: zone.music
        musicPlayer.setSong(song)
    }

    override fun interact(pc: PlayerCharacter) = Unit

    fun playSound(sound: Sound) = entity.playSound(sound)

    fun playSound(sound: Sound, from: Vector3) =
        entity.playSound(sound, from.toMinestom())

    fun disableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.0
    }

    fun enableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1
    }
}
