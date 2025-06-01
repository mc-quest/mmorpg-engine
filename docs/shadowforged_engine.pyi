from __future__ import annotations
from dataclasses import dataclass
from enum import Enum
from typing import Callable, ClassVar, Protocol, TypeVar, Union

class Point(Protocol):
    """Represents a 3D point interface with x, y, and z coordinates."""

    @property
    def x(self) -> float: ...

    @property
    def y(self) -> float: ...

    @property
    def z(self) -> float: ...


@dataclass
class Vector:
    """Represents a 3D vector."""

    x: float
    y: float
    z: float

    ZERO: ClassVar[Vector]
    """Shorthand for Vector(0, 0, 0)."""

    ONE: ClassVar[Vector]
    """Shorthand for Vector(1, 1, 1)."""

    LEFT: ClassVar[Vector]
    """Shorthand for Vector(1, 0, 0)."""

    RIGHT: ClassVar[Vector]
    """Shorthand for Vector(-1, 0, 0)."""

    UP: ClassVar[Vector]
    """Shorthand for Vector(0, 1, 0)."""

    DOWN: ClassVar[Vector]
    """Shorthand for Vector(0, -1, 0)."""

    FORWARD: ClassVar[Vector]
    """Shorthand for Vector(0, 0, 1)."""

    BACK: ClassVar[Vector]
    """Shorthand for Vector(0, 0, -1)."""

    def __add__(self, v: Vector) -> Vector:
        """Adds two vectors.

        Args:
            v (Vector): The vector to add.

        Returns:
            Vector: The resulting vector.
        """
        ...

    def __sub__(self, v: Vector) -> Vector:
        """Subtracts a vector from this vector.

        Args:
            v (Vector): The vector to subtract.

        Returns:
            Vector: The resulting vector.
        """
        ...

    def __mul__(self, s: float) -> Vector:
        """Multiplies the vector by a scalar.

        Args:
            s (float): The scalar to multiply by.

        Returns:
            Vector: The scaled vector.
        """
        ...


@dataclass
class Position:
    """Represents a 3D position with yaw and pitch."""

    x: float
    y: float
    z: float
    yaw: float = 0
    pitch: float = 0

    @property
    def direction(self) -> Vector:
        """The direction vector based on yaw and pitch."""
        ...

    def __add__(self, v: Vector) -> Position:
        """Adds a vector to this position.

        Args:
            v (Vector): The vector to add.

        Returns:
            Position: The resulting position.
        """
        ...

    def __sub__(self, v: Vector) -> Position:
        """Subtracts a vector from this position.

        Args:
            v (Vector): The vector to subtract.

        Returns:
            Position: The resulting position.
        """
        ...


class DamageType(Enum):
    PHYSICAL = 1
    ARCANE = 2
    FIRE = 3
    FROST = 4
    NATURE = 5
    SHADOW = 6
    HOLY = 7


class Damage(dict[DamageType, float]):
    """Represents a blow of damage with damage types and amounts."""

    def __init__(self, damage: Union[float, dict[DamageType, float]]) -> None:
        """Initializes the damage object.

        Args:
            damage (Union[float, dict[DamageType, float]]): The damage types and amounts.

        Examples:
            >>> Damage(10)
            >>> Damage({DamageType.PHYSICAL: 5, DamageType.HOLY: 10})
        """
        ...


@dataclass
class Sound:
    type: str
    category: str = 'master'
    volume: float = 1.0
    pitch: float = 1.0


class Instance:
    def spawn_character(self, position: Position, character: str) -> NonPlayerCharacter:
        """Spawns a character at the given position.

        Args:
            position (Position): The position to spawn the character.
            character (str): ID of the character blueprint.

        Returns:
            NonPlayerCharacter: The spawned character.

        Examples:
            >>> orc_grunt = instance.spawn_character(Position(0, 0, 0), 'characters:orc_grunt')
        """
        ...

    def play_sound(self, position: Point, sound: Sound) -> None:
        """Plays a sound at the given position.

        Args:
            position (Position): The position to play the sound.
            sound (Sound): The sound to play.

        Examples:
            >>> instance.play_sound(position, Sound('sounds:portcullis_close'))
        """
        ...

    def get_characters_in_box(
        self,
        position: Point,
        half_extents: Vector,
        filter: Callable[[Character], bool] = lambda _: True
    ) -> list[Character]:
        """Returns a list of characters in the box defined by position and half extents.

        Args:
            position (Position): The center position of the box.
            half_extents (Vector): The half extents of the box.
            filter (Callable[[Character], bool], optional): A filter function to apply to each character. Defaults to a function that returns True for all characters.

        Returns:
            list[Character]: List of characters in the box.
        """
        ...

    def spawn_projectile(
        self,
        position: Position,
        model: str,
        hitbox_extents: Vector,
        max_distance: float
    ) -> Projectile:
        """Spawns a projectile at the given position.

        Args:
            position (Position): The position to spawn the projectile.
            model (str): The model of the projectile.
            hitbox_extents (Vector): The hitbox extents of the projectile.
            max_distance (float): The maximum distance the projectile can travel before automatic removal.

        Returns:
            Projectile: The spawned projectile.

        Examples:
            >>> fireball = instance.spawn_projectile(Position(0, 0, 0), 'minecraft:fireball', Vector.ONE, 10)
            >>> fireball.on_hit += handle_fireball_hit
            >>> fireball.velocity = Vector(0, 0, 5)
        """
        ...


