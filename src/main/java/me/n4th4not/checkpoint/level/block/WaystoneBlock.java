package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WaystoneBlock
    extends AbstractWaystone {

    public WaystoneBlock() {
        super();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
        BlockPos pos1 = state.getValue(HALF) == DoubleBlockHalf.LOWER? pos : pos.below();
        if (player instanceof ServerPlayer srvPlayer && level.getBlockEntity(pos1) instanceof WaystoneEntity waystone) {
            if (player.isCreative() && player.getItemInHand(hand).is(Tags.Items.TOOLS_PICKAXES)) {
                super.switchTo(level, pos1, level.getBlockState(pos1), Main.BROKEN_WAYSTONE.get());

                player.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 0.4f);
                for (ServerPlayer target : level.getServer().getPlayerList().getPlayers()) {
                    if (waystone.isActive(target)) target.setRespawnPosition(null, null, 0, false, false);
                }
            }
            else if (!waystone.isActive(srvPlayer)) {
                waystone.setState(srvPlayer, WaystoneEntity.TURN_ON);
                srvPlayer.setRespawnPosition(level.dimension(), pos1, 0f, false, true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (level instanceof ServerLevel srvLevel && srvLevel.getBlockEntity(state.getValue(HALF) == DoubleBlockHalf.LOWER? pos : pos.below()) instanceof WaystoneEntity waystone) {
            for (ServerPlayer target : srvLevel.getServer().getPlayerList().getPlayers()) {
                if (waystone.isActive(target)) target.setRespawnPosition(null, null, 0f, false, false);
            }
        }
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WaystoneEntity(pos,state);
    }
}
