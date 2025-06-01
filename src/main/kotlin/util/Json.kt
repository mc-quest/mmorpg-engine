package com.shadowforgedmmo.engine.util

import com.google.gson.Gson
import java.io.InputStreamReader

private val gson = Gson()

fun <T> loadJsonResource(path: String, classOfT: Class<T>): T {
    val resource = object {}::class.java.classLoader.getResource(path)
        ?: error("Resource not found: $path")
    return InputStreamReader(resource.openStream()).use { reader ->
        gson.fromJson(reader, classOfT)
    }
}
