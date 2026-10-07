package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class SitHandler {

    /** Сколько игроков может сидеть на одном блоке. */
    private static final int MAX_SITTERS_PER_BLOCK = 2;

    /** Минимальное расстояние между двумя сидящими на блоке (ширина игрока). */
    private static final double MIN_SEAT_DISTANCE = 0.6;

    private SitHandler() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register(SitHandler::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        // Общие условия: не наблюдатель, ещё не сидит, не приседает, обе руки пустые.
        // Проверяются одинаково и на клиенте, и на сервере — эти данные видны обеим сторонам.
        if (player.isSpectator() || player.isPassenger() || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        // Кликнули по верхней грани подходящего блока.
        if (hit.getDirection() != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!SitRules.isSittable(state) || !SitRules.canStartSitting(level, state)) {
            return InteractionResult.PASS;
        }

        // Место занято другим сидящим? Проверяется одинаково на обеих сторонах:
        // сиденья видны клиенту благодаря clientTrackingRange.
        Vec3 at = hit.getLocation();
        if (!hasRoomFor(level, pos, at)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            // У клиента есть все данные, чтобы честно предсказать исход —
            // поэтому отвечаем SUCCESS, а не PASS. Это не решение о посадке
            // (его по-прежнему принимает только сервер), а просто сигнал игре
            // не пытаться угадывать клик самой: похоже, именно неопределённость
            // здесь провоцирует баг 26.1.2 с ложным сообщением о высоте.
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        // Создаём сиденье там, куда кликнул игрок, и сажаем игрока.
        SeatEntity seat = ModEntities.SEAT.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (seat == null) {
            return InteractionResult.PASS;
        }
        seat.setPos(at.x, at.y, at.z);
        seat.setAnchor(pos);
        serverLevel.addFreshEntity(seat);

        boolean rode = serverPlayer.startRiding(seat, true, true);
        if (!rode) {
            seat.discard();
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Есть ли на этом блоке место ещё для одного сидящего в точке клика. */
    private static boolean hasRoomFor(Level level, BlockPos pos, Vec3 at) {
        List<SeatEntity> seats = level.getEntities(
                net.minecraft.world.level.entity.EntityTypeTest.forClass(SeatEntity.class),
                new AABB(pos).inflate(1.0),
                seat -> pos.equals(seat.getAnchorPos()) && !seat.getPassengers().isEmpty()
        );
        if (seats.size() >= MAX_SITTERS_PER_BLOCK) {
            return false;
        }
        for (SeatEntity seat : seats) {
            double dx = seat.getX() - at.x;
            double dz = seat.getZ() - at.z;
            if (dx * dx + dz * dz < MIN_SEAT_DISTANCE * MIN_SEAT_DISTANCE) {
                return false;
            }
        }
        return true;
    }
}