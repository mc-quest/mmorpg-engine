@file:Suppress("FunctionName", "PropertyName", "unused")

package net.mcquest.engine.script

import net.mcquest.engine.character.NonPlayerCharacterSpawner
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.time.millisToSeconds
import net.mcquest.engine.time.secondsToDuration
import net.mcquest.engine.util.schedulerManager
import net.minestom.server.particle.Particle
import org.python.core.*
import org.python.util.PythonInterpreter
import net.mcquest.engine.character.Character as EngineCharacter
import net.mcquest.engine.character.NonPlayerCharacter as EngineNonPlayerCharacter
import net.mcquest.engine.character.PlayerCharacter as EnginePlayerCharacter
import net.mcquest.engine.instance.Instance as EngineInstance
import net.mcquest.engine.skill.SkillExecutor as EngineSkillExecutor
import net.minestom.server.timer.Task as EngineTask

class ScriptLibrary(val runtime: Runtime) {
    fun load(interpreter: PythonInterpreter) {
        interpreter.set("get_time", GetTime())
        interpreter.set("spawn_character", this::spawn_character)
        interpreter.set("spawn_particle", this::spawn_particle)
        interpreter.set("play_sound", this::play_sound)
        interpreter.set("run_delayed", this::run_delayed)
    }

    private inner class GetTime : PyBuiltinFunction("get_time", null) {
        override fun __call__(): PyObject = Py.java2py(millisToSeconds(runtime.timeMillis))
    }

    private fun spawn_character(character: String, instance: Instance, position: Position) =
        runtime.gameObjectManager.spawn(
            NonPlayerCharacterSpawner(
                convertInstance(instance, runtime),
                ScriptToEngine.position(position),
                runtime.nonPlayerCharacterManager.getBlueprint(character)
            ),
            runtime
        )

    private fun play_sound(instance: Instance, position: Vector3, sound: Sound) =
        runtime.soundManager.playSound(
            instance.handle,
            ScriptToEngine.vector3(position),
            ScriptToEngine.sound(sound)
        )

    private fun spawn_particle(particle: String, instance: Instance, position: Vector3) =
        runtime.particleManager.spawnParticle(
            Particle.fromNamespaceId(particle) ?: throw IllegalArgumentException(),
            convertInstance(instance, runtime),
            ScriptToEngine.vector3(position)
        )

    private fun run_delayed(task: PyFunction, seconds: Double) = Task(
        schedulerManager.buildTask(task::__call__)
            .delay(secondsToDuration(seconds))
            .schedule()
    )
}

class Instance(val handle: EngineInstance) {
    val id: String
        get() = handle.id
}

class Task(private val handle: EngineTask) {
    fun cancel() = handle.cancel()
}

data class Sound(val name: String, val volume: Float, val pitch: Float)

data class Vector3(val x: Double, val y: Double, val z: Double) {
    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
        val ONE = Vector3(1.0, 1.0, 1.0)
        val LEFT = Vector3(1.0, 0.0, 0.0)
        val RIGHT = Vector3(-1.0, 0.0, 0.0)
        val UP = Vector3(0.0, 1.0, 0.0)
        val DOWN = Vector3(0.0, -1.0, 0.0)
        val FORWARD = Vector3(0.0, 0.0, 1.0)
        val BACK = Vector3(0.0, 0.0, -1.0)
    }
}

data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double,
    val pitch: Double
) {
    private fun to_vector3() = Vector3(x, y, z)
}

open class Character(
    private val handle: EngineCharacter
) {
    val is_on_ground
        get() = handle.isOnGround

    val position
        get() = EngineToScript.position(handle.position)
}

class PlayerCharacter(
    private val handle: EnginePlayerCharacter
) : Character(handle) {
}

open class NonPlayerCharacter(
    private val handle: EngineNonPlayerCharacter
) : Character(handle) {
    fun play_animation(animation: String) = handle.playAnimation(animation)

    open fun tick() = Unit

    open fun on_spawn() = Unit

    open fun on_despawn() = Unit
}

// TODO: remove this
private fun convertInstance(instance: Instance, runtime: Runtime) =
    runtime.instanceManager.getInstance(instance.id)

open class SkillExecutor(private val handle: EngineSkillExecutor) {
    private val lifetime
        get() = millisToSeconds(handle.lifetimeMillis)

    fun complete() = handle.complete()

    open fun begin_cast() = Unit

    open fun tick() = Unit
}

fun loadScriptClasses(interpreter: PythonInterpreter) {
    interpreter.set("Vector3", Vector3::class.java)
    interpreter.set("Position", Position::class.java)
    interpreter.set("Sound", Sound::class.java)
    interpreter.set("NonPlayerCharacter", NonPlayerCharacter::class.java)
    interpreter.set("SkillExecutor", SkillExecutor::class.java)
}
