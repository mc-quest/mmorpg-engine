package net.mcquest.engine.util

import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.minestom.server.MinecraftServer
import net.minestom.server.collision.BoundingBox
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec

val globalEventHandler
    get() = MinecraftServer.getGlobalEventHandler()

val schedulerManager
    get() = MinecraftServer.getSchedulerManager()

fun Position.toMinestom() = Pos(x, y, z, yaw.toFloat(), pitch.toFloat())

fun Position.Companion.fromMinestom(pos: Pos) = Position(
    pos.x(),
    pos.y(),
    pos.z(),
    pos.yaw().toDouble(),
    pos.pitch().toDouble()
)

fun Vector3.toMinestom() = Vec(x, y, z)

fun Vector3.Companion.fromMinestom(point: Point) = Vector3(
    point.x(),
    point.y(),
    point.z()
)

fun Vector3.Companion.fromMinestom(boundingBox: BoundingBox) = Vector3(
    boundingBox.width(),
    boundingBox.height(),
    boundingBox.depth()
)

fun perSecondToPerTick(perSecond: Double) = perSecond / 20.0

fun perSecondToPerTick(perSecond: Vector3) = perSecond / 20.0

fun perTickToPerSecond(perTick: Double) = perTick * 20.0

fun perTickToPerSecond(perTick: Vector3) = perTick * 20.0
