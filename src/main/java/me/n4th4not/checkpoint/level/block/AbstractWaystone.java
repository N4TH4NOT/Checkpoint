package me.n4th4not.checkpoint.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static me.n4th4not.checkpoint.Main.WAYSTONE;
import static net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER;

@SuppressWarnings("deprecation")
public abstract class AbstractWaystone
    extends BaseEntityBlock
    implements SimpleWaterloggedBlock {

    private static final VoxelShape LOWER_SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
            box(1.0, 3.0, 1.0, 15.0, 7.0, 15.0),
            box(2.0, 7.0, 2.0, 14.0, 9.0, 14.0),
            box(3.0, 9.0, 3.0, 13.0, 16.0, 13.0)
    ).optimize();

    private static final VoxelShape UPPER_SHAPE = Shapes.or(
            box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0),
            box(2.0, 8.0, 2.0, 14.0, 10.0, 14.0),
            box(1.0, 10.0, 1.0, 15.0, 12.0, 15.0),
            box(3.0, 12.0, 3.0, 13.0, 14.0, 13.0),
            box(4.0, 14.0, 4.0, 12.0, 16.0, 12.0)
    ).optimize();

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public AbstractWaystone() {
        super(BlockBehaviour.Properties.copy(Blocks.BEDROCK));
        super.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, false));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState state2, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos pos2) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

        DoubleBlockHalf half = state.getValue(HALF);
        if ((dir.getAxis() != Direction.Axis.Y) || ((half == LOWER) != (dir == Direction.UP))
                || ((state2.getBlock() == this) && (state2.getValue(HALF) != half))) {

            if ((half != LOWER) || (dir != Direction.DOWN) || state.canSurvive(level, pos)) return state;
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity blockEntity, @NotNull ItemStack stack) {
        super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, stack);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, HALF);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos, @NotNull PathComputationType compute) {
        return false;
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        if (state.getValue(HALF) == LOWER) return true;

        BlockState below = world.getBlockState(pos.below());
        return below.getBlock() == this && below.getValue(HALF) == LOWER;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (pos.getY() < level.getHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        }
        return null;
    }

    @Override
    public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
        return RespawnAnchorBlock.findStandUpPosition(type,levelReader,pos);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        BlockPos above = pos.above();
        level.setBlockAndUpdate(above,
                state.setValue(HALF, DoubleBlockHalf.UPPER)
                        .setValue(WATERLOGGED, level.getFluidState(above).getType() == Fluids.WATER));
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

    public <T extends AbstractWaystone> void switchTo(Level level, BlockPos bottom, BlockState state, T type) {
        BlockPos top = bottom.above();
        BlockState state1 = level.getBlockState(top);
        if (state1.getBlock() == this) {
            level.removeBlock(bottom, true);
            BlockState def = type.defaultBlockState();
            level.setBlock(
                    bottom,
                    def.setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                            .setValue(FACING, state.getValue(FACING)),
                    0
            );
            type.setPlacedBy(
                    level,
                    bottom,
                    def.setValue(FACING, state.getValue(FACING)),
                    null,
                    null
            );
        }
    }
}
