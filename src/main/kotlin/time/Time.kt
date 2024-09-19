package net.mcquest.engine.time

import java.time.Duration

fun secondsToMillis(seconds: Double) = (seconds * 1000.0).toLong()

fun millisToSeconds(millis: Long) = millis / 1000.0

fun secondsToTicks(seconds: Double) = (seconds * 20.0).toInt()

fun secondsToDuration(seconds: Double) =
    Duration.ofMillis(secondsToMillis(seconds))
