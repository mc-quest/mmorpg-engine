package net.mcquest.engine.music

class MusicManager(music: Collection<Song>) {
    private val musicById = music.associateBy(Song::id)

    fun getSong(id: String) = musicById.getValue(id)
}
