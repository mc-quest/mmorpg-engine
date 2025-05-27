package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import org.python.core.Py
import org.python.core.PyBoolean

class Custom(private val methodName: String) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val status = Py.java2py(character.handle).invoke(methodName)
        if (status is PyBoolean) {
            return if (status.booleanValue) BehaviorStatus.SUCCESS else BehaviorStatus.FAILURE
        }
        return BehaviorStatus.SUCCESS
    }
}

class CustomBlueprint(private val methodName: String) : BehaviorBlueprint() {
    override fun create() = Custom(methodName)
}

fun deserializeCustom(data: JsonNode) = CustomBlueprint(data["method"].asText())
