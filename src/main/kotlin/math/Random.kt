package net.mcquest.engine.math

import kotlin.random.Random

fun weightedRandomIndex(weights: List<Double>): Int {
    var rand = Random.nextDouble(weights.sum())
    for (i in weights.indices) {
        rand -= weights[i]
        if (rand < 0) return i
    }
    return weights.size - 1
}
