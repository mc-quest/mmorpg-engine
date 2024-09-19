package net.mcquest.engine.model

import net.mcquest.engine.resource.parseId
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.reader.ModelReader
import java.io.File

class BlockbenchItemModel(val id: String, val customModelData: Int) {
    val itemStack = ItemStack.builder(Material.GLOW_ITEM_FRAME).meta {
        it.customModelData(customModelData)
    }.build()
}

fun parseBlockbenchItemModelId(id: String) = parseId(id, "item_models")

class BlockbenchItemModelAsset(val id: String, val model: Model)

fun deserializeBlockbenchItemModelAsset(
    id: String,
    file: File,
    modelReader: ModelReader
) = BlockbenchItemModelAsset(id, modelReader.read(file))
