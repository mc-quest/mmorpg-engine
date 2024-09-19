package net.mcquest.engine.model

import com.fasterxml.jackson.databind.JsonNode

class WeaponModel(val model: BlockbenchItemModel) {
    val itemStack
        get() = model.itemStack
}

fun deserializeWeaponModel(
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
): WeaponModel {
    val modelId = parseBlockbenchItemModelId(data.asText())
    val model = blockbenchItemModelsById.getValue(modelId)
    return WeaponModel(model)
}
