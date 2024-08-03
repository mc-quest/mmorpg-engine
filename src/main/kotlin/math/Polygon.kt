package net.mcquest.engine.math

import com.fasterxml.jackson.databind.JsonNode

data class Polygon(val points: List<Vector2>) {
    init {
        require(points.size >= 3)
    }

    val boundingBox = BoundingBox2(
        points.reduce(Vector2::minOf),
        points.reduce(Vector2::maxOf)
    )

    fun contains(point: Vector2): Boolean {
        if (!boundingBox.contains(point)) return false
        var inside = false
        for (i in points.indices) {
            val j = (i - 1).mod(points.size)
            val edge = points[j] - points[i]
            if (points[i].y > point.y != points[j].y > point.y &&
                point.x < edge.x * (point.y - points[i].y) / edge.y + points[i].x
            ) {
                inside = !inside
            }
        }
        return inside
    }

    fun offset(d: Double): Polygon {
        val edges = points.indices.map {
            points[(it + 1).mod(points.size)] - points[it]
        }

        val normals = edges.map { Vector2(it.y, -it.x).normalized }

        return Polygon(
            points.indices.map {
                val rightPoint = points[(it - 1).mod(points.size)]
                val point = points[it]
                val leftPoint = points[(it + 1).mod(points.size)]
                val rightNormal = normals[(it - 1).mod(points.size)]
                val leftNormal = normals[it]
                lineIntersection(
                    rightPoint + rightNormal * d,
                    point + rightNormal * d,
                    point + leftNormal * d,
                    leftPoint + leftNormal * d
                ) ?: (point + rightNormal * d)
            }
        )
    }
}

fun deserializePolygon(data: JsonNode) = Polygon(
    data.map(::deserializeVector2)
)
