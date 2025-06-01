package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.databind.JsonNode
import net.minestom.server.item.ItemStack
import net.minestom.server.item.armor.TrimMaterial
import net.minestom.server.item.armor.TrimPattern

abstract class ArmorModel {
    abstract val itemStack: ItemStack
}

class BasicArmorModel(val material: ArmorMaterial, val trim: ArmorTrim) : ArmorModel() {
    override val itemStack = ItemStack.builder(TODO())
        .build()
}

enum class ArmorMaterial { LEATHER, IRON }

class ArmorTrim(val material: TrimMaterial, val pattern: TrimPattern)

class BlockbenchArmorModel(val model: BlockbenchItemModel) : ArmorModel() {
    override val itemStack
        get() = model.itemStack
}

fun deserializeArmorModel(
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
): ArmorModel =
    if (data.isTextual) {
        val modelId = parseBlockbenchItemModelId(data.asText())
        val model = blockbenchItemModelsById.getValue(modelId)
        BlockbenchArmorModel(model)
    } else {
        deserializeBasicArmorModel(data)
    }

private fun deserializeBasicArmorModel(data: JsonNode) = BasicArmorModel(
    deserializeArmorMaterial(data["material"]),
    deserializeArmorTrim(data["trim"])
)

private fun deserializeArmorMaterial(data: JsonNode) =
    ArmorMaterial.valueOf(data.asText().uppercase())

private fun deserializeArmorTrim(data: JsonNode) = ArmorTrim(
    TODO(),
    TODO()
)
