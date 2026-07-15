package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.structure.dragon.DragonCavePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IafStructurePieceTypes {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, IceAndFire.MODID);

    public static final RegistryObject<StructurePieceType> DRAGON_CAVE = STRUCTURE_PIECE_TYPES.register(
            "dragon_cave", () -> (StructurePieceType.ContextlessType) DragonCavePiece::new);

    private IafStructurePieceTypes() {
    }
}
