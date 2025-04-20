package net.mcquest.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.math.Polygon
import net.mcquest.engine.math.Vector2
import net.mcquest.engine.math.deserializePolygon
import net.mcquest.engine.math.deserializeVector2
import java.awt.Graphics

abstract class QuestObjectiveMarker {
    abstract fun draw(graphics: Graphics)
}

class PointQuestObjectiveMarker(private val point: Vector2) : QuestObjectiveMarker() {
    override fun draw(graphics: Graphics) {
        graphics.fillOval(point.x.toInt(), point.y.toInt(), 10, 10)
    }
}

class PolygonQuestObjectiveMarker(private val polygon: Polygon) : QuestObjectiveMarker() {
    override fun draw(graphics: Graphics) {
        val xs = polygon.points.map { it.x.toInt() }.toIntArray()
        val ys = polygon.points.map { it.y.toInt() }.toIntArray()
        graphics.fillPolygon(xs, ys, polygon.points.size)
    }
}

fun deserializeQuestObjectiveMarker(data: JsonNode) = when (data["type"].asText()) {
    "point" -> PointQuestObjectiveMarker(deserializeVector2(data["point"]))

    "polygon" -> PolygonQuestObjectiveMarker(deserializePolygon(data["points"]))

    else -> error("unknown marker type")
}
