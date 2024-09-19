package net.mcquest.engine.sound

import net.kyori.adventure.key.Key
import java.io.File

class AudioClipAsset(val id: String, val file: File) {
    val key = audioClipKey(id)
}

fun deserializeAudioClipAsset(id: String, file: File) = AudioClipAsset(id, file)

private fun audioClipKey(id: String) = Key.key("audio_clips", id)
