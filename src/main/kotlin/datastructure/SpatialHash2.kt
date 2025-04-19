package net.mcquest.engine.datastructure

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mcquest.engine.math.Vector2
import net.mcquest.engine.math.Vector2Int
import kotlin.math.floor

class SpatialHash2<T>(private val cellSize: Double) {
    private val grid: SetMultimap<Vector2Int, T> = HashMultimap.create()

    val values: Collection<T>
        get() = grid.values().toSet()

    fun query(position: Vector2): Collection<T> = grid[cell(position)].toSet()

    fun query(min: Vector2, max: Vector2): Collection<T> =
        cells(min, max).flatMap(grid::get).toSet()

    fun query(center: Vector2, radius: Double) = query(
        center - Vector2.ONE * radius,
        center + Vector2.ONE * radius
    )

    fun put(position: Vector2, value: T) = grid.put(cell(position), value)

    fun put(min: Vector2, max: Vector2, value: T) =
        cells(min, max).forEach { grid.put(it, value) }

    fun remove(position: Vector2, value: T) = grid.remove(cell(position), value)

    fun remove(min: Vector2, max: Vector2, value: T) =
        cells(min, max).forEach { grid.remove(it, value) }

    private fun cellCoord(coord: Double) = floor(coord / cellSize).toInt()

    private fun cell(position: Vector2) = Vector2Int(
        cellCoord(position.x),
        cellCoord(position.y)
    )

    private fun cells(min: Vector2, max: Vector2) = sequence {
        val minCell = cell(min)
        val maxCell = cell(max)

        for (x in minCell.x..maxCell.x) {
            for (y in minCell.y..maxCell.y) {
                yield(Vector2Int(x, y))
            }
        }
    }
}