class GameObject:
    @property
    def instance(self) -> Instance:
        """The instance the object belongs to."""
        ...

    @property
    def position(self) -> Position:
        """The position of the object."""
        ...

    @property
    def velocity(self) -> Vector:
        """The velocity of the object."""
        ...

    @velocity.setter
    def velocity(self, value: Vector) -> None:
        ...

    def remove(self) -> None:
        """Removes the object from the instance."""
        ...


class Character(GameObject):
    @property
    def on_take_damage(self) -> Signal[TakeDamageEvent]:
        """Signal emitted when the character takes damage."""
        ...

    def damage(self, damage: Damage, source: Character) -> None:
        """Deals damage to the character.

        Args:
            damage (Damage): The damage to deal.
            source (Character): The character dealing the damage.

        Examples:
            >>> player.damage(Damage({DamageType.PHYSICAL: 5, DamageType.FIRE: 10}), pyromancer)
        """
        ...


class PlayerCharacter(Character):
    def send_message(self, message: str) -> None:
        """Sends a message to the player.

        Args:
            message (str): The message to send.

        Examples:
            >>> player.send_message('Unlocked portcullis using <items:portcullis_key>')
        """
        ...


class NonPlayerCharacter(Character):
    """Base class for non-player characters which scripts should subclass."""

    def tick(self) -> None:
        """Overridable method that is called every game tick."""
        ...

    def on_spawn(self) -> None:
        """Overridable method that is called when the character is spawned."""
        ...

    def on_despawn(self) -> None:
        """Overridable method that is called when the character is despawned."""
        ...

    def on_death(self) -> None:
        """Overridable method that is called when the character dies."""
        ...

    def on_finalize_death(self) -> None:
        """Overridable method that is called when the character's death is finalized."""
        ...


class Projectile(GameObject):
    @property
    def on_hit(self) -> Signal[ProjectileHitEvent]:
        """Signal emitted when the projectile hits something."""
        ...


class SkillStatus(Enum):
    COMPLETE = 3


class SkillExecutor:
    @property
    def user(self) -> PlayerCharacter:
        """The player character executing the skill."""
        ...

    @property
    def lifetime(self) -> float:
        """The time since the skill began executing in seconds."""
        ...

    def tick(self) -> SkillStatus:
        """Overridable method that is called every game tick."""
        ...


class Event:
    ...


class TakeDamageEvent(Event):
    ...


class ProjectileHitEvent(Event):
    @property
    def projectile(self) -> Projectile:
        """The projectile that hit something."""
        ...

    @property
    def hit(self) -> GameObject:
        """The object that was hit by the projectile."""
        ...


E = TypeVar('E', bound=Event)


class Signal[E]:
    def __iadd__(self, receiver: Callable[[E], None]) -> None:
        """Adds a receiver to the signal.

        Args:
            receiver (Callable[[E], None]): The receiver function that will be
            called when the signal is emitted.

        Examples:
            >>> handle_goblin_death = lambda event: print('Goblin died!')
            >>> goblin.on_death += handle_goblin_death
        """
        ...

    def __isub__(self, receiver: Callable[[E], None]) -> None:
        """Removes a receiver from the signal.

        Args:
            receiver (callable): The receiver function.

        Examples:
            >>> goblin.on_death -= handle_goblin_death
        """
        ...


class Task:
    def cancel(self) -> None:
        """Cancels the task."""
        ...


def get_time() -> float:
    """Returns the current time in seconds."""
    ...


def run_delayed(delay: float, function: Callable[[], None]) -> Task:
    """Runs a function after a delay.

    Args:
        delay (float): The delay in seconds.
        function (callable): The function to run.
    """
    ...
