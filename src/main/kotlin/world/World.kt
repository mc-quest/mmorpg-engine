package com.shadowforgedmmo.engine.world

import com.shadowforgedmmo.engine.resource.parseId

fun parseWorldId(id: String) = parseId(id, "worlds")

fun worldIdToWorldPath(id: String) = id.replace(".", "/")
