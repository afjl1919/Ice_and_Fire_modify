package com.github.alexthe666.iceandfire.world.structure.dragon;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.block.BlockGoldPile;
import com.github.alexthe666.iceandfire.datagen.tags.IafBlockTags;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.world.IafStructurePieceTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.List;

public class DragonCavePiece extends StructurePiece {
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final long SHELL_SALT = 0x2D54A72D19A4D1B9L;
    private static final long HOLLOW_SALT = 0x6A09E667F3BCC909L;
    private static final long TREASURE_SALT = 0x510E527FADE682D1L;
    private static final long DECORATION_SALT = 0x1F83D9ABFB41BD6BL;
    private static final long DRAGON_SALT = 0x5BE0CD19137E2179L;

    private final DragonCaveType dragonType;
    private final BlockPos center;
    private final int radius;
    private final int dragonAge;
    private final boolean male;
    private final long caveSeed;
    private boolean dragonSpawned;

    public DragonCavePiece(DragonCaveType dragonType, BlockPos center, int radius, int dragonAge, boolean male, long caveSeed) {
        super(IafStructurePieceTypes.DRAGON_CAVE.get(), 0, createBoundingBox(center, radius));
        this.dragonType = dragonType;
        this.center = center.immutable();
        this.radius = radius;
        this.dragonAge = dragonAge;
        this.male = male;
        this.caveSeed = caveSeed;
    }

    public DragonCavePiece(CompoundTag tag) {
        super(IafStructurePieceTypes.DRAGON_CAVE.get(), tag);
        this.dragonType = DragonCaveType.byName(tag.getString("DragonType"));
        this.center = new BlockPos(tag.getInt("CenterX"), tag.getInt("CenterY"), tag.getInt("CenterZ"));
        this.radius = tag.getInt("Radius");
        this.dragonAge = tag.getInt("DragonAge");
        this.male = tag.getBoolean("Male");
        this.caveSeed = tag.getLong("CaveSeed");
        this.dragonSpawned = tag.getBoolean("DragonSpawned");
    }

