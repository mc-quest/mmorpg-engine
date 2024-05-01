package net.mcquest.engine.ai.behavior.decorator

import net.mcquest.engine.ai.behavior.Behavior
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Decorator
import net.mcquest.engine.character.NonPlayerCharacter

class Inverter(child: Behavior) : Decorator(child) {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        return when (child.tick(character)) {
            BehaviorStatus.SUCCESS -> BehaviorStatus.FAILURE
            BehaviorStatus.FAILURE -> BehaviorStatus.SUCCESS
            else -> status
        }
    }
}

class InverterBlueprint(
    private val child: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = Inverter(child.create())
}
