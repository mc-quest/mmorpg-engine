package net.mcquest.engine.skill

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.script.SkillExecutor as ScriptSkillExecutor

class SkillExecutor(
    private val pc: PlayerCharacter,
    private val skill: Skill,
    private val startTimeMillis: Long
) {
    private val handle = skill.script.__call__()
        .__tojava__(ScriptSkillExecutor::class.java) as ScriptSkillExecutor

    var completed = false
        private set

    val lifetimeMillis
        get() = pc.runtime.timeMillis - startTimeMillis

    fun beginCast() = handle.begin_cast()

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