    private static BoundingBox createBoundingBox(BlockPos center, int radius) {
        int horizontalReach = radius * 2 + 12;
        int verticalReach = radius + 12;
        return new BoundingBox(
                center.getX() - horizontalReach,
                center.getY() - verticalReach,
                center.getZ() - horizontalReach,
                center.getX() + horizontalReach,
                center.getY() + verticalReach,
                center.getZ() + horizontalReach
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("DragonType", this.dragonType.getSerializedName());
        tag.putInt("CenterX", this.center.getX());
        tag.putInt("CenterY", this.center.getY());
        tag.putInt("CenterZ", this.center.getZ());
        tag.putInt("Radius", this.radius);
        tag.putInt("DragonAge", this.dragonAge);
        tag.putBoolean("Male", this.male);
        tag.putLong("CaveSeed", this.caveSeed);
        tag.putBoolean("DragonSpawned", this.dragonSpawned);
    }

    @Override
    public void postProcess(WorldGenLevel worldGen, StructureManager structureManager, net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        List<CaveSphere> spheres = this.createSpheres();
        int minX = Math.max(this.boundingBox.minX(), chunkBox.minX());
        int maxX = Math.min(this.boundingBox.maxX(), chunkBox.maxX());
        int minY = Math.max(Math.max(this.boundingBox.minY(), chunkBox.minY()), worldGen.getMinBuildHeight());
        int maxY = Math.min(Math.min(this.boundingBox.maxY(), chunkBox.maxY()), worldGen.getMaxBuildHeight() - 1);
        int minZ = Math.max(this.boundingBox.minZ(), chunkBox.minZ());
        int maxZ = Math.min(this.boundingBox.maxZ(), chunkBox.maxZ());

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return;
        }

        ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();
        List<Block> rareOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_RARE_ORES);
        List<Block> uncommonOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_UNCOMMON_ORES);
        List<Block> commonOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_COMMON_ORES);
        List<Block> dragonTypeOres = getBlockList(tagManager, this.dragonType.oreTag());

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    CaveBlockKind kind = classify(mutable, spheres);
                    if (kind == CaveBlockKind.SHELL) {
                        this.placeShellBlock(worldGen, mutable, rareOres, uncommonOres, commonOres, dragonTypeOres);
                    } else if (kind == CaveBlockKind.HOLLOW) {
                        BlockState existing = worldGen.getBlockState(mutable);
                        if (!(existing.getBlock() instanceof BaseEntityBlock)) {
                            worldGen.setBlock(mutable, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }

        this.placeStalactites(worldGen, chunkBox, spheres);
        this.placeTreasure(worldGen, chunkBox, spheres, minX, maxX, minY, maxY, minZ, maxZ);
        this.spawnDragon(worldGen, chunkBox);
    }

    private List<CaveSphere> createSpheres() {
        PosRandom random = new PosRandom(mix64(this.caveSeed ^ 0x243F6A8885A308D3L));
        List<CaveSphere> spheres = new ArrayList<>();
        spheres.add(CaveSphere.main(this.radius, this.center, 0));
        int amount = 3 + random.nextInt(2);
        for (int i = 0; i < amount; i++) {
            Direction direction = HORIZONTALS[random.nextInt(HORIZONTALS.length - 1)];
            int sphereRadius = 2 * (int) (this.radius / 3F) + random.nextInt(8);
            BlockPos sphereCenter = this.center.relative(direction, this.radius - 2);
            spheres.add(CaveSphere.satellite(sphereRadius, sphereCenter, i + 1));
        }
        return spheres;
    }

    private CaveBlockKind classify(BlockPos pos, List<CaveSphere> spheres) {
        boolean insideOuter = false;
        for (CaveSphere sphere : spheres) {
            if (sphere.containsHollow(pos, this.caveSeed)) {
                return CaveBlockKind.HOLLOW;
            }
            if (sphere.containsOuter(pos)) {
                insideOuter = true;
            }
        }
        return insideOuter ? CaveBlockKind.SHELL : CaveBlockKind.OUTSIDE;
    }

    private void placeShellBlock(WorldGenLevel worldGen, BlockPos pos, List<Block> rareOres, List<Block> uncommonOres,
                                 List<Block> commonOres, List<Block> dragonTypeOres) {
        BlockState existing = worldGen.getBlockState(pos);
        if (existing.getBlock() instanceof BaseEntityBlock || existing.getDestroySpeed(worldGen, pos) < 0) {
            return;
        }

        PosRandom random = randomAt(pos, SHELL_SALT);
        boolean doOres = random.nextInt(Math.max(1, IafConfig.oreToStoneRatioForDragonCaves + 1)) == 0;
        BlockState replacement = null;
        if (doOres) {
            Block ore = null;
            if (random.nextBoolean()) {
                if (!dragonTypeOres.isEmpty()) {
                    ore = dragonTypeOres.get(random.nextInt(dragonTypeOres.size()));
                }
            } else {
                double chance = random.nextDouble();
                if (!rareOres.isEmpty() && chance <= 0.15D) {
                    ore = rareOres.get(random.nextInt(rareOres.size()));
                } else if (!uncommonOres.isEmpty() && chance <= 0.45D) {
                    ore = uncommonOres.get(random.nextInt(uncommonOres.size()));
                } else if (!commonOres.isEmpty()) {
                    ore = commonOres.get(random.nextInt(commonOres.size()));
                }
            }
            if (ore != null) {
                replacement = ore.defaultBlockState();
            }
        }

        if (replacement == null) {
            replacement = random.nextBoolean() ? this.dragonType.paletteBlock1() : this.dragonType.paletteBlock2();
        }
        worldGen.setBlock(pos, replacement, Block.UPDATE_CLIENTS);
    }

    private void placeStalactites(WorldGenLevel worldGen, BoundingBox chunkBox, List<CaveSphere> spheres) {
        for (CaveSphere sphere : spheres) {
            PosRandom random = new PosRandom(mix64(this.caveSeed ^ DECORATION_SALT ^ ((long) sphere.index * 0x9E3779B97F4A7C15L)));
            int attempts = 15 + random.nextInt(10);
            for (int attempt = 0; attempt < attempts; attempt++) {
                BlockPos start = sphere.center.above(sphere.radius / 2 - 1).offset(
                        random.nextInt(Math.max(1, sphere.radius)) - sphere.radius / 2,
                        0,
                        random.nextInt(Math.max(1, sphere.radius)) - sphere.radius / 2);
                int height = this.dragonType.stalactiteMaxHeight() + random.nextInt(3);
                for (int i = 0; i < height; i++) {
                    BlockPos stem = start.below(i);
                    if (i < height / 2) {
                        this.setIfInside(worldGen, chunkBox, stem.north(), this.dragonType.stalactiteBlock().defaultBlockState(), 2);
                        this.setIfInside(worldGen, chunkBox, stem.east(), this.dragonType.stalactiteBlock().defaultBlockState(), 2);
                        this.setIfInside(worldGen, chunkBox, stem.south(), this.dragonType.stalactiteBlock().defaultBlockState(), 2);
                        this.setIfInside(worldGen, chunkBox, stem.west(), this.dragonType.stalactiteBlock().defaultBlockState(), 2);
                    }
                    this.setIfInside(worldGen, chunkBox, stem, this.dragonType.stalactiteBlock().defaultBlockState(), 2);
                }
            }
        }
    }

    private void placeTreasure(WorldGenLevel worldGen, BoundingBox chunkBox, List<CaveSphere> spheres,
                               int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= Math.min(maxY, this.center.getY() - 1); y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    if (classify(mutable, spheres) != CaveBlockKind.HOLLOW || !worldGen.getBlockState(mutable).isAir()) {
                        continue;
                    }
                    BlockState below = worldGen.getBlockState(mutable.below());
                    if (below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(IafBlockTags.DRAGON_ENVIRONMENT_BLOCKS)) {
                        this.setTreasure(worldGen, chunkBox, mutable.immutable());
                    }
                }
            }
        }
    }

    private void setTreasure(WorldGenLevel worldGen, BoundingBox chunkBox, BlockPos pos) {
        if (worldGen.getBlockState(pos).getBlock() instanceof BaseEntityBlock) {
            return;
        }

        PosRandom random = randomAt(pos, TREASURE_SALT);
        int roll = random.nextInt(99) + 1;
        if (roll < 60) {
            int goldRand = Math.max(1, IafConfig.dragonDenGoldAmount) * (this.male ? 1 : 2);
            if (random.nextInt(Math.max(1, goldRand)) == 0) {
                BlockState pile = this.dragonType.treasurePile().setValue(BlockGoldPile.LAYERS, 1 + random.nextInt(7));
                this.setIfInside(worldGen, chunkBox, pos, pile, 3);
            }
        } else if (roll == 61) {
            Direction facing = HORIZONTALS[random.nextInt(3)];
            this.setIfInside(worldGen, chunkBox, pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_CLIENTS);
            BlockEntity blockEntity = worldGen.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity chest) {
                chest.setLootTable(this.male ? this.dragonType.maleChestLootTable() : this.dragonType.femaleChestLootTable(), random.nextLong());
            }
        }
    }

    private void spawnDragon(WorldGenLevel worldGen, BoundingBox chunkBox) {
        if (this.dragonSpawned || !inside(chunkBox, this.center)) {
            return;
        }
        EntityDragonBase dragon = this.dragonType.dragonEntityType().create(worldGen.getLevel());
        if (dragon == null) {
            return;
        }

        PosRandom random = new PosRandom(mix64(this.caveSeed ^ DRAGON_SALT));
        dragon.setGender(this.male);
        dragon.growDragon(this.dragonAge);
        dragon.setAgingDisabled(true);
        dragon.setHealth(dragon.getMaxHealth());
        dragon.setVariant(random.nextInt(4));
        dragon.absMoveTo(this.center.getX() + 0.5D, this.center.getY() + 0.5D, this.center.getZ() + 0.5D, random.nextFloat() * 360F, 0F);
        dragon.setInSittingPose(true);
        dragon.homePos = new HomePosition(this.center, worldGen.getLevel());
        dragon.setHunger(50);
        this.dragonSpawned = worldGen.addFreshEntity(dragon);
    }

    private PosRandom randomAt(BlockPos pos, long salt) {
        return new PosRandom(mix64(this.caveSeed ^ salt ^ pos.asLong()));
    }

    private static List<Block> getBlockList(ITagManager<Block> tagManager, TagKey<Block> tagKey) {
        if (tagManager == null) {
            return List.of();
        }
        return tagManager.getTag(tagKey).stream().toList();
    }

    private void setIfInside(WorldGenLevel worldGen, BoundingBox box, BlockPos pos, BlockState state, int flags) {
        if (inside(box, pos) && inside(this.boundingBox, pos)) {
            worldGen.setBlock(pos, state, flags);
        }
    }

    private static boolean inside(BoundingBox box, BlockPos pos) {
        return pos.getX() >= box.minX() && pos.getX() <= box.maxX()
                && pos.getY() >= box.minY() && pos.getY() <= box.maxY()
                && pos.getZ() >= box.minZ() && pos.getZ() <= box.maxZ();
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    private enum CaveBlockKind {
        OUTSIDE,
        SHELL,
        HOLLOW
    }

    private static final class CaveSphere {
        private final int radius;
        private final BlockPos center;
        private final int outerYCutoff;
        private final int hollowRadius;
        private final int hollowMinRadius;
        private final int hollowYCutoff;
        private final int index;

        private CaveSphere(int radius, BlockPos center, int outerYCutoff, int index) {
            this.radius = radius;
            this.center = center.immutable();
            this.outerYCutoff = outerYCutoff;
            this.hollowRadius = Math.max(1, radius - 2);
            this.hollowMinRadius = (int) (this.hollowRadius * 0.75F);
            this.hollowYCutoff = this.hollowRadius / 2;
            this.index = index;
        }

        private static CaveSphere main(int radius, BlockPos center, int index) {
            return new CaveSphere(radius, center, radius / 2, index);
        }

        private static CaveSphere satellite(int radius, BlockPos center, int index) {
            return new CaveSphere(radius, center, radius, index);
        }

        private boolean containsOuter(BlockPos pos) {
            int dx = pos.getX() - this.center.getX();
            int dy = pos.getY() - this.center.getY();
            int dz = pos.getZ() - this.center.getZ();
            if (dy > this.outerYCutoff || dy < -this.outerYCutoff) {
                return false;
            }
            return dx * dx + dy * dy + dz * dz <= this.radius * this.radius;
        }

        private boolean containsHollow(BlockPos pos, long caveSeed) {
            int dx = pos.getX() - this.center.getX();
            int dy = pos.getY() - this.center.getY();
            int dz = pos.getZ() - this.center.getZ();
            if (dy > this.hollowYCutoff || dy < -this.hollowYCutoff) {
                return false;
            }
            int distance = dx * dx + dy * dy + dz * dz;
            int minDistance = this.hollowMinRadius * this.hollowMinRadius;
            int maxDistance = this.hollowRadius * this.hollowRadius;
            if (distance <= minDistance) {
                return true;
            }
            if (distance > maxDistance) {
                return false;
            }
            long hash = mix64(caveSeed ^ HOLLOW_SALT ^ pos.asLong() ^ ((long) this.index * 0x9E3779B97F4A7C15L));
            float random = (float) ((hash >>> 40) & 0xFFFFFFL) / (float) 0x1000000;
            float minimum = (float) this.hollowMinRadius / (float) this.hollowRadius;
            float threshold = Math.max(minimum, random);
            return distance <= maxDistance * threshold;
        }
    }

    private static final class PosRandom {
        private long state;

        private PosRandom(long seed) {
            this.state = seed;
        }

        private long nextLong() {
            this.state += 0x9E3779B97F4A7C15L;
            return mix64(this.state);
        }

        private int nextInt(int bound) {
            if (bound <= 1) {
                return 0;
            }
            return (int) Long.remainderUnsigned(nextLong(), bound);
        }

        private boolean nextBoolean() {
            return (nextLong() & 1L) != 0L;
        }

        private float nextFloat() {
            return (float) ((nextLong() >>> 40) & 0xFFFFFFL) / (float) 0x1000000;
        }

        private double nextDouble() {
            return (nextLong() >>> 11) * 0x1.0p-53;
        }
    }
}
