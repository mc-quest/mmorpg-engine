package net.mcquest.engine.character

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.entity.EntityHuman
import net.mcquest.engine.model.*
import net.mcquest.engine.resource.splitId
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.minestom.MinestomModelEngine
import team.unnamed.hephaestus.minestom.ModelEntity

abstract class CharacterModel {
    abstract fun createEntity(): EntityCreature
}

class BlockbenchCharacterModel(
    private val model: BlockbenchModel,
    private val scale: Float = 1.0F
) : CharacterModel() {
    override fun createEntity(): ModelEntity =
        BlockbenchCharacterModelEntity(model.model, scale)
}

private class BlockbenchCharacterModelEntity(model: Model, scale: Float) : ModelEntity(
    EntityType.ARMOR_STAND,
    model,
    scale
) {
    init {
        val meta = getEntityMeta() as ArmorStandMeta
        meta.isMarker = true
        meta.isHasNoGravity = false
    }

    override fun tickAnimations() = animationPlayer().tick(position.yaw(), 0.0F)
}

class CharacterModelEquipment(
    val mainHand: WeaponModel? = null,
    val offHand: WeaponModel? = null,
    val feet: ArmorModel? = null,
    val legs: ArmorModel? = null,
    val chest: ArmorModel? = null,
    val head: ArmorModel? = null
)

class SkinCharacterModel(
    private val skin: Skin,
    private val equipment: CharacterModelEquipment = CharacterModelEquipment()
) : CharacterModel() {
    override fun createEntity(): EntityCreature {
        val entity = EntityHuman(skin)
        applyEquipment(entity, equipment)
        return entity
    }
}

class EntityCharacterModel(
    private val entityType: EntityType,
    private val equipment: CharacterModelEquipment = CharacterModelEquipment()
) : CharacterModel() {
    override fun createEntity(): EntityCreature {
        val entity = EntityCreature(entityType)
        applyEquipment(entity, equipment)
        return entity
    }
}

fun deserializeCharacterModel(
    data: JsonNode,
    blockbenchModelsById: Map<String, BlockbenchModel>,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>,
    skinsById: Map<String, Skin>
): CharacterModel {
    if (data.isTextual) {
        val fullId = data.asText()
        val (prefix, id) = splitId(fullId)
        return when (prefix) {
            "models" -> BlockbenchCharacterModel(blockbenchModelsById.getValue(id))
            "skins" -> SkinCharacterModel(skinsById.getValue(id))
            "minecraft" -> EntityCharacterModel(EntityType.fromNamespaceId(fullId))
            else -> throw IllegalArgumentException()
        }
    }

    return when (data["type"].asText()) {
        "blockbench" -> {
            val modelId = parseBlockbenchModelId(data["model"].asText())
            val model = blockbenchModelsById.getValue(modelId)
            val scale = data["scale"]?.floatValue() ?: 1.0F
            BlockbenchCharacterModel(model, scale)
        }

        "skin" -> {
            val skinId = parseSkinId(data["skin"].asText())
            val skin = skinsById[skinId] ?: error("Skin not found: $skinId")
            val equipment = data["equipment"]?.let {
                deserializeCharacterModelEquipment(it, blockbenchItemModelsById)
            } ?: CharacterModelEquipment()
            SkinCharacterModel(skin, equipment)
        }

        "entity" -> {
            val entityType = EntityType.fromNamespaceId(data["entity"].asText())
            val equipment = data["equipment"]?.let {
                deserializeCharacterModelEquipment(it, blockbenchItemModelsById)
            } ?: CharacterModelEquipment()
            EntityCharacterModel(entityType, equipment)
        }

        else -> throw IllegalArgumentException()
    }
}

private fun deserializeCharacterModelEquipment(
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
) = CharacterModelEquipment(
    data["main_hand"]?.let { deserializeWeaponModel(it, blockbenchItemModelsById) },
    data["off_hand"]?.let { deserializeWeaponModel(it, blockbenchItemModelsById) },
    data["feet"]?.let { deserializeArmorModel(it, blockbenchItemModelsById) },
    data["legs"]?.let { deserializeArmorModel(it, blockbenchItemModelsById) },
    data["chest"]?.let { deserializeArmorModel(it, blockbenchItemModelsById) },
    data["head"]?.let { deserializeArmorModel(it, blockbenchItemModelsById) }
)

private fun applyEquipment(entity: EntityCreature, equipment: CharacterModelEquipment) {
    equipment.mainHand?.let { entity.itemInMainHand = it.itemStack }
    equipment.offHand?.let { entity.itemInOffHand = it.itemStack }
    equipment.feet?.let { entity.boots = it.itemStack }
    equipment.legs?.let { entity.leggings = it.itemStack }
    equipment.chest?.let { entity.chestplate = it.itemStack }
    equipment.head?.let { entity.helmet = it.itemStack }
}
