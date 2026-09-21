package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        // Всё решает сервер; событие приходит и на клиенте, там ничего не делаем.
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        // Условия: не наблюдатель, ещё не сидит, не приседает, обе руки пустые.
        if (player.isSpectator() || player.isPassenger() || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        // Кликнули по верхней грани блока из тега h1n0s-sit:sittable.
        if (hit.getDirection() != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!SitRules.isSittable(state)) {
            return InteractionResult.PASS;
        }
        // Дополнительные условия, только для момента посадки (например, кровать днём).
        if (!SitRules.canStartSitting(level, state)) {
            return InteractionResult.PASS;
        }

        // На блоке уже сидят двое, или рядом с точкой клика уже сидит другой игрок?
        Vec3 at = hit.getLocation();
        if (!hasRoomFor(serverLevel, pos, at)) {
            return InteractionResult.PASS;
        }

        // Создаём сиденье там, куда кликнул игрок, и сажаем игрока.
        SeatEntity seat = ModEntities.SEAT.create(serverLevel);
        if (seat == null) {
            return InteractionResult.PASS;
        }
        seat.setPos(at.x, at.y, at.z);
        seat.setAnchor(pos);
        serverLevel.addFreshEntity(seat);

        if (!serverPlayer.startRiding(seat, true)) {
            seat.discard();
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    /** Есть ли на этом блоке место ещё для одного сидящего в точке клика. */
    private static boolean hasRoomFor(ServerLevel level, BlockPos pos, Vec3 at) {
        List<SeatEntity> seats = level.getEntitiesOfClass(
                SeatEntity.class,
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