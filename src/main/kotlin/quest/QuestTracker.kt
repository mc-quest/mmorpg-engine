package net.mcquest.engine.quest

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.pack.Namespaces
import net.mcquest.engine.persistence.QuestTrackerData
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine

class QuestTracker(
    val pc: PlayerCharacter,
    val data: QuestTrackerData
) {
    private val progressByQuest = mutableMapOf<Quest, Array<Int>>() // TODO: Load from data
    private val trackedQuests = mutableListOf<Quest>() // TODO: Load from data

    fun start() {
        updateSidebar()
    }

    fun startQuest(quest: Quest) {
        if (quest in progressByQuest) error("Quest already started")

        progressByQuest[quest] = Array(quest.objectives.size) { 0 }
        trackedQuests += quest

        pc.entity.showTitle(
            Title.title(
                Component.text("Quest Started", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        // TODO: clean up
        pc.playSound(
            Sound.sound(
                Key.key(Namespaces.ENGINE_AUDIO_CLIPS, "quest_start"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
            )
        )

        updateSidebar()
    }

    fun completeQuest(quest: Quest) {
        progressByQuest -= quest
        trackedQuests -= quest

        pc.entity.showTitle(
            Title.title(
                Component.text("Quest Completed", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        pc.playSound(
            Sound.sound(
                Key.key(Namespaces.ENGINE_AUDIO_CLIPS, "quest_complete"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
            )
        )

        updateSidebar()
    }

    fun isInProgress(quest: Quest) = quest in progressByQuest

    fun isInProgress(quest: Quest, objectiveIndex: Int) = isInProgress(quest) &&
            getProgress(quest, objectiveIndex) < quest.objectives[objectiveIndex].goal

    fun getProgress(quest: Quest, objectiveIndex: Int) =
        progressByQuest.getValue(quest)[objectiveIndex]

    fun addProgress(quest: Quest, objectiveIndex: Int, progress: Int) {
        if (quest !in progressByQuest) return
        val oldProgress = progressByQuest.getValue(quest)[objectiveIndex]
        val newProgress = (oldProgress + progress)
            .coerceAtMost(quest.objectives[objectiveIndex].goal)
        if (oldProgress == newProgress) return
        progressByQuest.getValue(quest)[objectiveIndex] = newProgress
        updateSidebar()
    }

    fun incrementProgress(quest: Quest, objectiveIndex: Int) = addProgress(
        quest,
        objectiveIndex,
        1
    )

    fun isReadyToStart(quest: Quest) = pc.level >= quest.level &&
            quest !in progressByQuest &&
            quest.prerequisiteIds.all {
                it in progressByQuest.keys.map(Quest::id)
            }

    fun isReadyToTurnIn(quest: Quest) = quest in progressByQuest &&
            quest.objectives.withIndex().all { (index, objective) ->
                getProgress(quest, index) == objective.goal
            }

    private fun updateSidebar() {
        sidebar().addViewer(pc.entity)
    }

    private fun sidebar(): Sidebar {
        val sidebar = Sidebar(Component.text("Quests", NamedTextColor.YELLOW))
        val sidebarContent = sidebarContent()
        val numLines = minOf(15, sidebarContent.size)
        for (i in 0..<numLines) {
            val lineId = i.toString()
            val line = numLines - i - 1
            sidebar.createLine(ScoreboardLine(lineId, sidebarContent[i], line))
        }
        return sidebar
    }

    private fun sidebarContent(): List<Component> =
        trackedQuests.flatMapIndexed(::sidebarQuestContent)

    private fun sidebarQuestContent(index: Int, quest: Quest) = listOf(
        Component.text("(", NamedTextColor.YELLOW)
            .append(Component.text("${index + 1}").decorate(TextDecoration.BOLD))
            .append(Component.text(") ${quest.name}")),
        *quest.objectives.mapIndexed { objectiveIndex, objective ->
            sidebarObjectiveContent(quest, objectiveIndex, objective)
        }.toTypedArray()
    )

    private fun sidebarObjectiveContent(
        quest: Quest,
        objectiveIndex: Int,
        objective: QuestObjective
    ) = Component.empty().append(
        Component.text(
            " • ${getProgress(quest, objectiveIndex)}/${objective.goal} ",
            NamedTextColor.YELLOW,
            TextDecoration.BOLD
        )
    ).append(Component.text(objective.description, NamedTextColor.WHITE))
}
