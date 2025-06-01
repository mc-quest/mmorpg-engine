package com.shadowforgedmmo.engine

import com.shadowforgedmmo.engine.pack.PackBuilder
import com.shadowforgedmmo.engine.resource.ResourceLoader
import com.shadowforgedmmo.engine.runtime.Runtime
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 2) usage()

    val command = args[0]
    val root = File(args[1])

    when (command) {
        "runtime" -> startRuntime(root)
        "pack" -> buildPack(root)
        else -> usage()
    }
}

fun startRuntime(root: File) {
    val resources = ResourceLoader(root).loadAll()
    val runtime = Runtime(resources)
    runtime.start()
}

fun buildPack(root: File) {
    val resourceLoader = ResourceLoader(root)
    val packBuilder = PackBuilder(resourceLoader)
    packBuilder.build()
}

fun usage() {
    System.err.println("Usage: <runtime|pack|editor> <path/to/project>")
    exitProcess(1)
}
