package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.core.BlockPos;
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
        if (player.isCreative() && player.getItemInHand(hand).is(Tags.Items.TOOLS_PICKAXES)) {
            player.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 0.4f);
            super.switchTo(level, pos1, level.getBlockState(pos1), Main.BROKEN_WAYSTONE.get());
        }
        else if (player instanceof ServerPlayer srvPlayer && level.getBlockEntity(pos1) instanceof WaystoneEntity waystone && !waystone.isActive(srvPlayer)) {
            waystone.setState(srvPlayer, WaystoneEntity.TURN_ON);
            srvPlayer.setRespawnPosition(level.dimension(), pos1,0f, false, true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WaystoneEntity(pos,state);
    }
}
