package com.ovo.lastsongofelysian.registry;

import com.ovo.lastsongofelysian.block.ElysianRealmEntranceBlock;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlock {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, lastsongofelysian.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, lastsongofelysian.MODID);

    public static final RegistryObject<Block> ELYSIAN_REALM_ENTRANCE =
            BLOCKS.register(
                    "elysian_realm_entrance",
                    ElysianRealmEntranceBlock::new
            );
    public static final RegistryObject<Block> SOUL_STEEL_BLOCK =
            BLOCKS.register(
                    "soul_steel_block",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                                    .mapColor(MapColor.COLOR_BROWN)
                    )
            );

}
