package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

    public static final EntityType<SeatEntity> SEAT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(H1n0SSit.MOD_ID, "seat"),
            EntityType.Builder.of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.0f, 0.0f)          // без размера: ни коллизии, ни хитбокса
                    .clientTrackingRange(10)    // на каком расстоянии клиенты видят сиденье
                    .noSave()                   // никогда не записывается в мир
                    .noSummon()
                    .build("seat"));

    private ModEntities() {
    }

    /** Вызов этого метода загружает класс и тем самым регистрирует тип. */
    public static void register() {
    }
}