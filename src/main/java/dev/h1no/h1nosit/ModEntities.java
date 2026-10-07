package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {

    private static final ResourceKey<EntityType<?>> SEAT_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(H1n0SSit.MOD_ID, "seat"));

    public static final EntityType<SeatEntity> SEAT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            SEAT_KEY.identifier(),
            EntityType.Builder.of(SeatEntity::new, MobCategory.MISC)
                    .sized(0f, 0f)
                    .clientTrackingRange(10)
                    //.noSave()
                    .noSummon()
                    .build(SEAT_KEY));

    private ModEntities() {
    }

    public static void register() {
    }
}