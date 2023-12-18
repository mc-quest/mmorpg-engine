package net.mcquest.engine

import net.mcquest.engine.editor.Editor
import net.mcquest.engine.pack.PackBuilder
import net.mcquest.engine.runtime.Runtime
import kotlin.io.path.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 2) usage()

    val command = args[0]
    val path = Path(args[1])

    when (command) {
        "runtime" -> Runtime(path).start()
        "pack" -> PackBuilder(path).build()
        "editor" -> Editor(path).start()
        else -> usage()
    }
}

fun usage() {
    println("Usage: mmorpg-engine <runtime|pack|editor> <path/to/project>")
    exitProcess(1)
}
