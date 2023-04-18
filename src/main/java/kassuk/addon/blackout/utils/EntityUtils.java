package kassuk.addon.blackout.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityUtils {

    public static boolean inside(PlayerEntity en, Box bb) {
        if (mc.world == null) {
            return false;
        }

        return mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    public static Box getCrystalBox(BlockPos pos) {
        return new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
    }

    public static boolean isCrystalBlock(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }

    public static Box getCrystalBox(Vec3d pos) {
        return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
}
