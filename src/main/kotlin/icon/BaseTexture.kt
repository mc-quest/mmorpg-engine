package com.shadowforgedmmo.engine.texture

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class BaseTexture(id: String) : Icon(id, 0) {
    override fun image(): BufferedImage {
        val path = Path("")
        return path.inputStream().use(ImageIO::read)
    }
}
