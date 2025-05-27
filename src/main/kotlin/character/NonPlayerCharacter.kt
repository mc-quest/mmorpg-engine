package net.mcquest.engine.character

import net.kyori.adventure.text.Component
import net.mcquest.engine.ai.navigation.Navigator
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.script.SkillExecutor
import net.mcquest.engine.script.getScriptClass
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.util.toMinestom
import org.python.core.Py
import team.unnamed.hephaestus.minestom.MinestomModelEngine
import team.unnamed.hephaestus.minestom.ModelEntity
import net.mcquest.engine.script.NonPlayerCharacter as ScriptNonPlayerCharacter

class NonPlayerCharacter(
    spawner: NonPlayerCharacterSpawner,
    instance: Instance,
    runtime: Runtime
) : Character(spawner, instance, runtime, spawner.summoner) {
    val blueprint
        get() = (spawner as NonPlayerCharacterSpawner).blueprint
    override val entity = blueprint.model.createEntity()
    override val name
        get() = blueprint.name
    override val level
        get() = blueprint.level
    override val maxHealth
        get() = blueprint.maxHealth
    override var health = maxHealth
    override val mass
        get() = blueprint.mass
    val navigator = Navigator(this)
    val behavior = blueprint.behavior?.create()
    var target: Character? = null
    private val bossFight = blueprint.bossFight?.create(this)
    private val interactionIndices = mutableMapOf<Pair<PlayerCharacter, Interaction>, Int>()
    private val attackers = mutableSetOf<PlayerCharacter>()
    override val handle = ScriptNonPlayerCharacter(this)

    override fun spawn() {
        super.spawn()
        bossFight?.init()
        handle.on_spawn()
    }

    override fun despawn() {
        super.despawn()
        bossFight?.remove()
        handle.on_despawn()
    }

    override fun tick() {
        super.tick()
        if (isAlive and interactionIndices.isEmpty()) {
            navigator.tick()
            behavior?.tick(this)
        }
        bossFight?.tick()
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
            interactionIndices[pc to interaction] = index + 1
            pc.disableMovement()
        } else {
            interactionIndices.remove(pc to interaction)
            pc.enableMovement()
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
            runtime.questObjectiveManager.handleCharacterDeath(it, this)
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
