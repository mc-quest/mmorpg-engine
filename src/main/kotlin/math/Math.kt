package com.shadowforgedmmo.engine.math

fun clamp(value: Double, min: Double, max: Double) =
    if (value < min) min
    else if (value > max) max
    else value

fun lerp(a: Double, b: Double, t: Double) = a * (1.0 - t) + b * t

/**
 * Returns the intersection of the line p1-p2 and the line p3-p4.
 */
fun lineIntersection(
    p1: Vector2, p2: Vector2, p3: Vector2, p4: Vector2
): Vector2? {
    val (x1, y1) = p1
    val (x2, y2) = p2
    val (x3, y3) = p3
    val (x4, y4) = p4

    val denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)

    return if (denom == 0.0) {
        null
    } else {
        Vector2(
            (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4),
            (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)
        ) / denom
    }
}

fun overlapBox(a: BoundingBox3, b: BoundingBox3) =
    coordinateWiseLeq(a.min, b.max) && coordinateWiseLeq(b.min, a.min)

fun coordinateWiseLeq(a: Vector2, b: Vector2) =
    a.x <= b.x && a.y <= b.y

fun coordinateWiseLeq(a: Vector3, b: Vector3) =
    a.x <= b.x && a.y <= b.y && a.z <= b.z

fun boxCast(
    origin: Vector3,
    direction: Vector3,
    boxMin: Vector3,
    boxMax: Vector3,
    maxDistance: Double
): Vector3? {
    var tMin = -Double.MAX_VALUE
    var tMax = Double.MAX_VALUE

    for (i in 0..2) {
        if (direction[i] != 0.0) {
            val t1 = (boxMin[i] - origin[i]) / direction[i]
            val t2 = (boxMax[i] - origin[i]) / direction[i]
            tMin = maxOf(tMin, minOf(t1, t2))
            tMax = minOf(tMax, maxOf(t1, t2))
        }
    }

    if (tMax < 0 || tMin > tMax || tMin > maxDistance) return null

    return origin + direction * tMin
}
