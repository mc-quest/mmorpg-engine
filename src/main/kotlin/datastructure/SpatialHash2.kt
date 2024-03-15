package net.mcquest.engine.datastructure

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.BoundingBox2
import net.mcquest.engine.math.Vector2
import kotlin.math.floor

class SpatialHash2<T>(private val cellSize: Double) {
    private val valuesByCell: SetMultimap<Cell2, T> = HashMultimap.create()

    val values: Collection<T>
        get() = valuesByCell.values()

    fun query(instance: Instance, position: Vector2): Collection<T> =
        valuesByCell.get(cell(instance, position, cellSize))

    fun query(instance: Instance, min: Vector2, max: Vector2): Collection<T> =
        cells(instance, min, max, cellSize).flatMap(valuesByCell::get).toSet()

    fun query(instance: Instance, center: Vector2, radius: Double) = query(
        instance,
        center - Vector2(radius, radius),
        center + Vector2(radius, radius)
    )

    fun put(instance: Instance, position: Vector2, value: T) {
        valuesByCell.put(cell(instance, position, cellSize), value)
    }

    fun put(instance: Instance, min: Vector2, max: Vector2, value: T) =
        cells(instance, min, max, cellSize).forEach { cell ->
            valuesByCell.put(cell, value)
        }

    fun put(instance: Instance, boundingBox: BoundingBox2, value: T) =
        put(instance, boundingBox.min, boundingBox.max, value)

    fun move(
        oldInstance: Instance,
        oldPosition: Vector2,
        newInstance: Instance,
        newPosition: Vector2,
        value: T
    ) {
        valuesByCell.remove(cell(oldInstance, oldPosition, cellSize), value)
        valuesByCell.put(cell(newInstance, newPosition, cellSize), value)
    }

    fun move(
        oldInstance: Instance,
        oldMin: Vector2,
        oldMax: Vector2,
        newInstance: Instance,
        newMin: Vector2,
        newMax: Vector2,
        value: T
    ) {
        cells(oldInstance, oldMin, oldMax, cellSize).forEach { cell ->
            valuesByCell.remove(cell, value)
        }
        cells(newInstance, newMin, newMax, cellSize).forEach { cell ->
            valuesByCell.put(cell, value)
        }
    }

    fun remove(instance: Instance, position: Vector2, value: T) {
        valuesByCell.remove(cell(instance, position, cellSize), value)
    }
}

private data class Cell2(
    val instance: Instance,
    val x: Int,
    val y: Int
)

private fun cell(
    instance: Instance,
    position: Vector2,
    cellSize: Double
) = Cell2(
    instance,
    floor(position.x / cellSize).toInt(),
    floor(position.y / cellSize).toInt()
)

private fun cells(
    instance: Instance,
    min: Vector2,
    max: Vector2,
    cellSize: Double
) = sequence {
    val minCell = cell(instance, min, cellSize)
    val maxCell = cell(instance, max, cellSize)

    for (x in minCell.x..maxCell.x) {
        for (y in minCell.y..maxCell.y) {
            yield(Cell2(instance, x, y))
        }
    }
}
