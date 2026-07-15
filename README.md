# Ice and Fire
Ice and Fire is a minecraft mod created by Raptorfarian and Alexthe666 which adds various mythical creatures like dragons, hypogriffs, faries and many more to the game!

This modification changes the following three underground dragon caves from `Feature<NoneFeatureConfiguration>` to native `Structure`:
- `iceandfire:fire_dragon_cave`
- `iceandfire:ice_dragon_cave`
- `iceandfire:lightning_dragon_cave`
Target version: Minecraft 1.20.1 / Forge, corresponding to the source code package `Ice_and_Fire-1.20.1-2.1.13-beta-5`.
# Main Implementation
### 1. Native Structure and StructurePiece
- `DragonCaveStructure`: Responsible for candidate chunk selection, probability, height, biome, distance, and cave spacing determination, and creating the structure starting point.
- `DragonCavePiece`: Stores dragon type, center, radius, age, gender, and random seed, persisted via NBT; places the cave in chunks within each intersecting area.
- `DragonCaveType`: Centralizes maintenance of blocks, ore tags, stalactites, treasure piles, entities, and loot tables specific to fire dragons, ice dragons, and lightning dragons.
Large caves no longer rely on a single `Feature` call spanning multiple chunks. Instead, they are generated per-chunk boundary by `StructurePiece#postProcess`, allowing them to be saved and positioned by the structure system.
### 2. Registration and Data Files
Added and registered:
- `iceandfire:dragon_cave` StructureType
- `iceandfire:dragon_cave` StructurePieceType
- Three `worldgen/structure/*.json` files
- Three `worldgen/structure_set/*.json` files
- Three `tags/worldgen/biome/has_structure/*.json` files
Simultaneously removed the original configured features, placed features, and biome modifier injections for the three dragon caves.
### 3. Retained Gameplay Logic
The following will still be generated:
- Cave wall blocks and ores corresponding to the dragon type
- Main cave chambers and side chambers
- Stalactites on the cave ceiling
- Gold/silver/copper treasure piles
- Female/male dragon cave chests and original loot tables
- Tertiary dragons aged 75–124 days, sleeping, with growth disabled, and with nest positions
These configuration items will continue to be read:
- `generateDragonDenChance`
- `oreToStoneRatioForDragonCaves`
- `dragonDenGoldAmount`
- `dangerousWorldGenDistanceLimit`
- `dangerousWorldGenSeparationLimit`
- Citadel and this mod's existing biome configurations for the three dragon caves
# Location Commands
```mcfunction
/locate structure iceandfire:fire_dragon_cave
/locate structure iceandfire:ice_dragon_cave
/locate structure iceandfire:lightning_dragon_cave
```
This will only affect newly generated chunks; old dragon caves in already generated chunks will not automatically migrate.
# Differences from Old Feature Behavior
1. `Structure.GenerationContext` does not have access to real-time world spawn points or `IafWorldData` instances, so spawn point distance is now judged relative to the world origin `(0, 0)`.
2. Original dangerous terrain spacing is managed uniformly by persistent world data, which is inaccessible during the structure start phase. This implementation uses deterministic local priority to limit distance between the three underground dragon caves but does not enforce complete mutual exclusion with other dangerous Features like dragon dens or cyclops caves.
3. To ensure stable cross-chunk results, cave interiors use position randomness based on \"structure seed + block coordinates\". The shape and content rules remain consistent, but the exact cave shape and ore positions under the same world seed will not be identical to the old Feature version.
4. The three StructureSets use `spacing: 1`, with `DragonCaveStructure` applying the original `1 / generateDragonDenChance` probability. This configuration remains effective at runtime, but structure location scans will check more candidate chunks than structures with fixed large spacing.
5. Structure biome tags first override the Overworld, then execute the original dynamic `BiomeConfig` checks in `findGenerationPoint`. This preserves user configurations instead of hardcoding dynamic settings into static datapack tags.
