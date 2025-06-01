package com.shadowforgedmmo.engine.model

class ModelManager(
    blockbenchModels: Collection<BlockbenchModel>,
    blockbenchItemModels: Collection<BlockbenchItemModel>
) {
    private val blockbenchModelsById = blockbenchModels.associateBy(BlockbenchModel::id)
    private val blockbenchItemModelsById = blockbenchItemModels.associateBy(BlockbenchItemModel::id)

    fun getBlockbenchModel(id: String) = blockbenchModelsById.getValue(id)

    fun getBlockbenchItemModel(id: String) = blockbenchItemModelsById.getValue(id)

    fun start() {
        blockbenchModelsById.values.forEach { it.model.discardResourcePackData() }
    }
}
