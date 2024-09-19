package net.mcquest.engine.character

import net.kyori.adventure.text.Component
import net.mcquest.engine.ai.navigation.Navigator
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.util.toMinestom
import org.python.core.Py
import team.unnamed.hephaestus.minestom.MinestomModelEngine
import team.unnamed.hephaestus.minestom.ModelEntity
import net.mcquest.engine.script.NonPlayerCharacter as ScriptNonPlayerCharacter

class NonPlayerCharacter(
    runtime: Runtime,
    spawner: NonPlayerCharacterSpawner
) : Character(runtime, spawner) {
    val blueprint
        get() = (spawner as NonPlayerCharacterSpawner).blueprint
    override val name
        get() = blueprint.name
    override val level
        get() = blueprint.level
    override val maxHealth
        get() = blueprint.maxHealth
    override var health = maxHealth
    override val mass
        get() = blueprint.mass
    override val entity = blueprint.model.createEntity()
    val navigator = Navigator(this)
    val behavior = blueprint.behavior?.create()
    override val hitbox = CharacterHitbox(this)
    var target: Character? = null
    private val bossFight = blueprint.bossFight?.create()
    private val interactionIndices = mutableMapOf<Pair<PlayerCharacter, Interaction>, Int>()
    private val attackers = mutableSetOf<PlayerCharacter>()
    override val handle = blueprint.script.__call__(Py.java2py(this))
        .__tojava__(ScriptNonPlayerCharacter::class.java) as ScriptNonPlayerCharacter

    override fun spawn() {
        super.spawn()
        entity.setInstance(instance.instanceContainer, position.toMinestom()).join()
        if (entity is ModelEntity) {
            MinestomModelEngine.minestom().tracker().startGlobalTracking(entity)
        }
        bossFight?.init(this)
        handle.on_spawn()
    }

    override fun despawn() {
        super.despawn()
        bossFight?.remove()
        handle.on_despawn()
    }

    override fun tick() {
        super.tick()
        if (isAlive and interactionIndices.isEmpty()) behavior?.tick(this)
        bossFight?.tick(this)
        handle.tick()
    }

    override fun interact(pc: PlayerCharacter) {
        val availableInteractions = blueprint.interactions.filter {
            it.isAvailable(pc)
        }
        val interaction = availableInteractions.firstOrNull()

        if (interaction == null) {
            blueprint.speakSound?.let { pc.playSound(it, position.toVector3()) }
            return
        }

        val index = interactionIndices.getOrDefault(pc to interaction, 0)
        lookAt(pc)
        if (interaction.advance(this, pc, index)) {
            interactionIndices.remove(pc to interaction)
            pc.enableMovement()
        } else {
            interactionIndices[pc to interaction] = index + 1
            pc.disableMovement()
        }
    }

    override fun speak(dialogue: Component, to: PlayerCharacter) {
        super.speak(dialogue, to)
        blueprint.speakSound?.let { to.playSound(it, position.toVector3()) }
    }

    override fun getStance(toward: Character) = blueprint.stances.stance(toward)

    override fun die() {
        navigator.reset()
        if (entity is ModelEntity) {
            entity.animationPlayer().clear()
        }
        playAnimation(ANIMATION_DEATH)
        blueprint.deathSound?.let(::emitSound)
        schedulerManager.buildTask(::finalizeDeath)
            .delay(blueprint.removalDelay)
            .schedule()
        attackers.forEach {
            runtime.questManager.handleCharacterDeath(it, this)
        }
    }

    private fun finalizeDeath() {
        // TODO: loot
        attackers.forEach {
            it.addExperiencePoints(blueprint.experiencePoints / attackers.size)
        }
        remove()
    }
}
