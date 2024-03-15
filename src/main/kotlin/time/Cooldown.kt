package net.mcquest.engine.time

class Cooldown(private val durationMillis: Long) {
    private var lastSetMillis = 0L

    fun hasCooldown(timeMillis: Long) =
        timeMillis - lastSetMillis < durationMillis

    fun set(timeMillis: Long) {
        lastSetMillis = timeMillis
    }
}
