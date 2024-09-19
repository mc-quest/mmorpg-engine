package net.mcquest.engine.map

import net.mcquest.engine.resource.parseId
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class MapTexture(val id: String, val texture: BufferedImage)

fun deserializeMapTexture(id: String, file: File) = MapTexture(id, ImageIO.read(file))

fun parseMapTextureId(id: String) = parseId(id, "map_textures")
