package dev.h1no.h1nosit;

import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.Tilt;

/**
 * Все условия «можно ли сидеть на этом блоке».
 * Список блоков задаёт тег h1n0s-sit:sittable, а здесь только то,
 * что тегом не выразить: состояния блока.
 */
public final class SitRules {

    private SitRules() {
    }

    public static boolean isSittable(BlockState state) {
        if (!state.is(ModTags.SITTABLE)) {
            return false;
        }
        // Двойная плита — по сути целый блок, на ней не сидим.
        if (state.getBlock() instanceof SlabBlock
                && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            return false;
        }
        // Большой капельник: сидим, пока лист не наклонился полностью.
        if (state.getBlock() instanceof BigDripleafBlock
                && state.getValue(BlockStateProperties.TILT) == Tilt.FULL) {
            return false;
        }
        return true;
    }
}