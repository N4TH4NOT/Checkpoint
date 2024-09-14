package me.n4th4not.checkpoint;

import net.minecraft.world.level.GameRules;

public class AdditionalGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_BED_RESPAWN_POINT = GameRules.register("setSpawnWithBed", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> ALLOW_ANCHOR_RESPAWN_POINT = GameRules.register("setSpawnWithAnchor", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(false));

    static void register() {
    }
}
