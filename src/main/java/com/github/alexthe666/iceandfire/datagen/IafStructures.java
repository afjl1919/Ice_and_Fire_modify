package com.github.alexthe666.iceandfire.datagen;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.structure.GorgonTempleStructure;
import com.github.alexthe666.iceandfire.world.structure.GraveyardStructure;
import com.github.alexthe666.iceandfire.world.structure.MausoleumStructure;
import com.github.alexthe666.iceandfire.world.structure.dragon.DragonCaveStructure;
import com.github.alexthe666.iceandfire.world.structure.dragon.DragonCaveType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

public class IafStructures {

    public static final ResourceKey<Structure> GRAVEYARD = registerKey("graveyard");
    public static final ResourceKey<Structure> MAUSOLEUM = registerKey("mausoleum");
    public static final ResourceKey<Structure> GORGON_TEMPLE = registerKey("gorgon_temple");
    public static final ResourceKey<Structure> FIRE_DRAGON_CAVE = registerKey("fire_dragon_cave");
    public static final ResourceKey<Structure> ICE_DRAGON_CAVE = registerKey("ice_dragon_cave");
    public static final ResourceKey<Structure> LIGHTNING_DRAGON_CAVE = registerKey("lightning_dragon_cave");

    public static ResourceKey<Structure> registerKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(IceAndFire.MODID, name));
    }

    public static void bootstrap(BootstapContext<Structure> context) {
        context.register(GRAVEYARD, GraveyardStructure.buildStructureConfig(context));
        context.register(MAUSOLEUM, MausoleumStructure.buildStructureConfig(context));
        context.register(GORGON_TEMPLE, GorgonTempleStructure.buildStructureConfig(context));
        context.register(FIRE_DRAGON_CAVE, DragonCaveStructure.buildStructureConfig(context, DragonCaveType.FIRE));
        context.register(ICE_DRAGON_CAVE, DragonCaveStructure.buildStructureConfig(context, DragonCaveType.ICE));
        context.register(LIGHTNING_DRAGON_CAVE, DragonCaveStructure.buildStructureConfig(context, DragonCaveType.LIGHTNING));
    }
}
