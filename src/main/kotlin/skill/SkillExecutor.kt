package net.mcquest.engine.skill

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.script.getScriptClass
import net.mcquest.engine.script.SkillExecutor as ScriptSkillExecutor
import org.python.core.Py

class SkillExecutor(
    val user: PlayerCharacter,
    private val skill: Skill,
    private val startTimeMillis: Long
) {
    private val handle = getScriptClass(
        skill.scriptId,
        user.runtime.interpreter
    ).__call__(Py.java2py(this)).__tojava__(ScriptSkillExecutor::class.java) as ScriptSkillExecutor

    var completed = false
        private set

    val lifetimeMillis
        get() = user.runtime.timeMillis - startTimeMillis

    fun init() = handle.init()

    fun tick() {
        handle.tick()
        if (completed) {
            TODO()
        }
    }

    fun complete() {
        completed = true
    }
}
