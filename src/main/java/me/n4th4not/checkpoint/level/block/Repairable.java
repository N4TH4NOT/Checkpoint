package me.n4th4not.checkpoint.level.block;

import net.minecraft.world.item.Item;

public interface Repairable {
    Item getFixingItem();
    int getXpCost();
}
