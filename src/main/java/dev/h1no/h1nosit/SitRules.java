package dev.h1no.h1nosit;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.Tilt;

/**
 * Все условия «можно ли сидеть на этом блоке».
 * Список блоков задаёт тег h1n0s-sit:sittable, а здесь только то,
 * что тегом не выразить: состояния блока и условия момента посадки.
 */
public final class SitRules {

    private SitRules() {
    }

    /** Подходит ли блок для сидения. Проверяется и при посадке, и пока сиденье живо. */
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
        // Кровать: на занятой (в ней кто-то спит) не сидим.
        if (state.getBlock() instanceof BedBlock
                && state.getValue(BlockStateProperties.OCCUPIED)) {
            return false;
        }
        return true;
    }

    /** Дополнительные условия, которые проверяются только в момент посадки. */
    public static boolean canStartSitting(Level level, BlockState state) {
        if (state.getBlock() instanceof BedBlock) {
            // Кровать: садимся только там, где она работает как кровать (не Незер/Энд),
            // и только днём, когда лечь спать всё равно нельзя.
            return level.dimensionType().bedWorks() && level.isDay();
        }
        return true;
    }
}