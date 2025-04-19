package net.mcquest.engine.world

import net.mcquest.engine.resource.parseId

fun parseWorldId(id: String) = parseId(id, "worlds")

fun worldIdToWorldPath(id: String) = id.replace(".", "/")
