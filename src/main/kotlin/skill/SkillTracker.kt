package net.mcquest.engine.skill

import net.kyori.adventure.text.Component
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.time.Cooldown
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag

private val SKILL_TAG = Tag.Transient<Skill>("skill")

class SkillTracker(private val pc: PlayerCharacter) {
    private val cooldowns = mutableMapOf<Skill, Cooldown>()
    private val skillExecutors = mutableListOf<SkillExecutor>()

    fun tryUseSkill(slot: Int) {
        val skill = hotbarSkill(slot) ?: return
        tryUseSkill(skill)
    }

    private fun hotbarSkill(slot: Int): Skill? =
        pc.entity.inventory.getItemStack(slot).getTag(SKILL_TAG)

    private fun tryUseSkill(skill: Skill) {
        val cooldown = cooldown(skill)
        if (cooldown != null)
            return failUseSkill(Component.text("On cooldown")) // TODO

        if (pc.mana < skill.manaCost)
            return failUseSkill(Component.text("Not enough mana")) // TODO

        useSkill(skill)
    }

    private fun failUseSkill(message: Component) {
        pc.sendMessage(message)
        // TODO: play sound
    }

    private fun useSkill(skill: Skill) {
        val skillExecutor = SkillExecutor(pc, skill, pc.runtime.timeMillis)
        skillExecutor.beginCast()
        skillExecutors.add(skillExecutor)
    }

    fun tick() {
        skillExecutors.forEach(SkillExecutor::tick)
        skillExecutors.removeIf(SkillExecutor::completed)
        updateHotbar();
    }

    private fun updateHotbar() = (0..5).forEach(::updateHotbarSlot)

    private fun updateHotbarSlot(slot: Int) {
        // val skill = hotbarSkill(slot) ?: return
        pc.entity.inventory.setItemStack(slot, ItemStack.of(Material.BARRIER))
    }

    private fun cooldown(skill: Skill) = cooldowns[skill]
}
