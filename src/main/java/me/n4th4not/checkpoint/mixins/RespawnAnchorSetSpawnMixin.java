package me.n4th4not.checkpoint.mixins;

import me.n4th4not.checkpoint.AdditionalGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorSetSpawnMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private void use(ServerPlayer player, ResourceKey<Level> dimKey, BlockPos pos, float angle, boolean forced, boolean msg) {
        if (player.level.getGameRules().getBoolean(AdditionalGameRules.ALLOW_ANCHOR_RESPAWN_POINT)) {
            player.setRespawnPosition(dimKey, pos, angle, forced, msg);
        }
    }
}
