@file:Suppress("FunctionName", "PropertyName", "unused")

package com.shadowforgedmmo.engine.script

import com.shadowforgedmmo.engine.combat.DamageType
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.skill.SkillStatus
import com.shadowforgedmmo.engine.time.millisToSeconds
import com.shadowforgedmmo.engine.time.secondsToDuration
import com.shadowforgedmmo.engine.util.schedulerManager
import org.python.core.*
import org.python.util.PythonInterpreter
import com.shadowforgedmmo.engine.character.Character as EngineCharacter
import com.shadowforgedmmo.engine.character.NonPlayerCharacter as EngineNonPlayerCharacter
import com.shadowforgedmmo.engine.character.PlayerCharacter as EnginePlayerCharacter
import com.shadowforgedmmo.engine.instance.Instance as EngineInstance
import com.shadowforgedmmo.engine.skill.SkillExecutor as EngineSkillExecutor
import net.minestom.server.timer.Task as EngineTask

const val moduleName = "shadowforged_engine"

fun loadScriptLibrary(interpreter: PythonInterpreter, runtime: Runtime) {
    val classes = mapOf(
        "Point" to Point::class,
        "Vector" to Vector::class,
        "Position" to Position::class,
        "Instance" to Instance::class,
        "NonPlayerCharacter" to NonPlayerCharacter::class,
        "SkillExecutor" to SkillExecutor::class,
        "SkillStatus" to SkillStatus::class,
        "DamageType" to DamageType::class
    ).mapValues { Py.java2py(it.value.java) }
    val functions = mapOf(
        "get_time" to GetTime(runtime),
        "run_delayed" to RunDelayed(runtime)
    )
    val module = PyModule(moduleName, PyStringMap(classes + functions))
    interpreter.systemState.modules.__setitem__(moduleName, module)
}

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

class Instance(val handle: EngineInstance) {
    val id: String
        get() = handle.id

    fun play_sound(position: Point, sound: Sound) = handle.playSound(
        ScriptToEngine.vector3(position),
        ScriptToEngine.sound(sound)
    )

    fun spawn_character(position: Point, character: String) = Unit
}

class Task(private val handle: EngineTask) {
    fun cancel() = handle.cancel()
}

data class Sound(val name: String, val volume: Float, val pitch: Float)

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
    open fun tick() = Unit

    open fun on_spawn() = Unit

    open fun on_despawn() = Unit
}

open class SkillExecutor(private val handle: EngineSkillExecutor) {
    private val user
        get() = handle.user.handle

    private val lifetime
        get() = millisToSeconds(handle.lifetimeMillis)

    fun complete() = handle.complete()

    open fun init() = Unit

    open fun tick() = Unit
}

class GetTime(val runtime: Runtime) : PyObject() {
    override fun __call__(): PyObject = Py.java2py(millisToSeconds(runtime.timeMillis))
}

class RunDelayed(val runtime: Runtime) : PyObject() {
    override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
        val delay = args[0].asDouble()
        val function = args[1]
        return Py.java2py(
            Task(
                schedulerManager.buildTask(function::__call__).delay(secondsToDuration(delay)).schedule()
            )
        )
    }
}
