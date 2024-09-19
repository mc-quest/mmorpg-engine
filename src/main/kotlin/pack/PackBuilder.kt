package net.mcquest.engine.pack

import net.kyori.adventure.key.Key
import net.mcquest.engine.model.BlockbenchModel
import net.mcquest.engine.model.BlockbenchItemModelAsset
import net.mcquest.engine.resource.ResourceLoader
import net.mcquest.engine.util.loadJsonResource
import net.minestom.server.item.Material
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.model.*
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.sound.Sound
import team.unnamed.creative.sound.SoundEntry
import team.unnamed.creative.sound.SoundEvent
import team.unnamed.creative.sound.SoundRegistry
import team.unnamed.hephaestus.writer.ModelWriter
import java.io.File

class PackBuilder(root: File) {
    private val resourceLoader = ResourceLoader(root)
    private val pack = ResourcePack.resourcePack()

    fun build() {
        writePackMeta()
        writeBasePack()
        writeEngineModels()
        writeEngineAudioClips()
        writeModels()
        writeItemModels()
        writeMusic()
        writeAudioClips()
        disableMinecraftMusic()
        savePack()
    }

    private fun writePackMeta() {
        val config = resourceLoader.loadConfig()
        pack.packMeta(22, "${config.name} resource pack")
    }

    private fun writeBasePack() {
        // TODO
    }

    private fun writeEngineModels() {
        // TODO
    }

    private fun writeEngineAudioClips() {
        // TODO: replace this
        val key = Key.key(Namespaces.ENGINE_AUDIO_CLIPS, "quest_start")
        val sound = Sound.sound(
            key,
            Writable.resource(this::class.java.classLoader, "audio_clips/quest_start.ogg")
        )
        val soundEvent = SoundEvent.soundEvent(
            key,
            false,
            null,
            listOf(SoundEntry.soundEntry(sound))
        )
        pack.sound(sound)
        pack.soundEvent(soundEvent)

        val key2 = Key.key(Namespaces.ENGINE_AUDIO_CLIPS, "quest_complete")
        val sound2 = Sound.sound(
            key2,
            Writable.resource(this::class.java.classLoader, "audio_clips/quest_complete.ogg")
        )
        val soundEvent2 = SoundEvent.soundEvent(
            key2,
            false,
            null,
            listOf(SoundEntry.soundEntry(sound2))
        )
        pack.sound(sound2)
        pack.soundEvent(soundEvent2)
    }

    private fun writeModels() {
        val models = resourceLoader.loadModels().values.map(BlockbenchModel::model)
        ModelWriter.resource(Namespaces.MODELS).write(pack, models)
    }

    private fun writeItemModels() {
        val itemModelAssets = resourceLoader.loadBlockbenchItemModelAssets().values
        val overrides = mutableListOf<ItemOverride>()
        itemModelAssets.forEach { writeBlockbenchItemModel(it, overrides) }
    }

    private fun writeBlockbenchItemModel(
        blockbenchItemModelAsset: BlockbenchItemModelAsset,
        overrides: MutableList<ItemOverride>
    ) {
//        pack.model(
//            Model.model()
//                .key(key)
//                .display(display)
//                .textures(textures)
//                .elements(elements)
//                .build()
//        )
//        model.textures.forEach(pack::texture)
//        overrides += ItemOverride.of(
//            key,
//            ItemPredicate.customModelData(itemModelAsset.customModelData)
//        )
    }

    private fun writeItemOverrides(
        material: Material,
        overrides: List<ItemOverride>
    ) {
        pack.model(
            Model.model()
                .key(material.key())
                .parent(Key.key(Namespaces.MINECRAFT, "item/handheld"))
                .textures(
                    ModelTextures.builder()
                        .layers(ModelTexture.ofKey(material.key()))
                        .build()
                )
                .overrides(overrides)
                .build()
        )
    }

    private fun writeMusic() {
        val songAssets = resourceLoader.loadMusicAssets().values
        songAssets.forEach { songAsset ->
            val sound = Sound.sound(songAsset.key, Writable.file(songAsset.file))
            val soundEvent = SoundEvent.soundEvent(
                songAsset.key,
                false,
                null,
                listOf(SoundEntry.soundEntry(sound))
            )
            pack.sound(sound)
            pack.soundEvent(soundEvent)
        }
    }

    private fun writeAudioClips() {
        val audioClipAssets = resourceLoader.loadAudioClipAssets().values
        audioClipAssets.forEach { audioClipAsset ->
            val sound = Sound.sound(
                audioClipAsset.key,
                Writable.file(audioClipAsset.file)
            )
            val soundEvent = SoundEvent.soundEvent(
                audioClipAsset.key,
                false,
                null,
                listOf(SoundEntry.soundEntry(sound))
            )
            pack.sound(sound)
            pack.soundEvent(soundEvent)
        }
    }

    private fun disableMinecraftMusic() {
        val minecraftMusic = loadJsonResource(
            "data/minecraft_music.json",
            Array<String>::class.java
        )
        val soundEvents = minecraftMusic.map { song ->
            SoundEvent.soundEvent(
                Key.key(Namespaces.MINECRAFT, song),
                true,
                null,
                emptyList()
            )
        }
        pack.soundRegistry(
            SoundRegistry.soundRegistry(
                Namespaces.MINECRAFT,
                soundEvents
            )
        )
    }

    private fun savePack() {
        MinecraftResourcePackWriter.minecraft().writeToZipFile(
            File("pack.zip"),
            pack
        )
    }
}
