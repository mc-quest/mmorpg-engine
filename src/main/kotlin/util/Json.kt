package com.shadowforgedmmo.engine.util

import com.google.gson.Gson
import java.io.InputStreamReader
import kotlin.reflect.KClass

private val gson = Gson()

fun <T : Any> loadJsonResource(path: String, classOfT: KClass<T>): T {
    val resource = object {}::class.java.classLoader.getResource(path) ?: error("Resource not found: $path")
    return InputStreamReader(resource.openStream()).use { gson.fromJson(it, classOfT.java) }
}