package com.shadowforgedmmo.engine.music

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.util.schedulerManager
import net.minestom.server.timer.Task
import java.time.Duration

class MusicPlayer(private val pc: PlayerCharacter) {
    private var song: Song? = null
    private var replayTask: Task? = null

    fun setSong(song: Song?) {
        if (song == this.song) return

        replayTask?.cancel()
        this.song?.let { pc.entity.stopSound(it.sound) }

        song?.let {
            replayTask = schedulerManager.buildTask {
                pc.entity.playSound(it.sound)
            }.repeat(Duration.ofMillis(it.durationMillis)).schedule()
        }
        this.song = song
    }
}
