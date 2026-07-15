package com.github.alexthe666.iceandfire.world.structure.dragon;

import com.github.alexthe666.citadel.config.biome.SpawnBiomeData;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.config.BiomeConfig;
import com.github.alexthe666.iceandfire.datagen.tags.IafBlockTags;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;

public enum DragonCaveType {
    FIRE("fire", 14712409),
    ICE("ice", 72519311),
    LIGHTNING("lightning", 91254187);

    public static final Codec<DragonCaveType> CODEC = Codec.STRING.xmap(DragonCaveType::byName, DragonCaveType::getSerializedName);

    private final String serializedName;
    private final int placementSalt;

    DragonCaveType(String serializedName, int placementSalt) {
        this.serializedName = serializedName;
        this.placementSalt = placementSalt;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public int getPlacementSalt() {
        return this.placementSalt;
    }

    public static DragonCaveType byName(String name) {
        if (name != null) {
            String normalized = name.toLowerCase(Locale.ROOT);
            for (DragonCaveType type : values()) {
                if (type.serializedName.equals(normalized)) {
                    return type;
                }
            }
        }
        return FIRE;
    }

    public BlockState paletteBlock1() {
        return switch (this) {
            case FIRE -> IafBlockRegistry.CHARRED_STONE.get().defaultBlockState();
            case ICE -> IafBlockRegistry.FROZEN_STONE.get().defaultBlockState();
            case LIGHTNING -> IafBlockRegistry.CRACKLED_STONE.get().defaultBlockState();
        };
    }

    public BlockState paletteBlock2() {
        return switch (this) {
            case FIRE -> IafBlockRegistry.CHARRED_COBBLESTONE.get().defaultBlockState();
            case ICE -> IafBlockRegistry.FROZEN_COBBLESTONE.get().defaultBlockState();
            case LIGHTNING -> IafBlockRegistry.CRACKLED_COBBLESTONE.get().defaultBlockState();
        };
    }

    public BlockState treasurePile() {
        return switch (this) {
            case FIRE -> IafBlockRegistry.GOLD_PILE.get().defaultBlockState();
            case ICE -> IafBlockRegistry.SILVER_PILE.get().defaultBlockState();
            case LIGHTNING -> IafBlockRegistry.COPPER_PILE.get().defaultBlockState();
        };
    }

    public Block stalactiteBlock() {
        return switch (this) {
            case FIRE -> IafBlockRegistry.CHARRED_STONE.get();
            case ICE -> IafBlockRegistry.FROZEN_STONE.get();
            case LIGHTNING -> IafBlockRegistry.CRACKLED_STONE.get();
        };
    }

    public int stalactiteMaxHeight() {
        return this == LIGHTNING ? 6 : 3;
    }

    public TagKey<Block> oreTag() {
        return switch (this) {
            case FIRE -> IafBlockTags.FIRE_DRAGON_CAVE_ORES;
            case ICE -> IafBlockTags.ICE_DRAGON_CAVE_ORES;
            case LIGHTNING -> IafBlockTags.LIGHTNING_DRAGON_CAVE_ORES;
        };
    }

    public EntityType<? extends EntityDragonBase> dragonEntityType() {
        return switch (this) {
            case FIRE -> IafEntityRegistry.FIRE_DRAGON.get();
            case ICE -> IafEntityRegistry.ICE_DRAGON.get();
            case LIGHTNING -> IafEntityRegistry.LIGHTNING_DRAGON.get();
        };
    }

    public Pair<String, SpawnBiomeData> biomeConfig() {
        return switch (this) {
            case FIRE -> BiomeConfig.fireDragonCaveBiomes;
            case ICE -> BiomeConfig.iceDragonCaveBiomes;
            case LIGHTNING -> BiomeConfig.lightningDragonCaveBiomes;
        };
    }

    public ResourceLocation femaleChestLootTable() {
        return new ResourceLocation(IceAndFire.MODID, "chest/" + this.serializedName + "_dragon_female_cave");
    }

    public ResourceLocation maleChestLootTable() {
        return new ResourceLocation(IceAndFire.MODID, "chest/" + this.serializedName + "_dragon_male_cave");
    }
}
