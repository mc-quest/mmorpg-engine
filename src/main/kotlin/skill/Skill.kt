package net.mcquest.engine.skill

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.resource.loadScript
import net.mcquest.engine.script.SkillExecutor as ScriptSkillExecutor
import org.python.core.PyType
import org.python.util.PythonInterpreter

class Skill(
    val id: String,
    val name: String,
    val description: String,
    val script: PyType
) {
    val manaCost = 0.0 // TODO
}

fun deserializeSkill(
    id: String,
    data: JsonNode,
    interpreter: PythonInterpreter
) = Skill(
    id,
    data["name"].asText(),
    data["description"].asText(),
    loadScript(ScriptSkillExecutor::class, data["script"].asText(), id, interpreter)
)
