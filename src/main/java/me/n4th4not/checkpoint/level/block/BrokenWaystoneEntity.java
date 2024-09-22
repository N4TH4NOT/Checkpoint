package me.n4th4not.checkpoint.level.block;

import me.n4th4not.checkpoint.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BrokenWaystoneEntity
    extends BlockEntity {

    private static final String NBT_DAMAGE = "Damage";
    public static final int NO_DAMAGE = 0;
    public static final int MAX_DAMAGE = 2;

    private int damage = 0;

    public BrokenWaystoneEntity(BlockPos pos, BlockState state) {
        super(Main.BROKEN_WAYSTONE_TILE.get(), pos, state);
    }

    public int getDamage() {
        return this.damage;
    }

    public void damage(int amount) {
        this.damage = Mth.clamp(this.damage+amount, NO_DAMAGE, MAX_DAMAGE);
    }

    public void repair(int amount) {
        damage(-amount);
    }

    public boolean shouldBeFix() {
        return this.damage > NO_DAMAGE;
    }

    public boolean canBeFix() {
        return this.damage < MAX_DAMAGE;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (shouldBeFix()) nbt.putInt(NBT_DAMAGE, this.damage);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.damage = nbt.getInt(NBT_DAMAGE);
    }
}
