package net.mcquest.engine.character

class NonPlayerCharacterManager(blueprints: Collection<CharacterBlueprint>) {
    private val blueprintsById = blueprints.associateBy(CharacterBlueprint::id)

    fun getBlueprint(id: String): CharacterBlueprint {
        return blueprintsById.getValue(id)
    }
}