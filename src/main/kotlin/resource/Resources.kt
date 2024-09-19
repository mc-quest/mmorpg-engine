package net.mcquest.engine.resource

import net.mcquest.engine.character.CharacterBlueprint
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.model.BlockbenchItemModel
import net.mcquest.engine.model.BlockbenchModel
import net.mcquest.engine.music.Song
import net.mcquest.engine.quest.Quest
import net.mcquest.engine.zone.Zone
import net.minestom.server.MinecraftServer
import org.python.util.PythonInterpreter

class Resources(
    val server: MinecraftServer,
    val config: Config,
    val instances: Collection<Instance>,
    val quests: Collection<Quest>,
    val music: Collection<Song>,
    val blockbenchModels: Collection<BlockbenchModel>,
    val blockbenchItemModels: Collection<BlockbenchItemModel>,
    val characterBlueprints: Collection<CharacterBlueprint>,
    val zones: Collection<Zone>,
    val spawners: Collection<GameObjectSpawner>,
    val interpreter: PythonInterpreter
)
