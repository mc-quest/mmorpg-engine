package net.mcquest.engine.math

import com.fasterxml.jackson.databind.JsonNode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(val x: Double, val y: Double, val z: Double) {
    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
        val ONE = Vector3(1.0, 1.0, 1.0)
        val LEFT = Vector3(1.0, 0.0, 0.0)
        val RIGHT = Vector3(-1.0, 0.0, 0.0)
        val UP = Vector3(0.0, 1.0, 0.0)
        val DOWN = Vector3(0.0, -1.0, 0.0)
        val FORWARD = Vector3(0.0, 0.0, 1.0)
        val BACK = Vector3(0.0, 0.0, -1.0)

        fun dot(a: Vector3, b: Vector3) = a.x * b.x + a.y * b.y + a.z * b.z

        fun cross(a: Vector3, b: Vector3) = Vector3(
            a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x
        )

        fun distance(a: Vector3, b: Vector3) = (b - a).magnitude

        fun sqrDistance(a: Vector3, b: Vector3) = (b - a).sqrMagnitude

        fun lerp(a: Vector3, b: Vector3, t: Double) = a * (1.0 - t) + b * t
    }

    operator fun get(index: Int) = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException()
    }

    val magnitude
        get() = sqrt(x * x + y * y + z * z)

    val sqrMagnitude
        get() = x * x + y * y + z * z

    val normalized
        get() = this / magnitude

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    operator fun plus(other: Vector3) = Vector3(
        x + other.x, y + other.y, z + other.z
    )

    operator fun minus(other: Vector3) = Vector3(
        x - other.x, y - other.y, z - other.z
    )

    operator fun times(scalar: Double) = Vector3(
        x * scalar, y * scalar, z * scalar
    )

    operator fun div(scalar: Double) = this * (1.0 / scalar)

    fun withX(x: Double) = Vector3(x, y, z)

    fun withY(y: Double) = Vector3(x, y, z)

    fun withZ(z: Double) = Vector3(x, y, z)

    fun rotateAroundY(radians: Double): Vector3 {
        val cos = cos(radians)
        val sin = sin(radians)
        return Vector3(x * cos + z * sin, y, -x * sin + z * cos)
    }
}

fun deserializeVector3(data: JsonNode) = Vector3(
    data[0].asDouble(),
    data[1].asDouble(),
    data[2].asDouble()
)
