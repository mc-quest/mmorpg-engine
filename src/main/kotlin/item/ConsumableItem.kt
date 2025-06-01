package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.texture.Icon

class ConsumableItem(
    id: String,
    name: String,
    quality: ItemQuality,
    texture: Icon
) : Item(id, name, quality) {
    init {
        TODO("ensure texture is derived")
    }
}
