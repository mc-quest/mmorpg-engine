package net.mcquest.engine.combat

import net.mcquest.engine.character.Character
import net.mcquest.engine.character.CharacterHitbox
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.collision.CollisionManager
import net.mcquest.engine.gameobject.GameObjectManager
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector3

fun getNearbyCharacters(
    gameObjectManager: GameObjectManager,
    instance: Instance,
    center: Vector3,
    radius: Double
): Collection<Character> = gameObjectManager
    .getNearbyObjects(instance, center, radius)
    .filterIsInstance<Character>()

fun getNearbyPlayerCharacters(
    gameObjectManager: GameObjectManager,
    instance: Instance,
    center: Vector3,
    radius: Double
): Collection<PlayerCharacter> = gameObjectManager
    .getNearbyObjects(instance, center, radius)
    .filterIsInstance<PlayerCharacter>()

fun getCharactersInBox(
    collisionManager: CollisionManager,
    instance: Instance,
    center: Vector3,
    halfExtents: Vector3
): Collection<Character> = collisionManager
    .overlapBoxByCenter(instance, center, halfExtents)
    .filterIsInstance<CharacterHitbox>()
    .map(CharacterHitbox::character)
