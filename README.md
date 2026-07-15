# Ice and Fire
Ice and Fire is a minecraft mod created by Raptorfarian and Alexthe666 which adds various mythical creatures like dragons, hypogriffs, faries and many more to the game!

# Ice and Fire 1.20.1: Migrate Three Dragon Caves from Feature to Structure

This mod migrates the following three underground dragon caves from `Feature<NoneFeatureConfiguration>` to native `Structure`:

- `iceandfire:fire_dragon_cave`
- `iceandfire:ice_dragon_cave`
- `iceandfire:lightning_dragon_cave`

Target version: Minecraft 1.20.1 Forge, corresponding to the source package `Ice_and_Fire-1.20.1-2.1.13-beta-5`.

## Core Implementation

### 1. Native Structure and StructurePiece

- `DragonCaveStructure`: Handles candidate chunk evaluation, probability, height, biome, distance, and dragon cave spacing checks, and creates the structure start point.
- `DragonCavePiece`: Stores dragon type, center, radius, age, gender, and random seed, persisted via NBT. Places the cave chunk by chunk within each intersecting block.
- `DragonCaveType`: Centralizes block types, ore tags, stalactites, treasure piles, entities, and loot tables for fire, ice, and lightning dragons.

Large caves no longer rely on a single Feature call spanning multiple chunks. Instead, they are generated chunk-boundary by `StructurePiece#postProcess`, enabling them to be saved and located by the structure system.

### 2. Registration and Data Files

Newly added and registered:

- `iceandfire:dragon_cave` StructureType
- `iceandfire:dragon_cave` StructurePieceType
- Three `worldgen/structure/*.json` files
- Three `worldgen/structure_set/*.json` files
- Three `tags/worldgen/biome/has_structure/*.json` files

The existing configured features, placed features, and biome modifier injection for the three dragon caves have been removed.

### 3. Preserved Gameplay Logic

The following still generate as before:

- Cave wall blocks and ores corresponding to each dragon type
- Main chambers and side chambers
- Ceiling stalactites
- Gold/silver/copper treasure piles
- Female/male dragon cave chests with original loot tables
- Stage 3 dragons at age 75–124, sleeping, with growth disabled and lair position set

These configuration values remain in effect:

- `generateDragonDenChance`
- `oreToStoneRatioForDragonCaves`
- `dragonDenGoldAmount`
- `dangerousWorldGenDistanceLimit`
- `dangerousWorldGenSeparationLimit`
- this mod's original biome configuration for the three dragon caves

## Locate Command

```mcfunction
/locate structure iceandfire:fire_dragon_cave
/locate structure iceandfire:ice_dragon_cave
/locate structure iceandfire:lightning_dragon_cave
```

This only affects newly generated chunks. Existing dragon caves in already-generated chunks will not be automatically migrated.

## Differences from the Old Feature Behavior

1. `Structure.GenerationContext` does not provide access to the real-time world spawn point or `IafWorldData` instances, so spawn distance checks now evaluate against the world origin `(0, 0)`.
2. The original dangerous feature spacing was managed centrally by persistent world data, which is not accessible at the structure start stage. This implementation uses deterministic local priority to limit distance between the three underground dragon caves, but does not enforce the exact same mutual exclusion with dragon roosts, cyclops caves, and other dangerous features.
3. To ensure stable cross-chunk results, the cave interior uses positional randomness derived from "structure seed + block coordinates." The shape and content rules remain consistent, but exact cave layouts and ore positions will not be identical to the old Feature version under the same world seed.
4. All three StructureSets use `spacing: 1`, with `DragonCaveStructure` applying the original `1 / generateDragonDenChance` probability. This keeps configuration runtime-effective, but the structure locator will scan more candidate chunks than structures using fixed larger spacing.
5. Structure biome tags first cover the overworld, then the original dynamic BiomeConfig check is performed in `findGenerationPoint`. This preserves user configuration rather than hardcoding dynamic configs into static datapack tags.

## Usage
In theory, it can be used as a direct replacement for 'iceandfire-2.1.13-1.20.1-beta-5.jar'.

There is a possibility of conflict with mods that modify the dragon nest generation logic.

\\\\\\!!! Back up your save file before use !!!///

# Build
Run
```bash
./gradlew build
```
The output `.jar` file can be found in `build\libs\`
