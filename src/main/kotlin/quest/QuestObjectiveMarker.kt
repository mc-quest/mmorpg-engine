package net.mcquest.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.instance.parseInstanceId
import net.mcquest.engine.math.*
import java.awt.Graphics

abstract class QuestObjectiveMarker(val instance: Instance) {
    abstract fun draw(graphics: Graphics)
}

class PointQuestObjectiveMarker(
    instance: Instance,
    private val point: Vector2
) : QuestObjectiveMarker(instance) {
    override fun draw(graphics: Graphics) {
        graphics.fillOval(point.x.toInt(), point.y.toInt(), 10, 10)
    }
}

class PolygonQuestObjectiveMarker(
    instance: Instance,
    private val polygon: Polygon
) : QuestObjectiveMarker(instance) {
    override fun draw(graphics: Graphics) {
        val xs = polygon.points.map { it.x.toInt() }.toIntArray()
        val ys = polygon.points.map { it.y.toInt() }.toIntArray()
        graphics.fillPolygon(xs, ys, polygon.points.size)
    }
}

fun deserializeQuestObjectiveMarker(
    data: JsonNode,
    instancesById: Map<String, Instance>
): QuestObjectiveMarker {
    val instance = instancesById.getValue(parseInstanceId(data["instance"].asText()))
    return when (data["type"].asText()) {
        "point" -> PointQuestObjectiveMarker(instance, deserializeVector2(data["point"]))

        "polygon" -> PolygonQuestObjectiveMarker(instance, deserializePolygon(data["points"]))

        else -> error("unknown marker type")
    }
}
