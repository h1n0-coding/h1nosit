package dev.h1no.h1nosit;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {

    /** Блоки, на которые можно сесть: data/h1n0s-sit/tags/block/sittable.json */
    public static final TagKey<Block> SITTABLE = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(H1n0SSit.MOD_ID, "sittable"));

    private ModTags() {
    }
}