package com.shadowforgedmmo.engine.sound

import net.kyori.adventure.key.Key
import com.shadowforgedmmo.engine.pack.Namespaces
import java.io.File

class SoundAsset(val id: String, val file: File) {
    val key = soundKey(id)
}

fun deserializeSoundAsset(id: String, file: File) = SoundAsset(id, file)

private fun soundKey(id: String) = Key.key(Namespaces.SOUNDS, id)
