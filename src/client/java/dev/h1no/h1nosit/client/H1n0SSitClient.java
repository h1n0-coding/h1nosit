package dev.h1no.h1nosit.client;

import dev.h1no.h1nosit.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public class H1n0SSitClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.SEAT, NoopRenderer::new);
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}