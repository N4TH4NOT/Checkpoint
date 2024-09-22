package me.n4th4not.checkpoint.listener;

import me.n4th4not.checkpoint.level.block.WaystoneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;

import java.util.Objects;

public class PlayerListener {
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {

            if (player.getRespawnPosition() == null ||
                    Objects.equals(event.getSpawnLevel(), player.getRespawnDimension()) && Objects.equals(event.getNewSpawn(), player.getRespawnPosition()))
                return;

            ServerLevel level = player.getServer().getLevel(player.getRespawnDimension());
            if (level != null && level.getBlockEntity(player.getRespawnPosition()) instanceof WaystoneEntity waystone) waystone.setState(player, WaystoneEntity.TURN_OFF);
        }
    }

    public static void onSentChunk(ChunkWatchEvent.Watch event) {
        if (event.getLevel().dimension() != event.getPlayer().getRespawnDimension()) return;
        BlockPos spawn = event.getPlayer().getRespawnPosition();
        if (spawn != null && SectionPos.of(spawn).chunk().equals(event.getChunk().getPos()) && event.getLevel().getBlockEntity(spawn) instanceof WaystoneEntity waystone)
            waystone.setState(event.getPlayer(), WaystoneEntity.SILENT_ON);
    }
}
