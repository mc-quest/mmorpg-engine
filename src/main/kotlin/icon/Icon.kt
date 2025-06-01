package com.shadowforgedmmo.engine.texture

import java.awt.image.BufferedImage

abstract class Icon(
    id: String,
    customModelData: Int
) {
    abstract fun image(): BufferedImage
}
