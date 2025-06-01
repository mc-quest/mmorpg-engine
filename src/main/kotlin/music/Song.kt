package com.shadowforgedmmo.engine.music

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.time.secondsToMillis
import org.gagravarr.ogg.audio.OggAudioStatistics
import org.gagravarr.vorbis.VorbisFile
import java.io.File

class Song(val id: String, val durationMillis: Long) {
    val sound = Sound.sound(
        songKey(id),
        Sound.Source.MUSIC,
        1.0F,
        1.0F
    )
}

class SongAsset(val id: String, val file: File) {
    val key = songKey(id)
}

fun deserializeSong(id: String, file: File) = Song(
    id,
    computeDurationMillis(file)
)

fun deserializeSongAsset(id: String, file: File) = SongAsset(id, file)

fun parseSongId(id: String) = parseId(id, "music")

private fun computeDurationMillis(file: File): Long {
    return VorbisFile(file).use { vorbisFile ->
        val stats = OggAudioStatistics(vorbisFile, vorbisFile)
        stats.calculate()
        secondsToMillis(stats.durationSeconds)
    }
}

private fun songKey(id: String) = Key.key("music", id)
