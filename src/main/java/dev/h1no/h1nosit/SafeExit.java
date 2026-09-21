package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Сиденье не сохраняется в мир, а игрок сидит ниже точки сиденья (ногами «в блоке»).
 * Поэтому перед сохранением игрока ставим его на точку сиденья, а не оставляем внутри блока.
 */
public final class SafeExit {

    private SafeExit() {
    }

    public static void register() {
        // Игрок вышел из игры (в том числе «Save and Quit» в одиночной игре).
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> standUp(handler.getPlayer()));

        // Сервер начинает остановку: игроки ещё подключены, сохранения ещё не было.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                standUp(player);
            }
        });
    }

    private static void standUp(ServerPlayer player) {
        if (player.getVehicle() instanceof SeatEntity seat) {
            Vec3 pos = seat.position();
            player.stopRiding();
            player.setPos(pos.x, pos.y, pos.z);
        }
    }
}