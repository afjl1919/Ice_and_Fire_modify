package com.github.alexthe666.iceandfire.world.structure.dragon;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.config.BiomeConfig;
import com.github.alexthe666.iceandfire.datagen.IafBiomeTagGenerator;
import com.github.alexthe666.iceandfire.world.IafStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class DragonCaveStructure extends Structure {
    public static final Codec<DragonCaveStructure> CODEC = RecordCodecBuilder.<DragonCaveStructure>mapCodec(instance ->
            instance.group(
                    DragonCaveStructure.settingsCodec(instance),
                    DragonCaveType.CODEC.fieldOf("dragon_type").forGetter(structure -> structure.dragonType)
            ).apply(instance, DragonCaveStructure::new)).codec();

    private final DragonCaveType dragonType;

    public DragonCaveStructure(StructureSettings settings, DragonCaveType dragonType) {
        super(settings);
        this.dragonType = dragonType;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int chance = Math.max(1, IafConfig.generateDragonDenChance);
        long startSeed = mixSeed(context.seed(), context.chunkPos().x, context.chunkPos().z, this.dragonType.getPlacementSalt());
        RandomSource random = RandomSource.create(startSeed);
        if (random.nextInt(chance) != 0) {
            return Optional.empty();
        }

        boolean male = random.nextBoolean();
        int caveY = findCaveY(context, context.chunkPos().x, context.chunkPos().z, random);
        if (caveY < context.heightAccessor().getMinBuildHeight() + 20) {
            return Optional.empty();
        }

        BlockPos center = context.chunkPos().getMiddleBlockPosition(caveY);
        if (!isFarEnoughFromWorldOrigin(center)) {
            return Optional.empty();
        }

        if (!isBiomeValid(context, center, this.dragonType)) {
            return Optional.empty();
        }

        if (!passesCaveSeparation(context, center, chance, startSeed)) {
            return Optional.empty();
        }

        int dragonAge = 75 + random.nextInt(50);
        int radius = (int) (dragonAge * 0.2F) + random.nextInt(4);
        long caveSeed = random.nextLong();

        return Optional.of(new GenerationStub(center, builder ->
                builder.addPiece(new DragonCavePiece(this.dragonType, center, radius, dragonAge, male, caveSeed))));
    }

    private boolean passesCaveSeparation(GenerationContext context, BlockPos center, int chance, long currentStartSeed) {
        int separation = (int) Math.ceil(Math.max(0D, IafConfig.dangerousWorldGenSeparationLimit));
        if (separation == 0) {
            return true;
        }

        int chunkRadius = (separation + 15) / 16 + 1;
        long currentPriority = priority(currentStartSeed);
        int currentChunkX = context.chunkPos().x;
        int currentChunkZ = context.chunkPos().z;
        long separationSquared = (long) separation * separation;

        for (int chunkX = currentChunkX - chunkRadius; chunkX <= currentChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = currentChunkZ - chunkRadius; chunkZ <= currentChunkZ + chunkRadius; chunkZ++) {
                int neighborX = (chunkX << 4) + 8;
                int neighborZ = (chunkZ << 4) + 8;
                long dx = neighborX - center.getX();
                long dz = neighborZ - center.getZ();
                if (dx * dx + dz * dz > separationSquared) {
                    continue;
                }

                for (DragonCaveType type : DragonCaveType.values()) {
                    if (chunkX == currentChunkX && chunkZ == currentChunkZ && type == this.dragonType) {
                        continue;
                    }
                    long neighborStartSeed = mixSeed(context.seed(), chunkX, chunkZ, type.getPlacementSalt());
                    RandomSource neighborRandom = RandomSource.create(neighborStartSeed);
                    if (neighborRandom.nextInt(chance) != 0) {
                        continue;
                    }

                    long neighborPriority = priority(neighborStartSeed);
                    int comparison = Long.compareUnsigned(neighborPriority, currentPriority);
                    if (comparison > 0 || comparison == 0 && !comesBefore(chunkX, chunkZ, type, currentChunkX, currentChunkZ, this.dragonType)) {
                        continue;
                    }

                    // Structure generation has no access to IafWorldData. Probe the horizontal candidate
                    // at the current cave Y so invalid dragon biomes do not suppress a valid local winner.
                    BlockPos neighborProbe = new BlockPos(neighborX, center.getY(), neighborZ);
                    if (isFarEnoughFromWorldOrigin(neighborProbe) && isBiomeValid(context, neighborProbe, type)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static int findCaveY(GenerationContext context, int chunkX, int chunkZ, RandomSource random) {
        int sampleX = chunkX << 4;
        int sampleZ = chunkZ << 4;
        int caveY = 40;
        for (int xOffset = 0; xOffset < 20; xOffset++) {
            for (int zOffset = 0; zOffset < 20; zOffset++) {
                caveY = Math.min(caveY, context.chunkGenerator().getBaseHeight(
                        sampleX + xOffset,
                        sampleZ + zOffset,
                        Heightmap.Types.OCEAN_FLOOR_WG,
                        context.heightAccessor(),
                        context.randomState()));
            }
        }
        return caveY - 20 - random.nextInt(30);
    }

    private static boolean isFarEnoughFromWorldOrigin(BlockPos center) {
        double spawnLimit = Math.max(0D, IafConfig.dangerousWorldGenDistanceLimit);
        return (double) center.getX() * center.getX() + (double) center.getZ() * center.getZ() >= spawnLimit * spawnLimit;
    }

    private static boolean comesBefore(int chunkX, int chunkZ, DragonCaveType type, int otherX, int otherZ, DragonCaveType otherType) {
        if (chunkX != otherX) {
            return chunkX < otherX;
        }
        if (chunkZ != otherZ) {
            return chunkZ < otherZ;
        }
        return type.ordinal() < otherType.ordinal();
    }

    private static long priority(long startSeed) {
        long mixed = startSeed ^ 0x9E3779B97F4A7C15L;
        mixed ^= mixed >>> 30;
        mixed *= 0xbf58476d1ce4e5b9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94d049bb133111ebL;
        return mixed ^ mixed >>> 31;
    }

    private static boolean isBiomeValid(GenerationContext context, BlockPos center, DragonCaveType type) {
        Set<Holder<Biome>> biomes = context.chunkGenerator().getBiomeSource().getBiomesWithin(
                center.getX(), center.getY(), center.getZ(), 0, context.randomState().sampler());
        for (Holder<Biome> biome : biomes) {
            if (BiomeConfig.test(type.biomeConfig(), biome)) {
                return true;
            }
        }
        return false;
    }

    private static long mixSeed(long seed, int chunkX, int chunkZ, int salt) {
        long mixed = seed;
        mixed ^= (long) chunkX * 341873128712L;
        mixed ^= (long) chunkZ * 132897987541L;
        mixed ^= (long) salt * 42317861L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccDL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        return mixed ^ mixed >>> 33;
    }

    @Override
    public StructureType<?> type() {
        return IafStructureTypes.DRAGON_CAVE.get();
    }

    public static DragonCaveStructure buildStructureConfig(BootstapContext<Structure> context, DragonCaveType dragonType) {
        return new DragonCaveStructure(
                new StructureSettings(
                        context.lookup(Registries.BIOME).getOrThrow(IafBiomeTagGenerator.dragonCaveTag(dragonType)),
                        new HashMap<>(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                dragonType
        );
    }
}
