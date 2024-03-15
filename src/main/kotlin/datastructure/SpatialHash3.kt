package net.mcquest.engine.datastructure

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector3
import kotlin.math.floor

class SpatialHash3<T>(private val cellSize: Double) {
    private val valuesByCell: SetMultimap<Cell3, T> = HashMultimap.create()

    val values: Collection<T>
        get() = valuesByCell.values()

    fun query(instance: Instance, min: Vector3, max: Vector3): Collection<T> =
        cells(instance, min, max, cellSize).flatMap(valuesByCell::get).toSet()

    fun query(instance: Instance, center: Vector3, radius: Double) = query(
        instance,
        center - Vector3(radius, radius, radius),
        center + Vector3(radius, radius, radius)
    )

    fun put(instance: Instance, position: Vector3, value: T) {
        valuesByCell.put(cell(instance, position, cellSize), value)
    }

    fun put(instance: Instance, min: Vector3, max: Vector3, value: T) =
        cells(instance, min, max, cellSize).forEach { cell ->
            valuesByCell.put(cell, value)
        }

    fun move(
        oldInstance: Instance,
        oldPosition: Vector3,
        newInstance: Instance,
        newPosition: Vector3,
        value: T
    ) {
        valuesByCell.remove(cell(oldInstance, oldPosition, cellSize), value)
        valuesByCell.put(cell(newInstance, newPosition, cellSize), value)
    }

    fun move(
        oldInstance: Instance,
        oldMin: Vector3,
        oldMax: Vector3,
        newInstance: Instance,
        newMin: Vector3,
        newMax: Vector3,
        value: T
    ) {
        cells(oldInstance, oldMin, oldMax, cellSize).forEach { cell ->
            valuesByCell.remove(cell, value)
        }
        cells(newInstance, newMin, newMax, cellSize).forEach { cell ->
            valuesByCell.put(cell, value)
        }
    }

    fun remove(instance: Instance, position: Vector3, value: T) {
        valuesByCell.remove(cell(instance, position, cellSize), value)
    }
}

private data class Cell3(
    val instance: Instance,
    val x: Int,
    val y: Int,
    val z: Int
)

private fun cell(
    instance: Instance,
    position: Vector3,
    cellSize: Double
) = Cell3(
    instance,
    floor(position.x / cellSize).toInt(),
    floor(position.y / cellSize).toInt(),
    floor(position.z / cellSize).toInt(),
)

private fun cells(
    instance: Instance,
    min: Vector3,
    max: Vector3,
    cellSize: Double
) = sequence {
    val minCell = cell(instance, min, cellSize)
    val maxCell = cell(instance, max, cellSize)

    for (x in minCell.x..maxCell.x) {
        for (y in minCell.y..maxCell.y) {
            for (z in minCell.z..maxCell.z) {
                yield(Cell3(instance, x, y, z))
            }
        }
    }
}
