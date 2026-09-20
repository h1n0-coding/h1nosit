package dev.h1no.h1nosit.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Невидимое сиденье: игрок садится на него как пассажир.
 * Живёт только пока на нём кто-то сидит и пока цел блок под ним.
 */
public class SeatEntity extends Entity {

    // Блок, на котором стоит сиденье. Сущность не сохраняется в мир,
    // поэтому эти поля не нужно записывать в NBT.
    private BlockPos anchorPos;
    private Block anchorBlock;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
    }

    public void setAnchor(BlockPos pos, Block block) {
        this.anchorPos = pos;
        this.anchorBlock = block;
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        // Нет пассажира -> удаляем (первые ticks не проверяем: пассажир ещё садится).
        if (this.tickCount > 5 && this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }
        // Блок под сиденьем сломали или заменили на другой -> удаляем.
        if (this.anchorPos != null
                && !this.level().getBlockState(this.anchorPos).is(this.anchorBlock)) {
            this.discard();
        }
    }

    // У сиденья нет собственных данных, но Entity требует эти три метода.
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}