package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.mixins.MixinBlockSettings;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockUtils {
    public static boolean isAnvilBlock(Block block) {
        return block instanceof AnvilBlock;
    }

    public static boolean replaceable(BlockPos block) {
        return ((MixinBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).getMaterial().isReplaceable();
    }

    public static boolean solid(BlockPos blockPos) {
        if (mc.world == null) {
            return false;
        }

        Block block = mc.world.getBlockState(blockPos).getBlock();
        return !(block instanceof AbstractFireBlock || block instanceof FluidBlock || block instanceof AirBlock);
    }

    public static boolean solid(Block block) {
        return !(block instanceof AbstractFireBlock || block instanceof FluidBlock || block instanceof AirBlock);
    }
}
