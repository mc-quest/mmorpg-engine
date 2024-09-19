package net.mcquest.engine.item

import net.mcquest.engine.texture.Icon

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
