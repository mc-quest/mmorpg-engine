package net.mcquest.engine.math

import com.fasterxml.jackson.databind.JsonNode
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double = 0.0,
    val pitch: Double = 0.0
) {
    companion object {
        fun distance(a: Position, b: Position) =
            Vector3.distance(a.toVector3(), b.toVector3())

        fun sqrDistance(a: Position, b: Position) =
            Vector3.sqrDistance(a.toVector3(), b.toVector3())
    }

    val direction: Vector3
        get() {
            val cosPitch = cos(toRadians(pitch))
            return Vector3(
                -cosPitch * sin(toRadians(yaw)),
                -sin(toRadians(pitch)),
                cosPitch * cos(toRadians(yaw))
            )
        }

    fun toVector3() = Vector3(x, y, z)

    fun toVector2() = Vector2(x, z)

    operator fun plus(vector3: Vector3) = Position(
        x + vector3.x,
        y + vector3.y,
        z + vector3.z,
        yaw,
        pitch
    )

    operator fun minus(vector3: Vector3) = Position(
        x - vector3.x,
        y - vector3.y,
        z - vector3.z,
        yaw,
        pitch
    )
}

fun deserializePosition(data: JsonNode) =
    if (data.size() == 3) Position(
        data[0].doubleValue(),
        data[1].doubleValue(),
        data[2].doubleValue()
    )
    else if (data.size() == 5) Position(
        data[0].doubleValue(),
        data[1].doubleValue(),
        data[2].doubleValue(),
        data[3].doubleValue(),
        data[4].doubleValue()
    )
    else throw IllegalArgumentException()
