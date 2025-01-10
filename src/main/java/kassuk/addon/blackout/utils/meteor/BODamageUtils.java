/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/player/DamageUtils.java
*/

package kassuk.addon.blackout.utils.meteor;

import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.mixininterface.IExplosion;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BODamageUtils {
    public static RaycastContext raycastContext;

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BODamageUtils.class);
    }

    @EventHandler
    public static void onGameJoin(GameJoinedEvent event) {
        raycastContext = new RaycastContext(Vec3d.ZERO, Vec3d.ZERO, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
    }

    public static double crystalDamage(LivingEntity entity, Box box, Vec3d pos, boolean ignoreTerrain) {
        return crystalDamage(entity, box, pos, null, ignoreTerrain);
    }

    public static double crystalDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, boolean ignoreTerrain) {
        return explosionDamage(entity, box, pos, ignorePos, ignoreTerrain, 6);
    }

    public static double crystalDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain) {
        return explosionDamage(entity, box, pos, ignorePos, obbyPos, ignoreTerrain, 6);
    }

    public static double anchorDamage(LivingEntity entity, Box box, Vec3d pos, boolean ignoreTerrain) {
        return explosionDamage(entity, box, pos, null, ignoreTerrain, 5);
    }

    public static double anchorDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, boolean ignoreTerrain) {
        return explosionDamage(entity, box, pos, ignorePos, ignoreTerrain, 5);
    }

    private static double explosionDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, boolean ignoreTerrain, double strength) {
        return explosionDamage(entity, box, pos, ignorePos, null, ignoreTerrain, strength);
    }

    public static double getBaseDamage(Box box, Vec3d pos, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain, double strength) {
        double q = strength * 2;
        double dist = OLEPOSSUtils.feet(box).distanceTo(pos) / q;

        if (dist > 1.0) return 0;

        double aa = getExposure(pos, box, ignorePos, obbyPos, ignoreTerrain);
        double ab = (1.0 - dist) * aa;

        return (float)((int)((ab * ab + ab) * 3.5 * q + 1.0));
    }

    private static double explosionDamage(LivingEntity entity, Box box, Vec3d pos, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain, double strength) {
        if (box == null) return 0;

        double damage = getBaseDamage(box, pos, ignorePos, obbyPos, ignoreTerrain, strength);

        damage = difficultyDamage(damage);
        damage = applyArmor(entity, damage);
        damage = applyResistance(entity, damage);
        damage = applyProtection(entity, damage, true);

        return damage;
    }

    public static int getProtectionAmount(Iterable<ItemStack> equipment, boolean explosion) {
        MutableInt mint = new MutableInt();

        for (ItemStack stack : equipment) {
            if (stack.isEmpty()) continue;

            ItemEnchantmentsComponent enchantments = stack.getEnchantments();

            enchantments.getEnchantments().stream().forEach(entry -> {
                int level = enchantments.getLevel(entry);
                if (entry.matchesId(Enchantments.PROTECTION.getValue()))
                    mint.add(level);
                else if (explosion && entry.matchesId(Enchantments.BLAST_PROTECTION.getValue()))
                    mint.add(level * 2);
            });
        }

        return mint.intValue();
    }

    public static double difficultyDamage(double damage) {
        Difficulty difficulty = mc.world.getDifficulty();
        if (difficulty == Difficulty.EASY) return Math.min(damage / 2 + 1, damage);
        if (difficulty == Difficulty.NORMAL) return damage;

        return damage * 1.5;
    }

    public static double applyArmor(LivingEntity entity, double damage) {
        double armor = entity.getArmor();
        double f = 2 + entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) / 4;

        return damage * (1 - MathHelper.clamp(armor - damage / f, armor * 0.2, 20) / 25);
    }

    public static double applyResistance(LivingEntity entity, double damage) {
        int amplifier = entity.hasStatusEffect(StatusEffects.RESISTANCE) ? entity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() : 0;

        int j = 25 - (amplifier + 1) * 5;
        return Math.max(damage * j / 25, 0);
    }

    public static double applyProtection(LivingEntity entity, double damage, boolean explosions) {
        int i = getProtectionAmount(entity.getArmorItems(), explosions);
        if (i > 0)
            damage *= (1 - MathHelper.clamp(i, 0f, 20f) / 25);

        return damage;
    }

    public static double getExposure(Vec3d source, Box box) {
        return getExposure(source, box, null, null, true);
    }

    public static double getExposure(Vec3d source, Box box, BlockPos ignorePos, boolean ignoreTerrain) {
        return getExposure(source, box, ignorePos, null, ignoreTerrain);
    }

    public static double getExposure(Vec3d source, Box box, BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain) {
        ((IRaycastContext) raycastContext).set(source, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);

        double lx = box.getLengthX();
        double ly = box.getLengthY();
        double lz = box.getLengthZ();

        double deltaX = 1 / (lx * 2 + 1);
        double deltaY = 1 / (ly * 2 + 1);
        double deltaZ = 1 / (lz * 2 + 1);

        double offsetX = (1 - Math.floor(1 / deltaX) * deltaX) / 2;
        double offsetZ = (1 - Math.floor(1 / deltaZ) * deltaZ) / 2;

        double stepX = deltaX * lx;
        double stepY = deltaY * ly;
        double stepZ = deltaZ * lz;

        if (stepX < 0 || stepY < 0 || stepZ < 0) return 0;

        float i = 0;
        float j = 0;

        for (double x = box.minX + offsetX, maxX = box.maxX + offsetX; x <= maxX; x += stepX) {
            for (double y = box.minY; y <= box.maxY; y += stepY) {
                for (double z = box.minZ + offsetZ, maxZ = box.maxZ + offsetZ; z <= maxZ; z += stepZ) {
                    Vec3d vec3d = new Vec3d(x, y, z);

                    ((IRaycastContext) raycastContext).set(source, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
                    if (raycast(BODamageUtils.raycastContext, ignorePos, obbyPos, ignoreTerrain).getType() == HitResult.Type.MISS) ++i;

                    ++j;
                }
            }
        }

        return i / j;
    }

    public static BlockHitResult raycast(RaycastContext context) {
        return raycast(context, false);
    }

    public static BlockHitResult raycast(RaycastContext context, boolean ignoreTerrain) {
        return raycast(context, null, null, ignoreTerrain);
    }

    public static BlockHitResult raycast(RaycastContext context, BlockPos ignorePos, BlockPos obbyPos) {
        return raycast(context, ignorePos, obbyPos, false);
    }

    public static BlockHitResult raycast(RaycastContext context,BlockPos ignorePos, BlockPos obbyPos, boolean ignoreTerrain) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (contextx, pos) -> {
            BlockState blockState;

            if (pos.equals(obbyPos))
                blockState = Blocks.OBSIDIAN.getDefaultState();
            else if (pos.equals(ignorePos))
                blockState = Blocks.AIR.getDefaultState();
            else {
                BlockState state = mc.world.getBlockState(pos);

                if (ignoreTerrain && state.getBlock().getBlastResistance() < 200) blockState = Blocks.AIR.getDefaultState();
                else blockState = state;
            }

            Vec3d vec3d = contextx.getStart();
            Vec3d vec3d2 = contextx.getEnd();

            VoxelShape voxelShape = contextx.getBlockShape(blockState, mc.world, pos);

            return mc.world.raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);
        }, (contextx) -> {
            Vec3d vec3d = contextx.getStart().subtract(contextx.getEnd());
            return BlockHitResult.createMissed(contextx.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(contextx.getEnd()));
        });
    }
}
