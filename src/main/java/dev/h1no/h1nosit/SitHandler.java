package dev.h1no.h1nosit;

import dev.h1no.h1nosit.entity.SeatEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SitHandler {

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

        // Кликнули по верхней грани подходящего блока (пока: любые ступеньки).
        if (hit.getDirection() != Direction.UP) {
            return InteractionResult.PASS;
        }
        if (!level.getBlockState(hit.getBlockPos()).is(BlockTags.STAIRS)) {
            return InteractionResult.PASS;
        }

        // Создаём сиденье там, куда кликнул игрок, и сажаем игрока.
        SeatEntity seat = ModEntities.SEAT.create(serverLevel);
        if (seat == null) {
            return InteractionResult.PASS;
        }
        Vec3 at = hit.getLocation();
        seat.setPos(at.x, at.y, at.z);
        serverLevel.addFreshEntity(seat);

        if (!serverPlayer.startRiding(seat, true)) {
            seat.discard();
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
}