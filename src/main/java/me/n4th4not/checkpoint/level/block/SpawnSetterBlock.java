package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER;

@SuppressWarnings("deprecation")
public abstract class SpawnSetterBlock
    extends BaseEntityBlock
    implements SimpleWaterloggedBlock {

    /*
    * REFS
    * ServerPlayer#setRespawnPosition(dimensionKey, position, angle, blockMethodPositionFinder, displayMessage)
    */

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected SpawnSetterBlock(Properties prop) {
        super(prop);
        super.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState state2, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos pos2) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

        if (isDoubleBlock(state)) {
            DoubleBlockHalf half = state.getValue(HALF);
            if ((dir.getAxis() != Direction.Axis.Y) || ((half == LOWER) != (dir == Direction.UP))
                    || ((state2.getBlock() == this) && (state2.getValue(HALF) != half))) {

                if ((half != LOWER) || (dir != Direction.DOWN) || state.canSurvive(level, pos)) return state;
            }

            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack stack) {
        if (isDoubleBlock(state)) super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, stack);
        else super.playerDestroy(world, player, pos, state, blockEntity, stack);
    }

    private boolean isDoubleBlock(BlockState state) {
        return state.hasProperty(HALF);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        if (!isDoubleBlock(state)) return true;

        if (state.getValue(HALF) == LOWER) return true;

        BlockState below = world.getBlockState(pos.below());
        return below.getBlock() == this && below.getValue(HALF) == LOWER;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (pos.getY() < world.getHeight() - 1 && world.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
        }
        return null;
    }

    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
        BlockPos fixedPos = state.getValue(HALF) == LOWER? pos : pos.below();
        if (player instanceof ServerPlayer srvPlayer && level.getBlockEntity(fixedPos) instanceof WaystoneEntity waystone && !waystone.isActive(srvPlayer)) {
            waystone.setState(srvPlayer, WaystoneEntity.TURN_ON);
            srvPlayer.setRespawnPosition(level.dimension(), fixedPos,0f, false, true);
            //TODO: Play sound ??
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (isDoubleBlock(state)) {
            BlockPos above = pos.above();
            world.setBlockAndUpdate(above,
                    state.setValue(HALF, DoubleBlockHalf.UPPER)
                         .setValue(WATERLOGGED, world.getFluidState(above).getType() == Fluids.WATER));
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
