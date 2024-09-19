package net.mcquest.engine.character

import net.mcquest.engine.combat.Damage
import net.mcquest.engine.combat.DamageType
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.persistence.PlayerCharacterData
import net.mcquest.engine.persistence.QuestTrackerData
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.globalEventHandler
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.util.toMinestom
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.player.*
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.timer.TaskSchedule

private const val INTERACT_DISTANCE = 4.0
private const val SPAWN_INSTANCE = "eladrador"
private val SPAWN_POSITION = Position(2864.0, 73.0, 3598.0)

class PlayerCharacterManager {
    private val pcsByPlayer = mutableMapOf<Player, PlayerCharacter>()

    val playerCharacters: Collection<PlayerCharacter>
        get() = pcsByPlayer.values

    fun start(runtime: Runtime) {
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            schedulerManager.buildTask {
                val data = PlayerCharacterData(
                    SPAWN_INSTANCE,
                    SPAWN_POSITION,
                    5.0,
                    5.0,
                    QuestTrackerData(),
                    "oakshire"
                )
                event.player.respawnPoint = data.position.toMinestom()
                val instance = runtime.instanceManager.getInstance(data.instanceId)
                val spawner = PlayerCharacterSpawner(instance, data.position, event.player, data)
                val pc = runtime.gameObjectManager.spawn(spawner, runtime) as PlayerCharacter
                pcsByPlayer[event.player] = pc
                event.player.inventory.setItemStack(8, ItemStack.builder(Material.IRON_SWORD).build())
            }.delay(TaskSchedule.seconds(3)).schedule()
        }

        globalEventHandler.addListener(PlayerDisconnectEvent::class.java, ::handlePlayerDisconnect)

        globalEventHandler.addListener(PlayerUseItemEvent::class.java) { event ->
            handlePlayerInteract(event, runtime)
        }

        globalEventHandler.addListener(PlayerHandAnimationEvent::class.java) { event ->
            val pc = getPlayerCharacter(event.player)
            runtime.collisionManager.raycastAll(
                pc.instance,
                pc.eyePosition,
                pc.position.direction,
                6.0
            ) { it is CharacterHitbox }.map {
                it.collider as CharacterHitbox
            }.map { it.character }.filter { it != pc }.forEach {
                it.damage(Damage(DamageType.PHYSICAL, 25.0), pc)
                it.applyImpulse(pc.position.direction * 100.0)
            }
        }

        globalEventHandler.addListener(PlayerStartSneakingEvent::class.java) {
            handleStartSneaking(it, runtime)
        }
        globalEventHandler.addListener(PlayerStopSneakingEvent::class.java) {
            handleStopSneaking(it, runtime)
        }

        globalEventHandler.addListener(PlayerChangeHeldSlotEvent::class.java) {
            handleChangeHeldSlot(it, runtime)
        }
    }

    fun getPlayerCharacter(player: Player) = pcsByPlayer.getValue(player)

    private fun handlePlayerDisconnect(event: PlayerDisconnectEvent) {
        val pc = pcsByPlayer.remove(event.player) ?: return
        pc.remove()
        pcsByPlayer.remove(event.player)
    }

    private fun handlePlayerInteract(event: PlayerUseItemEvent, runtime: Runtime) {
        val pc = pcsByPlayer[event.player] ?: return
        if (event.hand != Player.Hand.MAIN) return
        val hitbox = runtime.collisionManager.raycast(
            pc.instance,
            pc.eyePosition,
            pc.position.direction,
            INTERACT_DISTANCE,
        ) { collider ->
            collider != pc.hitbox &&
                    collider is CharacterHitbox &&
                    collider.character.isAlive &&
                    !collider.character.isInvisible
        }?.collider as? CharacterHitbox
        hitbox?.character?.interact(pc)
    }

    private val pcStartSneakTimes = mutableMapOf<PlayerCharacter, Long>()

    private fun handleStartSneaking(event: PlayerStartSneakingEvent, runtime: Runtime) {
        val pc = getPlayerCharacter(event.player)
        pcStartSneakTimes[pc] = runtime.timeMillis
    }

    private fun handleStopSneaking(event: PlayerStopSneakingEvent, runtime: Runtime) {
        val pc = getPlayerCharacter(event.player)
        val shouldRoll = runtime.timeMillis - pcStartSneakTimes.getValue(pc) < 300 &&
                pc.isOnGround
        if (shouldRoll) {
            var offset = pc.position.toVector3() - pc.previousPosition.toVector3()
            offset = offset.withY(0.0)
            if (offset == Vector3.ZERO) return
            offset = offset.normalized
            pc.velocity = offset * 20.0
            listOf(
                0L to Entity.Pose.SNEAKING,
                100L to Entity.Pose.SWIMMING,
                300L to Entity.Pose.SNEAKING,
                600L to Entity.Pose.STANDING
            ).forEach { (delay, pose) ->
                schedulerManager.buildTask {
                    event.player.pose = pose
                }.delay(TaskSchedule.millis(delay)).schedule()
            }
        }
        pcStartSneakTimes.remove(pc)
    }

    private fun handleChangeHeldSlot(event: PlayerChangeHeldSlotEvent, runtime: Runtime) {
        val pc = pcsByPlayer[event.player] ?: return
        event.isCancelled = true
        if (event.slot in 0..5) {
            pc.skillTracker.tryUseSkill(event.slot.toInt())
        } else if (event.slot in 6..7) {
            pc.inventory.tryUseConsumable(event.slot - 6)
        }
    }
}
