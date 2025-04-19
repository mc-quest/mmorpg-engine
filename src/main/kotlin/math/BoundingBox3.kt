package net.mcquest.engine.math

data class BoundingBox3(val min: Vector3, val max: Vector3) {
    init {
        require(coordinateWiseLeq(min, max))
    }

    companion object {
        fun from(center: Vector3, halfExtents: Vector3) =
            BoundingBox3(center - halfExtents, center + halfExtents)
    }

    val center = (min + max) / 2.0

    val halfExtents = (max - min) / 2.0

    fun contains(point: Vector3) = coordinateWiseLeq(min, point) && coordinateWiseLeq(point, max)
}
