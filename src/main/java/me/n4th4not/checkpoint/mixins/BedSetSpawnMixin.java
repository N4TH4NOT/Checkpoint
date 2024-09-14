package me.n4th4not.checkpoint.mixins;

import me.n4th4not.checkpoint.AdditionalGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public abstract class BedSetSpawnMixin {
    @Shadow public abstract void setRespawnPosition(ResourceKey<Level> p_9159_, @Nullable BlockPos p_9160_, float p_9161_, boolean p_9162_, boolean p_9163_);

    @Redirect(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    public void startSleepInBed(ServerPlayer instance, ResourceKey<Level> dimKey, BlockPos pos, float angle, boolean forced, boolean msg) {
        if (instance.level.getGameRules().getBoolean(AdditionalGameRules.ALLOW_BED_RESPAWN_POINT)) {
            setRespawnPosition(dimKey, pos, angle, forced, msg);
        }
    }
}
