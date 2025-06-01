package com.shadowforgedmmo.engine.math

import com.fasterxml.jackson.databind.JsonNode
import kotlin.math.sqrt

data class Vector2(val x: Double, val y: Double) {
    companion object {
        val ZERO = Vector2(0.0, 0.0)
        val ONE = Vector2(1.0, 1.0)
        val LEFT = Vector2(1.0, 0.0)
        val RIGHT = Vector2(-1.0, 0.0)
        val UP = Vector2(0.0, 1.0)
        val DOWN = Vector2(0.0, -1.0)

        fun dot(a: Vector2, b: Vector2) = a.x * b.x + a.y * b.y

        fun minOf(a: Vector2, b: Vector2) = Vector2(
            minOf(a.x, b.x),
            minOf(a.y, b.y)
        )

        fun maxOf(a: Vector2, b: Vector2) = Vector2(
            maxOf(a.x, b.x),
            maxOf(a.y, b.y)
        )
    }

    val magnitude
        get() = sqrt(x * x + y * y)

    val sqrMagnitude
        get() = x * x + y * y

    val normalized
        get() = this * (1.0 / magnitude)

    operator fun get(index: Int) = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException()
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Vector2(-x, -y)

    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun times(scalar: Double) = Vector2(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Vector2(x / scalar, y / scalar)
}

fun deserializeVector2(data: JsonNode) = Vector2(
    data[0].asDouble(), data[1].asDouble()
)
