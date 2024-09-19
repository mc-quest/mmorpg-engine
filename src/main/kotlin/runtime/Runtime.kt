package net.mcquest.engine.runtime

import net.mcquest.engine.character.NonPlayerCharacterManager
import net.mcquest.engine.character.PlayerCharacterManager
import net.mcquest.engine.collision.CollisionManager
import net.mcquest.engine.gameobject.GameObjectManager
import net.mcquest.engine.instance.InstanceManager
import net.mcquest.engine.login.LoginManager
import net.mcquest.engine.model.ModelManager
import net.mcquest.engine.music.MusicManager
import net.mcquest.engine.particle.ParticleManager
import net.mcquest.engine.quest.QuestManager
import net.mcquest.engine.resource.Resources
import net.mcquest.engine.script.ScriptLibrary
import net.mcquest.engine.sound.SoundManager
import net.mcquest.engine.util.schedulerManager
import net.mcquest.engine.zone.ZoneManager
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule

class Runtime(resources: Resources) {
    val server = resources.server
    val config = resources.config
    val instanceManager = InstanceManager(resources.instances)
    val questManager = QuestManager(resources.quests)
    val musicManager = MusicManager(resources.music)
    val modelManager = ModelManager(resources.blockbenchModels, resources.blockbenchItemModels)
    val zoneManager = ZoneManager(resources.zones)
    val gameObjectManager = GameObjectManager(resources.spawners)
    val nonPlayerCharacterManager = NonPlayerCharacterManager(resources.characterBlueprints)
    val playerCharacterManager = PlayerCharacterManager()
    val collisionManager = CollisionManager()
    val loginManager = LoginManager()
    val soundManager = SoundManager()
    val particleManager = ParticleManager()
    val interpreter = resources.interpreter

    var timeMillis = 0L
        private set

    init {
        ScriptLibrary(this).load(interpreter)
    }

    fun start() {
        instanceManager.start()
        questManager.start(this)
        modelManager.start()
        loginManager.start(this)
        gameObjectManager.start(this)
        playerCharacterManager.start(this)

        schedulerManager.buildTask(::tick)
            .repeat(TaskSchedule.tick(1))
            .schedule()

        server.start("0.0.0.0", 25565)
    }

    private fun tick() {
        instanceManager.tick(this)
        gameObjectManager.tick(this)

        timeMillis += MinecraftServer.TICK_MS
    }
}
