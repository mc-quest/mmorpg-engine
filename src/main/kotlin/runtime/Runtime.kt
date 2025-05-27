package net.mcquest.engine.runtime

import net.mcquest.engine.character.CharacterBlueprint
import net.mcquest.engine.character.CharacterEvents
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.login.LoginManager
import net.mcquest.engine.music.Song
import net.mcquest.engine.quest.Quest
import net.mcquest.engine.quest.QuestObjectiveManager
import net.mcquest.engine.resource.Resources
import net.mcquest.engine.script.loadScriptLibrary
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.zone.Zone
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import org.python.util.PythonInterpreter

class Runtime(resources: Resources) {
    private val server = resources.server
    val interpreter = PythonInterpreter()
    val config = resources.config
    val instancesById = resources.instances.associateBy(Instance::id)
    val questsById = resources.quests.associateBy(Quest::id)
    val musicById = resources.music.associateBy(Song::id)
    val characterBlueprintsById = resources.characterBlueprints.associateBy(CharacterBlueprint::id)
    val zonesById = resources.zones.associateBy(Zone::id)
    val questObjectiveManager = QuestObjectiveManager()
    val loginManager = LoginManager(this)
    val characterEvents = CharacterEvents(this)

    var timeMillis = 0L
        private set

    init {
        interpreter.systemState.path.add(resources.scriptDir.path)
    }

    fun start() {
        loadScriptLibrary(interpreter, this)
        instancesById.values.forEach(Instance::start)
        questsById.values.forEach { it.start(this) }
        loginManager.start()
        characterEvents.start()

        schedulerManager.buildTask(::tick)
            .repeat(TaskSchedule.tick(1))
            .schedule()

        server.start("0.0.0.0", 25565)
    }

    private fun tick() {
        instancesById.values.forEach { it.tick(this) }

        timeMillis += MinecraftServer.TICK_MS
    }
}
