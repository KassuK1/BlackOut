/*
Modified from Meteor Client
https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/utils/player/DamageUtils.java
*/

package kassuk.addon.blackout.utils.meteor;

import kassuk.addon.blackout.utils.EnchantmentUtils;
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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;
import java.util.Set;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BODamageUtils {
    private static final Vec3d vec3d = new Vec3d(0, 0, 0);
    private static Explosion explosion;
    public static RaycastContext raycastContext;
    public static RaycastContext bedRaycast;

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BODamageUtils.class);
    }

    @EventHandler
    private static void onGameJoined(GameJoinedEvent event) {
        explosion = new Explosion(mc.world, null, 0, 0, 0, 6, false, Explosion.DestructionType.DESTROY);
        raycastContext = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
        bedRaycast = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player);
    }

    // Crystal damage
    public static double crystal(PlayerEntity player, Box bb, Vec3d crystal, BlockPos ignore, boolean ignoreTerrain) {
        if (SettingUtils.oldDamage()) return oldVerCrystal(player, bb, crystal, ignore, ignoreTerrain);
        return crystalDamage(player, bb, crystal, ignore, ignoreTerrain);
    }

    public static double crystalDamage(PlayerEntity player, Box bb, Vec3d crystal, BlockPos obsidianPos, boolean ignoreTerrain) {
        if (player == null) return 0;
        if (EntityUtils.getGameMode(player) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) return 0;

        ((IVec3d) vec3d).set((bb.minX + bb.maxX) / 2, bb.minY, (bb.minZ + bb.maxZ) / 2);

        double modDistance = Math.sqrt(vec3d.squaredDistanceTo(crystal));
        if (modDistance > 12) return 0;

        double exposure = getExposure(crystal, player, bb, raycastContext, obsidianPos, ignoreTerrain);
        double impact = (1 - (modDistance / 12)) * exposure;
        double damage = ((impact * impact + impact) / 2 * 7 * (6 * 2) + 1);

        damage = getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft(player, (float) damage, player.getDamageSources().explosion(explosion),  (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        damage = resistanceReduction(player, damage);

        ((IExplosion) explosion).set(crystal, 6, false);
        damage = blastProtReduction(player, damage, explosion);

        return damage < 0 ? 0 : damage;
    }

    public static float oldVerCrystal(PlayerEntity player, Box bb, Vec3d crystal, BlockPos ignore, boolean ignoreTerrain) {
        ((IVec3d) vec3d).set((bb.minX + bb.maxX) / 2, bb.minY, (bb.minZ + bb.maxZ) / 2);

        double dist = vec3d.distanceTo(crystal) / 12;
        if (dist > 1) return 0;

        double exposure = getExposure(crystal, player, bb, raycastContext, ignore, ignoreTerrain);
        double d10 = (1.0D - dist) * exposure;

        float damage = (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)12 + 1.0D));
        damage = (float) getDamageForDifficulty(damage);

        damage = getDamageAfterAbsorb(damage, (float)player.getArmor(), (float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
        damage = oldVerPotionReduce(player, damage);

        return damage;
    }

    public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
        float f = 2.0F + toughnessAttribute / 4.0F;
        float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }


    private static float oldVerPotionReduce(LivingEntity livingEntity, float damage) {
        if (livingEntity.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int i = (livingEntity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float) j;
            damage = f / 25.0F;
        }

        int k = getEnchantmentModifierDamage(livingEntity.getArmorItems());

        if (k > 0)
        {
            damage = getDamageAfterMagicAbsorb(damage, (float)k);
        }

        return damage;
    }

    private static int getEnchantmentModifierDamage(Iterable<ItemStack> stacks) {
        int i = 0;

        for (ItemStack stack : stacks) {
            i += sus(stack);
        }
        return i;
    }

    private static int sus(ItemStack stack) {
        int r = 0;
        if (!stack.isEmpty()) {

            Set<RegistryEntry<Enchantment>> enchantList = stack.getEnchantments().getEnchantments();

            for (RegistryEntry<Enchantment> entry : enchantList)
            {
                if (entry != null)
                {
                    if (entry == Enchantments.BLAST_PROTECTION) {
                        r += EnchantmentUtils.getLevel(Enchantments.BLAST_PROTECTION, stack) * 2;
                    } else if (entry == Enchantments.PROTECTION) {
                        r += EnchantmentUtils.getLevel(Enchantments.PROTECTION, stack);
                    }
                }
            }
        }
        return r;
    }

    private static float getDamageAfterMagicAbsorb(float damage, float enchantModifiers) {
        float f = MathHelper.clamp(enchantModifiers, 0.0F, 20.0F);
        return damage * (1.0F - f / 25.0F);
    }

    // Sword damage
    public static double getSwordDamage(ItemStack stack, PlayerEntity player, PlayerEntity target, boolean charged) {
        // Get sword damage
        double damage = 0;
        if (charged) {
            if (stack.getItem() == Items.NETHERITE_SWORD) {
                damage += 8;
            } else if (stack.getItem() == Items.DIAMOND_SWORD) {
                damage += 7;
            } else if (stack.getItem() == Items.GOLDEN_SWORD) {
                damage += 4;
            } else if (stack.getItem() == Items.IRON_SWORD) {
                damage += 6;
            } else if (stack.getItem() == Items.STONE_SWORD) {
                damage += 5;
            } else if (stack.getItem() == Items.WOODEN_SWORD) {
                damage += 4;
            }
            damage *= 1.5;
        }
        //TODO check both
        if (stack.getEnchantments() != null) {
            if (EnchantmentHelper.getEnchantments(stack).getEnchantments().contains(Enchantments.SHARPNESS)) {
                int level = EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack);
                damage += (0.5 * level) + 0.5;
            }
        }

        if (player.getActiveStatusEffects().containsKey(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            damage += 3 * strength;
        }

        // Reduce by resistance
        damage = resistanceReduction(target, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft(player, (float) damage, target.getDamageSources().playerAttack(player), (float) target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        damage = normalProtReduction(target, damage);

        return damage < 0 ? 0 : damage;
    }


    // Bed damage

    public static double bedDamage(LivingEntity player, Box box, Vec3d bed, BlockPos ignore) {
        if (player instanceof PlayerEntity && ((PlayerEntity) player).getAbilities().creativeMode) return 0;

        double modDistance = Math.sqrt(player.squaredDistanceTo(bed));
        if (modDistance > 10) return 0;

        double exposure = getExposure(bed, player, box, raycastContext, ignore, true);
        double impact = (1.0 - (modDistance / 10.0)) * exposure;
        double damage = (impact * impact + impact) / 2 * 7 * (5 * 2) + 1;

        // Multiply damage by difficulty
        damage = getDamageForDifficulty(damage);

        // Reduce by resistance
        damage = resistanceReduction(player, damage);

        // Reduce by armour
        damage = DamageUtil.getDamageLeft(player, (float) damage, player.getDamageSources().badRespawnPoint(bed), (float) player.getArmor(), (float) player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        ((IExplosion) explosion).set(bed, 5, true);
        damage = blastProtReduction(player, damage, explosion);

        if (damage < 0) damage = 0;
        return damage;
    }

    // Anchor damage

    public static double anchorDamage(LivingEntity player, Box box, BlockPos anchor) {
        return bedDamage(player, box, anchor.toCenterPos(), anchor);
    }

    // Utils

    private static double getDamageForDifficulty(double damage) {
        return switch (mc.world.getDifficulty()) {
            case EASY -> Math.min(damage / 2 + 1, damage);
            case HARD, PEACEFUL -> damage * 3 / 2;
            default -> damage;
        };
    }

    private static double normalProtReduction(LivingEntity player, double damage) {
        float protLevel = EnchantmentHelper.getProtectionAmount((ServerWorld) mc.player.getWorld(), player, mc.world.getDamageSources().generic());
        if (protLevel > 20) protLevel = 20;

        damage *= 1 - (protLevel / 25.0);
        return damage < 0 ? 0 : damage;
    }

    private static double blastProtReduction(LivingEntity player, double damage, Explosion explosion) {
        float protLevel = EnchantmentHelper.getProtectionAmount((ServerWorld) mc.player.getWorld(), player, mc.world.getDamageSources().explosion(explosion));
        if (protLevel > 20) protLevel = 20;

        damage *= (1 - (protLevel / 25.0));
        return damage < 0 ? 0 : damage;
    }

    private static double resistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = (player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1);
            damage *= (1 - (lvl * 0.2));
        }

        return damage < 0 ? 0 : damage;
    }

    public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, BlockPos ignore, boolean ignoreTerrain) {
        double d = 1 / ((box.maxX - box.minX) * 2 + 1);
        double e = 1 / ((box.maxY - box.minY) * 2 + 1);
        double f = 1 / ((box.maxZ - box.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;

        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for (double k = 0; k <= 1; k += d) {
                for (double l = 0; l <= 1; l += e) {
                    for (double m = 0; m <= 1; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                        ((IVec3d) vec3d).set(n + g, o, p + h);
                        ((IRaycastContext) raycastContext).set(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);

                        if (raycast(raycastContext, ignore, ignoreTerrain).getType() == HitResult.Type.MISS) i++;

                        j++;
                    }
                }
            }

            return (double) i / j;
        }

        return 0;
    }

    public static BlockHitResult raycast(RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            blockState = mc.world.getBlockState(blockPos);

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
        });
    }

    private static BlockHitResult raycast(RaycastContext context, BlockPos ignore, boolean ignoreTerrain) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals(ignore)) blockState = Blocks.AIR.getDefaultState();
            else {
                blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain)
                    blockState = Blocks.AIR.getDefaultState();
            }

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();

            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
        });
    }
}
