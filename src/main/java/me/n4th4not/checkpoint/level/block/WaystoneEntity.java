package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class WaystoneEntity
    extends BlockEntity {

    public static final String STATE = "active";
    public static final int TURN_OFF = 0;
    public static final int SILENT_ON = 1;
    public static final int TURN_ON = 2;

    private boolean state = false;

    public WaystoneEntity(BlockPos pos, BlockState state) {
        super(Main.WAYSTONE_TILE.get(), pos, state);
    }

    public void setState(ServerPlayer player, int val) {
        player.connection.send(new ClientboundBlockEventPacket(super.worldPosition, super.getBlockState().getBlock(), 0, val));
    }

    public boolean isActive(ServerPlayer player) {
        return super.level != null && player.getRespawnDimension().equals(super.level.dimension()) &&  super.worldPosition.equals(player.getRespawnPosition());
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(super.worldPosition.getX(),
                super.worldPosition.getY(),
                super.worldPosition.getZ(),
                super.worldPosition.getX() + 1,
                super.worldPosition.getY() + 2,
                super.worldPosition.getZ() + 1);
    }

    @Override
    public boolean triggerEvent(int method, int arg) {
        if (super.level == null || !super.level.isClientSide) return false;

        switch (arg) {
            case 0:
                this.state = false;
                break;
            case 2:
                super.level.playSound(null, super.worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
//                super.level.getEntitiesOfClass(Player.class, Shapes.block().bounds().inflate(8))
//                        .stream().filter(Player::isLocalPlayer)
//                        .forEach(player -> player.displayClientMessage(Component.translatable("chat." + Main.ID + ".waystone_activated").withStyle(ChatFormatting.DARK_AQUA),true));
            case 1:
                this.state = true;
                break;
            default:
                return false;
        }
        super.requestModelDataUpdate();
        return true;
    }
}
