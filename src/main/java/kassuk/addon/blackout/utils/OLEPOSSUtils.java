package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.mixins.IBlockSettings;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Comparator;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author OLEPOSSU
 */

public class OLEPOSSUtils {
    public static Vec3d feet(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2, box.minY, (box.minZ + box.maxZ) / 2);
    }

    public static boolean hasAquaAffinity(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (hasEnchantment(Enchantments.AQUA_AFFINITY, stack)) return true;
        }
        return false;
    }

    public static boolean hasEnchantment(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.getValue();
        return v.getEnchantments().stream().anyMatch(entry -> entry.matchesId(enchantmentId));
    }

    public static int getLevel(RegistryKey<Enchantment> enchantment, ItemStack stack) {
        ItemEnchantmentsComponent v = stack.getEnchantments();
        Identifier enchantmentId = enchantment.getValue();
        return v.getEnchantments().stream().filter(entry -> entry.matchesId(enchantmentId)).map(v::getLevel).max(Comparator.comparingInt(level -> level)).orElse(0);
    }

    public static Vec3d getMiddle(Box box) {
        return new Vec3d((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2);
    }

    public static boolean inside(PlayerEntity en, Box bb) {
        return mc.world != null && mc.world.getBlockCollisions(en, bb).iterator().hasNext();
    }

    public static int closerToZero(int x) {
        return (int) (x - Math.signum(x));
    }

    public static Vec3d getClosest(Vec3d pPos, Vec3d middle, double width, double height) {
        return new Vec3d(Math.min(Math.max(pPos.x, middle.x - width / 2), middle.x + width / 2),
            Math.min(Math.max(pPos.y, middle.y), middle.y + height),
            Math.min(Math.max(pPos.z, middle.z - width / 2), middle.z + width / 2));
    }

    @SuppressWarnings({"DataFlowIssue", "BooleanMethodIsAlwaysInverted"})
    public static boolean strictDir(BlockPos pos, Direction dir) {
        return switch (dir) {
            case DOWN -> mc.player.getEyePos().y <= pos.getY() + 0.5;
            case UP -> mc.player.getEyePos().y >= pos.getY() + 0.5;
            case NORTH -> mc.player.getZ() < pos.getZ();
            case SOUTH -> mc.player.getZ() >= pos.getZ() + 1;
            case WEST -> mc.player.getX() < pos.getX();
            case EAST -> mc.player.getX() >= pos.getX() + 1;
        };
    }

    public static Box getCrystalBox(BlockPos pos) {
        return new Box(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
    }

    public static Box getCrystalBox(Vec3d pos) {
        return new Box(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean replaceable(BlockPos block) {
        return ((IBlockSettings) AbstractBlock.Settings.copy(mc.world.getBlockState(block).getBlock())).replaceable();
    }

    public static boolean solid2(BlockPos block) {
        return mc.world.getBlockState(block).isSolid();
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "DataFlowIssue"})
    public static boolean solid(BlockPos block) {
        Block b = mc.world.getBlockState(block).getBlock();
        return !(b instanceof AbstractFireBlock || b instanceof FluidBlock || b instanceof AirBlock);
    }

    public static boolean isGapple(Item item) {
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public static boolean isGapple(ItemStack stack) {
        return isGapple(stack.getItem());
    }

    public static boolean collidable(BlockPos block) {
        return ((AbstractBlockAccessor) mc.world.getBlockState(block).getBlock()).isCollidable();
    }
}
