package com.github.alexthe666.iceandfire.datagen;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.structure.dragon.DragonCaveType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IafBiomeTagGenerator extends BiomeTagsProvider {
    public static final TagKey<Biome> HAS_GORGON_TEMPLE = createKey("has_structure/gorgon_temple");
    public static final TagKey<Biome> HAS_MAUSOLEUM = createKey("has_structure/mausoleum");
    public static final TagKey<Biome> HAS_GRAVEYARD = createKey("has_structure/graveyard");
    public static final TagKey<Biome> HAS_FIRE_DRAGON_CAVE = createKey("has_structure/fire_dragon_cave");
    public static final TagKey<Biome> HAS_ICE_DRAGON_CAVE = createKey("has_structure/ice_dragon_cave");
    public static final TagKey<Biome> HAS_LIGHTNING_DRAGON_CAVE = createKey("has_structure/lightning_dragon_cave");

    private static TagKey<Biome> createKey(String name) {
        return TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation(IceAndFire.MODID, name));
    }

    public static TagKey<Biome> dragonCaveTag(DragonCaveType type) {
        return switch (type) {
            case FIRE -> HAS_FIRE_DRAGON_CAVE;
            case ICE -> HAS_ICE_DRAGON_CAVE;
            case LIGHTNING -> HAS_LIGHTNING_DRAGON_CAVE;
        };
    }

    public IafBiomeTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(HAS_GRAVEYARD).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_MAUSOLEUM).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_GORGON_TEMPLE).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_FIRE_DRAGON_CAVE).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_ICE_DRAGON_CAVE).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_LIGHTNING_DRAGON_CAVE).addTag(BiomeTags.IS_OVERWORLD);
    }

    @Override
    public String getName() {
        return "Ice and Fire Biome Tags";
    }
}
