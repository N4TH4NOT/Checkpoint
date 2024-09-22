package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.n4th4not.checkpoint.Main.WAYSTONE;

//TODO: Update block mark for nearby players when block was placed again
@SuppressWarnings("deprecation")
public class BrokenWaystoneBlock
    extends AbstractWaystone
    implements Repairable {

    public static final BooleanProperty FIXABLE = BooleanProperty.create("fixable");
    private static final int EVENT_STATE_ID = getId(WAYSTONE.get().defaultBlockState());

    public BrokenWaystoneBlock() {
        super();
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        boolean bypass = player.isCreative();
        if (bypass || state.getValue(FIXABLE)) {
            DoubleBlockHalf half = state.getValue(HALF);
            boolean flag = half == DoubleBlockHalf.LOWER;
            BlockPos pos1 = pos.relative(flag ? Direction.UP : Direction.DOWN);
            if (player instanceof ServerPlayer srvPlayer && level.getBlockEntity(flag ? pos : pos1) instanceof BrokenWaystoneEntity waystone) {
                if (waystone.canBeFix()) {
                    ItemStack item = player.getItemInHand(hand);
                    if (bypass && item.is(Tags.Items.TOOLS_PICKAXES)) {
                        waystone.damage(1);
                        level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, EVENT_STATE_ID);
                        boolean flag1 = waystone.canBeFix();
                        level.playSound(
                                null,
                                flag ? pos1 : pos,
                                flag1 ? SoundEvents.TUFF_BREAK : SoundEvents.DEEPSLATE_BREAK,
                                SoundSource.BLOCKS,
                                1.0f,
                                flag1 ? 0.92f - waystone.getDamage() * 0.11f + level.random.nextInt(8) * 0.01f : 1.0f
                        );
                    }
                    else if (waystone.shouldBeFix()) {
                        if (!item.is(getFixingItem())) return InteractionResult.PASS;
                        if (!bypass) item.shrink(1);
                        waystone.repair(1 + (level.random.nextFloat() <= 0.12f ? 1 : 0));

                        level.levelEvent(
                                player,
                                LevelEvent.PARTICLES_DESTROY_BLOCK,
                                flag && level.getBlockState(pos.relative(Direction.UP)).getBlock() == this ? pos : pos.relative(Direction.UP),
                                EVENT_STATE_ID
                        );
                        level.playSound(null, flag ? pos1 : pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    else if (bypass || player.experienceLevel >= getXpCost()) {
                        super.switchTo(level, flag? pos : pos1, flag? state : level.getBlockState(pos1), WAYSTONE.get());
                        if (!bypass) player.giveExperiencePoints(-getXpCost());
                        level.playSound(null, flag ? pos1 : pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    else {
                        player.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.AMBIENT, 0.8f, 0.6f + level.random.nextInt(10) * 0.01f);
                        srvPlayer.sendSystemMessage(Component.translatable("chat.checkpoint.no_enough_xp").withStyle(ChatFormatting.DARK_RED), true);
                    }
                }
                else return InteractionResult.PASS;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FIXABLE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BrokenWaystoneEntity(pos,state);
    }

    @Override
    public @NotNull Item getFixingItem() {
        return Items.ANDESITE;
    }

    @Override
    public int getXpCost() {
        return 10; //lvl
    }
}
