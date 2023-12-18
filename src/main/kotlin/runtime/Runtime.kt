package net.mcquest.engine.runtime

import net.minestom.server.MinecraftServer
import java.nio.file.Path

class Runtime(val path: Path) {
    fun start() {
        val server = MinecraftServer.init()
        server.start("0.0.0.0", 25565)
    }
}
