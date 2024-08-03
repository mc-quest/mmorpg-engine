package net.mcquest.engine.math

data class BoundingBox2(val min: Vector2, val max: Vector2) {
    init {
        require(coordinateWiseLeq(min, max))
    }

    fun contains(point: Vector2) = coordinateWiseLeq(min, point) && coordinateWiseLeq(point, max)
}
