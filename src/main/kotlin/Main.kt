package net.mcquest.engine

import net.mcquest.engine.editor.Editor
import net.mcquest.engine.pack.PackBuilder
import net.mcquest.engine.resource.ResourceLoader
import net.mcquest.engine.runtime.Runtime
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 2) usage()

    val command = args[0]
    val root = File(args[1])

    when (command) {
        "runtime" -> startRuntime(root)
        "pack" -> buildPack(root)
        "editor" -> startEditor(root)
        else -> usage()
    }
}

fun startRuntime(root: File) {
    val resources = ResourceLoader(root).loadAll()
    val runtime = Runtime(resources)
    runtime.start()
}

fun buildPack(root: File) = PackBuilder(root).build()

fun startEditor(root: File) = Editor(root).start()

fun usage() {
    System.err.println("Usage: <runtime|pack|editor> <path/to/project>")
    exitProcess(1)
}
