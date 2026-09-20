package dev.h1no.h1nosit.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Невидимое сиденье: игрок садится на него как пассажир.
 * Живёт только пока на нём кто-то сидит.
 */
public class SeatEntity extends Entity {

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        // Самоочистка (только на сервере): нет пассажира -> удаляем сиденье.
        // Первые ticks не проверяем, чтобы пассажиры успели сесть.
        if (!this.level().isClientSide && this.tickCount > 5 && this.getPassengers().isEmpty()) {
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