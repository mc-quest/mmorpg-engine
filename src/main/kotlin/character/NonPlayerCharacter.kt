package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.ai.navigation.Navigator
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.script.idToPythonClassName
import com.shadowforgedmmo.engine.util.schedulerManager
import net.kyori.adventure.text.Component
import net.minestom.server.particle.Particle
import org.python.core.Py
import com.shadowforgedmmo.engine.script.NonPlayerCharacter as ScriptNonPlayerCharacter

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
    override val handle = blueprint.scriptId?.let {
        // TODO: make this script instantiation reusable for SkillExecutors, etc.
        val scriptClassName = idToPythonClassName(it)
        runtime.interpreter.exec("from $it import $scriptClassName")
        val scriptClass = runtime.interpreter.get(scriptClassName)
        scriptClass.__call__(Py.java2py(this))
            .__tojava__(ScriptNonPlayerCharacter::class.java) as ScriptNonPlayerCharacter
    } ?: ScriptNonPlayerCharacter(this)

    override fun spawn() {
        super.spawn()
        if (entity is BlockbenchCharacterModelEntity) entity.spawnHitbox()
        bossFight?.init()
        handle.on_spawn()
    }

    override fun despawn() {
        super.despawn()
        if (entity is BlockbenchCharacterModelEntity) entity.removeHitbox()
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

    override fun damage(damage: Damage, source: Character) {
        if (source is PlayerCharacter) attackers.add(source)
        super.damage(damage, source)
    }

    override fun die() {
        navigator.reset()
        if (entity is BlockbenchCharacterModelEntity) entity.animationPlayer().clear()
        playAnimation(ANIMATION_DEATH)
        blueprint.deathSound?.let(::emitSound)
        attackers.forEach { runtime.questObjectiveManager.handleCharacterDeath(it, this) }
        schedulerManager.buildTask(::finalizeDeath).delay(blueprint.removalDelay).schedule()
    }

    private fun distributeExperiencePoints() {
        if (attackers.isEmpty()) return
        val experiencePointsEach = blueprint.experiencePoints / attackers.size
        if (experiencePointsEach == 0) return
        val experiencePointsPosition = position.toVector3() + Vector3.UP * height / 2.0
        attackers.forEach { it.addExperiencePoints(experiencePointsEach, experiencePointsPosition) }
    }

    private fun finalizeDeath() {
        // TODO: loot
        distributeExperiencePoints()
        remove()
        spawnDeathParticles()
    }

    private fun spawnDeathParticles() = instance.spawnParticle(
        position.toVector3() - Vector3.ONE / 4.0 + Vector3.UP,
        Particle.POOF,
        offset = Vector3.ONE / 2.0,
        maxSpeed = 0.1,
        count = 10
    )
}
