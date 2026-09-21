package dev.h1no.h1nosit.entity;

import dev.h1no.h1nosit.SitRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Невидимое сиденье: игрок садится на него как пассажир.
 * Живёт только пока на нём кто-то сидит и пока блок под ним подходит для сидения.
 */
public class SeatEntity extends Entity {

    // Блок, на котором стоит сиденье. Сущность не сохраняется в мир,
    // поэтому это поле не нужно записывать в NBT.
    private BlockPos anchorPos;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
    }

    public void setAnchor(BlockPos pos) {
        this.anchorPos = pos;
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    // Сиденье считается «стоящим на земле»: этого требуют блоки, реагирующие на вес.
    @Override
    public boolean onGround() {
        return true;
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
        // Незагруженные чанки не трогаем, чтобы проверка не заставила игру их грузить.
        if (this.anchorPos != null && this.level().hasChunkAt(this.anchorPos)) {
            BlockState state = this.level().getBlockState(this.anchorPos);
            // Блок под сиденьем изменился и больше не подходит -> удаляем.
            if (!SitRules.isSittable(state)) {
                this.discard();
                return;
            }
            // Сиденье «стоит» на блоке: сообщаем блоку, что на нём есть сущность.
            // Так, например, большой капельник начинает наклоняться, как обычно.
            state.entityInside(this.level(), this.anchorPos, this);
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