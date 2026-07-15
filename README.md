# Ice and Fire
Ice and Fire is a minecraft mod created by Raptorfarian and Alexthe666 which adds various mythical creatures like dragons, hypogriffs, faries and many more to the game!

# Ice and Fire 1.20.1: Migrate Three Dragon Caves from Feature to Structure

This modification converts the following three underground dragon caves from `Feature<NoneFeatureConfiguration>` to native `Structure`:

- `iceandfire:fire_dragon_cave`
- `iceandfire:ice_dragon_cave`
- `iceandfire:lightning_dragon_cave`

Target version: Minecraft 1.20.1 / Forge, corresponding to source package `Ice_and_Fire-1.20.1-2.1.13-beta-5`.

## Key Implementation

### 1. Native Structure and StructurePiece

- `DragonCaveStructure`: Handles candidate chunk evaluation, probability, height, biome, distance, and dragon cave spacing logic, and creates the structure start.
- `DragonCavePiece`: Persists dragon type, center, radius, age, gender, and random seed via NBT; places the cave chunk by chunk within each intersecting section.
- `DragonCaveType`: Centralizes block types, ore tags, stalactites, treasure piles, entities, and loot tables for fire, ice, and lightning dragons respectively.

Large caves are no longer generated across multiple chunks via a single Feature call. Instead, `StructurePiece#postProcess` generates them per chunk boundary, allowing the structure system to save and locate them.

### 2. Registration and Data Files

Newly added and registered:

- `iceandfire:dragon_cave` StructureType
- `iceandfire:dragon_cave` StructurePieceType
- Three `worldgen/structure/*.json` files
- Three `worldgen/structure_set/*.json` files
- Three `tags/worldgen/biome/has_structure/*.json` files

The original configured features, placed features, and biome modifier injections for the three dragon caves have been removed.

### 3. Preserved Gameplay Logic

Still generates:

- Cave wall blocks and ores corresponding to the dragon type
- Main chamber and side chambers
- Ceiling stalactites
- Gold/silver/copper treasure piles
- Female/male dragon cave chests with original loot tables
- Tier 3 dragons aged 75–124 days, sleeping, with growth disabled and lair position set

The following configuration options continue to be respected:

- `generateDragonDenChance`
- `oreToStoneRatioForDragonCaves`
- `dragonDenGoldAmount`
- `dangerousWorldGenDistanceLimit`
- `dangerousWorldGenSeparationLimit`
- Citadel/this mod's existing biome configurations for the three dragon cave types

## Locate Commands

```mcfunction
/locate structure iceandfire:fire_dragon_cave
/locate structure iceandfire:ice_dragon_cave
/locate structure iceandfire:lightning_dragon_cave
```

This only affects newly generated chunks. Old dragon caves in already-generated chunks will not be automatically migrated.

## Differences from Legacy Feature Behavior

1. `Structure.GenerationContext` does not provide access to the real-time world spawn point or `IafWorldData` instances, so spawn distance checks are now evaluated relative to the world origin `(0, 0)`.

2. The original dangerous feature spacing was managed globally by persistent world data, which is inaccessible during structure start generation. This implementation uses deterministic local priority to limit distances between the three underground dragon caves, but does not enforce identical mutual exclusion with other dangerous features such as dragon roosts or cyclops caves.

3. To ensure stable cross-chunk results, the cave uses positional randomness based on "structure seed + block coordinates." While shape and content rules remain consistent, the exact cave layout and ore positions will not be identical to the legacy Feature under the same world seed.

4. All three StructureSets use `spacing: 1`, with `DragonCaveStructure` applying the original `1 / generateDragonDenChance` probability. This allows runtime configuration to remain effective, but structure localization scanning will evaluate more candidate chunks than structures using a fixed larger spacing.

5. Structure biome tags are first applied to the overworld, then the existing dynamic BiomeConfig check is executed in `findGenerationPoint`. This preserves user configuration rather than hardcoding dynamic settings into static datapack tags.
