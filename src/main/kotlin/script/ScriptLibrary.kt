@file:Suppress("FunctionName", "PropertyName", "unused")

package net.mcquest.engine.script

import net.mcquest.engine.character.NonPlayerCharacterSpawner
import net.mcquest.engine.character.parseCharacterBlueprintId
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
        interpreter["get_time"] = GetTime()
        interpreter["spawn_character"] = SpawnCharacter()
        interpreter["spawn_particle"] = SpawnParticle()
        interpreter["play_sound"] = PlaySound()
        interpreter["run_delayed"] = RunDelayed()
    }

    private inner class GetTime : PyObject() {
        override fun __call__(): PyObject = Py.java2py(millisToSeconds(runtime.timeMillis))
    }

    private inner class SpawnCharacter : PyObject() {
        override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
            val instance = args[0].__tojava__(Instance::class.java) as Instance
            val position = args[1].__tojava__(Position::class.java) as Position
            val blueprintId = parseCharacterBlueprintId(args[2].asString())
            val blueprint = runtime.nonPlayerCharacterManager.getBlueprint(blueprintId)
            val character = runtime.gameObjectManager.spawn(
                NonPlayerCharacterSpawner(
                    instance.handle,
                    ScriptToEngine.position(position),
                    blueprint
                ),
                runtime
            ) as EngineNonPlayerCharacter
            return Py.java2py(character.handle)
        }
    }

    private inner class PlaySound : PyObject() {
        override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
            val instance = args[0].__tojava__(Instance::class.java) as Instance
            val position = args[1].__tojava__(Vector::class.java) as Vector
            val sound = args[2].__tojava__(Sound::class.java) as Sound
            runtime.soundManager.playSound(
                instance.handle,
                ScriptToEngine.vector3(position),
                ScriptToEngine.sound(sound)
            )
            return Py.None
        }
    }

    private inner class SpawnParticle : PyObject() {
        override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
            val instance = args[0].__tojava__(Instance::class.java) as Instance
            val position = args[1].__tojava__(Vector::class.java) as Vector
            val particle = args[2].asString()
            Particle.fromNamespaceId(particle)?.let {
                runtime.particleManager.spawnParticle(
                    it,
                    instance.handle,
                    ScriptToEngine.vector3(position)
                )
            }
            return Py.None
        }
    }

    private inner class RunDelayed : PyObject() {
        override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
            val task = args[0]
            val delay = args[1].asDouble()
            return Py.java2py(
                Task(
                    schedulerManager.buildTask(task::__call__).delay(secondsToDuration(delay)).schedule()
                )
            )
        }
    }
}

class Instance(val handle: EngineInstance) {
    val id: String
        get() = handle.id

    fun spawn_character(position: Position, character: String) = Unit
}

class Task(private val handle: EngineTask) {
    fun cancel() = handle.cancel()
}

data class Sound(val name: String, val volume: Float, val pitch: Float)

interface Point {
    val x: Double
    val y: Double
    val z: Double
}

data class Vector(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : Point {
    companion object {
        val ZERO = Vector(0.0, 0.0, 0.0)
        val ONE = Vector(1.0, 1.0, 1.0)
        val LEFT = Vector(1.0, 0.0, 0.0)
        val RIGHT = Vector(-1.0, 0.0, 0.0)
        val UP = Vector(0.0, 1.0, 0.0)
        val DOWN = Vector(0.0, -1.0, 0.0)
        val FORWARD = Vector(0.0, 0.0, 1.0)
        val BACK = Vector(0.0, 0.0, -1.0)
    }

    fun __add__(v: Vector) = Vector(x + v.x, y + v.y, z + v.z)

    fun __sub__(v: Vector) = Vector(x - v.x, y - v.y, z - v.z)

    fun __mul__(s: Double) = Vector(x * s, y * s, z * s)
}

data class Position(
    override val x: Double,
    override val y: Double,
    override val z: Double,
    val yaw: Double,
    val pitch: Double
) : Point {
    fun __add__(v: Vector) = Position(x + v.x, y + v.y, z + v.z, yaw, pitch)

    fun __sub__(v: Vector) = Position(x - v.x, y - v.y, z - v.z, yaw, pitch)
}

open class Character(
    private val handle: EngineCharacter
) {
    val is_on_ground
        get() = handle.isOnGround

    val instance
        get() = handle.instance.handle

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

open class SkillExecutor(private val handle: EngineSkillExecutor) {
    private val lifetime
        get() = millisToSeconds(handle.lifetimeMillis)

    fun complete() = handle.complete()

    open fun init() = Unit

    open fun tick() = Unit
}

fun loadScriptClasses(interpreter: PythonInterpreter) {
    interpreter["Vector"] = Vector::class.java
    interpreter["Position"] = Position::class.java
    interpreter["Sound"] = Sound::class.java
    interpreter["NonPlayerCharacter"] = NonPlayerCharacter::class.java
    interpreter["SkillExecutor"] = SkillExecutor::class.java
}
