package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.RotationUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.util.math.*;

/**
 * @author OLEPOSSU
 */

public class RangeSettings extends BlackOutModule {
    public RangeSettings() {
        super(BlackOut.SETTINGS, "Range", "Global range settings for every blackout module.");
    }

    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");
    private final SettingGroup sgMining = settings.createGroup("Mining");

    //  Place Ranges
    public final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range")
        .description("Range for placing.")
        .defaultValue(5.2)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    public final Setting<Double> placeRangeWalls = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Range Walls")
        .description("Range for placing behind blocks.")
        .defaultValue(5.2)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    private final Setting<FromMode> placeRangeFrom = sgPlace.add(new EnumSetting.Builder<FromMode>()
        .name("Place Range From")
        .description("Where to calculate place ranges from.")
        .defaultValue(FromMode.Eyes)
        .build()
    );
    private final Setting<PlaceRangeMode> placeRangeMode = sgPlace.add(new EnumSetting.Builder<PlaceRangeMode>()
        .name("Place Range Mode")
        .description("Where to calculate place ranges from.")
        .defaultValue(PlaceRangeMode.NCP)
        .build()
    );
    private final Setting<Double> blockWidth = sgPlace.add(new DoubleSetting.Builder()
        .name("Block Width")
        .description("How wide should the box be for closest range.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> placeRangeMode.get().equals(PlaceRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> blockHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Block Height")
        .description("How tall should the box be for closest range.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> placeRangeMode.get().equals(PlaceRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> placeHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Place Height")
        .description("The height to calculate ranges from.")
        .defaultValue(0.5)
        .sliderRange(0, 1)
        .visible(() -> placeRangeMode.get().equals(PlaceRangeMode.Height))
        .build()
    );

    //  Attack Ranges
    public final Setting<Double> attackRange = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Range")
        .description("Range for attacking entities.")
        .defaultValue(4.8)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    public final Setting<Double> attackRangeWalls = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Range Walls")
        .description("Range for attacking entities behind blocks.")
        .defaultValue(4.8)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    public final Setting<Boolean> reduce = sgAttack.add(new BoolSetting.Builder()
        .name("Reduce")
        .description("Reduces range on every hit by reduce step until it reaches (range - reduce amount).")
        .defaultValue(false)
        .build()
    );
    public final Setting<Double> reduceAmount = sgAttack.add(new DoubleSetting.Builder()
        .name("Reduce Amount")
        .description("Check description from 'Reduce' setting.")
        .defaultValue(0.8)
        .range(0, 6)
        .sliderRange(0, 6)
        .visible(reduce::get)
        .build()
    );
    public final Setting<Double> reduceStep = sgAttack.add(new DoubleSetting.Builder()
        .name("Reduce Step")
        .description("Check description from 'Reduce' setting.")
        .defaultValue(0.14)
        .range(0, 6)
        .sliderRange(0, 6)
        .visible(reduce::get)
        .build()
    );
    private final Setting<FromMode> attackRangeFrom = sgAttack.add(new EnumSetting.Builder<FromMode>()
        .name("Attack Range From")
        .description("Where to calculate ranges from.")
        .defaultValue(FromMode.Eyes)
        .build()
    );
    private final Setting<AttackRangeMode> attackRangeMode = sgAttack.add(new EnumSetting.Builder<AttackRangeMode>()
        .name("Attack Range Mode")
        .description("Where to calculate ranges from.")
        .defaultValue(AttackRangeMode.NCP)
        .build()
    );
    private final Setting<Double> closestAttackWidth = sgAttack.add(new DoubleSetting.Builder()
        .name("Closest Attack Width")
        .description("How wide should the box be for closest range.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> closestAttackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Closest Attack Height")
        .description("How tall should the box be for closest range.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> attackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height")
        .description("The height above feet to calculate ranges from.")
        .defaultValue(1)
        .sliderRange(-2, 2)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.Height))
        .build()
    );

    //  Mining Ranges
    public final Setting<Double> miningRange = sgMining.add(new DoubleSetting.Builder()
        .name("Mining Range")
        .description("Range for mining blocks.")
        .defaultValue(4.8)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    public final Setting<Double> miningRangeWalls = sgMining.add(new DoubleSetting.Builder()
        .name("Mining Range Walls")
        .description("Range for mining blocks behind other blocks.")
        .defaultValue(4.8)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    private final Setting<FromMode> miningRangeFrom = sgMining.add(new EnumSetting.Builder<FromMode>()
        .name("Mining Range From")
        .description("Where to calculate mining ranges from.")
        .defaultValue(FromMode.Eyes)
        .build()
    );
    private final Setting<MiningRangeMode> miningRangeMode = sgMining.add(new EnumSetting.Builder<MiningRangeMode>()
        .name("Mining Range Mode")
        .description("Where to calculate mining ranges from.")
        .defaultValue(MiningRangeMode.NCP)
        .build()
    );
    private final Setting<Double> closestMiningWidth = sgMining.add(new DoubleSetting.Builder()
        .name("Closest Mining Width")
        .description("How wide should the box be for closest range.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> miningRangeMode.get().equals(MiningRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> closestMiningHeight = sgMining.add(new DoubleSetting.Builder()
        .name("Closest Mining Height")
        .description("How tall should the box be for closest range.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 3)
        .visible(() -> miningRangeMode.get().equals(MiningRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> miningHeight = sgMining.add(new DoubleSetting.Builder()
        .name("Mining Height")
        .description("The height above block bottom to calculate ranges from.")
        .defaultValue(0.5)
        .sliderRange(0, 1)
        .visible(() -> miningRangeMode.get().equals(MiningRangeMode.Height))
        .build()
    );

    public double rangeMulti = 0;

    // Place Range Checks
    public boolean inPlaceRange(BlockPos pos, Vec3d from) {
        if (mc.player == null) {
            return false;
        }

        double dist = placeRangeTo(pos, from);
        return dist >= 0 && dist <= (SettingUtils.placeTrace(pos) ? placeRange.get() : placeRangeWalls.get());
    }

    public boolean inPlaceRangeNoTrace(BlockPos pos, Vec3d from) {
        if (mc.player == null) {
            return false;
        }

        double dist = placeRangeTo(pos, from);
        return dist >= 0 && dist <= Math.max(placeRange.get(), placeRangeWalls.get());
    }

    public double placeRangeTo(BlockPos pos, Vec3d from) {
        Box pBB = mc.player.getBoundingBox();
        if (from == null) {
            from = mc.player.getEyePos();
            Vec3d pPos = mc.player.getPos();
            switch (placeRangeFrom.get()) {
                case Middle ->
                    ((IVec3d) from).set((pBB.minX + pBB.maxX) / 2, (pBB.minY + pBB.maxY) / 2, (pBB.minZ + pBB.maxZ) / 2);
                case Feet -> ((IVec3d) from).set(pPos.x, pPos.y, pPos.z);
            }
        }

        Vec3d feet = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        switch (placeRangeMode.get()) {
            case NCP -> {
                return getRange(from, feet.add(0, 0.5, 0));
            }
            case Height -> {
                return getRange(from, feet.add(0, placeHeight.get(), 0));
            }
            case Vanilla -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, 1, 1));
            }
            case CustomBox -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, blockWidth.get(), blockHeight.get()));
            }
        }
        return -1;
    }

    // Attack Range Chesks
    public boolean inAttackRange(Box bb, Vec3d from) {
        return inAttackRange(bb, getFeet(bb), from);
    }

    public boolean inAttackRange(Box bb, Vec3d feet, Vec3d from) {
        if (mc.player == null) {
            return false;
        }

        if (SettingUtils.attackTrace(bb)) {
            return attackRangeTo(bb, feet, from, true) < attackRange.get();
        }
        return attackRangeTo(bb, feet, from, false) < attackRangeWalls.get();
    }

    public boolean inAttackRangeNoTrace(Box bb, Vec3d feet, Vec3d from) {
        if (mc.player == null) {
            return false;
        }

        return attackRangeTo(bb, feet, from, true) <= Math.max(attackRange.get(), attackRangeWalls.get());
    }

    public double attackRangeTo(Box bb, Vec3d feet, Vec3d from, boolean countReduce) {
        Box pBB = mc.player.getBoundingBox();
        if (from == null) {
            from = mc.player.getEyePos();
            switch (attackRangeFrom.get()) {
                case Middle ->
                    ((IVec3d) from).set((pBB.minX + pBB.maxX) / 2, (pBB.minY + pBB.maxY) / 2, (pBB.minZ + pBB.maxZ) / 2);
                case Feet -> from = mc.player.getPos();
            }
        } else {
            switch (attackRangeFrom.get()) {
                case Middle ->
                    from = from.add(0, mc.player.getEyeHeight(mc.player.getPose()) / 2, 0);
                case Eyes ->
                    from = from.add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
            }
        }

        double dist = switch (attackRangeMode.get()) {
            case Height -> getRange(from, feet.add(0, attackHeight.get(), 0));
            case NCP -> getRange(from, new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z));

            case Vanilla ->
                getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX), Math.abs(bb.minY - bb.maxY)));

            case Middle ->
                getRange(from, new Vec3d((bb.minX + bb.maxX) / 2, (bb.minY + bb.maxY) / 2, (bb.minZ + bb.maxZ) / 2));

            case CustomBox ->
                getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX) * closestAttackWidth.get(), Math.abs(bb.minY - bb.maxY) * closestAttackHeight.get()));

            case UpdatedNCP ->
                getRange(from, new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z)) - getDistFromCenter(bb, feet, from);
        };

        return dist * (countReduce && reduce.get() ? rangeMulti : 1);
    }

    public double getDistFromCenter(Box bb, Vec3d feet, Vec3d from) {
        Vec3d startPos = new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z);
        Vec3d rangePos = new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.maxY), feet.z);

        double halfWidth = Math.abs(bb.minX - bb.maxX) / 2f;

        if (from.x == rangePos.x && from.z == rangePos.z) {
            return 0;
        }

        Vec3d dist = new Vec3d(from.x - rangePos.x, 0, from.z - rangePos.z);

        if (getDistXZ(dist) < halfWidth * Math.sqrt(2)) {
            return 0;
        }

        if (dist.getZ() > 0.0) {
            ((IVec3d) rangePos).setXZ(rangePos.x, rangePos.z + halfWidth);
        } else if (dist.getZ() < 0.0) {
            ((IVec3d) rangePos).setXZ(rangePos.x, rangePos.z - halfWidth);
        } else if (dist.getX() > 0.0) {
            ((IVec3d) rangePos).setXZ(rangePos.x + halfWidth, rangePos.z);
        } else ((IVec3d) rangePos).setXZ(rangePos.x - halfWidth, rangePos.z);


        Vec3d vec2 = rangePos.subtract(startPos);
        double angle = RotationUtils.radAngle(new Vec2f((float) dist.x, (float) dist.z), new Vec2f((float) vec2.x, (float) vec2.z));

        if (angle > Math.PI / 4) {
            angle = Math.PI / 2 - angle;
        }

        if (angle >= 0.0 && angle <= Math.PI / 4) {
            return halfWidth / Math.cos(angle);
        } else {
            return 0;
        }
    }

    private double getRange(Vec3d from, Vec3d to) {
        double x = Math.abs(from.x - to.x);
        double y = Math.abs(from.y - to.y);
        double z = Math.abs(from.z - to.z);

        return Math.sqrt(x * x + y * y + z * z);
    }

    private Vec3d getFeet(Box bb) {
        return new Vec3d((bb.minX + bb.maxX) / 2, bb.minY, (bb.minZ + bb.maxZ) / 2);
    }

    // Mining Range Checks
    public boolean inMineRange(BlockPos pos) {
        if (mc.player == null) {
            return false;
        }

        double dist = miningRangeTo(pos, null);
        return dist >= 0 && dist <= (SettingUtils.placeTrace(pos) ? miningRange.get() : miningRangeWalls.get());
    }

    public boolean inMineRangeNoTrace(BlockPos pos) {
        if (mc.player == null) {
            return false;
        }

        double dist = miningRangeTo(pos, null);
        return dist >= 0 && dist <= Math.max(miningRange.get(), miningRangeWalls.get());
    }

    public double miningRangeTo(BlockPos pos, Vec3d from) {
        Box pBB = mc.player.getBoundingBox();
        Vec3d pPos = mc.player.getPos();
        if (from == null) {
            from = mc.player.getEyePos();
            switch (miningRangeFrom.get()) {
                case Middle ->
                    ((IVec3d) from).set((pBB.minX + pBB.maxX) / 2, (pBB.minY + pBB.maxY) / 2, (pBB.minX + pBB.maxX) / 2);
                case Feet -> ((IVec3d) from).set(pPos.x, pPos.y, pPos.z);
            }
        }

        Vec3d feet = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        switch (miningRangeMode.get()) {
            case NCP -> {
                return getRange(from, feet.add(0, 0.5, 0));
            }
            case Height -> {
                return getRange(from, feet.add(0, miningHeight.get(), 0));
            }
            case Vanilla -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, 1, 1));
            }
            case CustomBox -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, closestMiningWidth.get(), closestMiningHeight.get()));
            }
        }
        return -1;
    }

    private double getDistXZ(Vec3d vec) {
        return Math.sqrt(vec.x * vec.x + vec.z * vec.z);
    }

    public void registerAttack(Box bb) {
        if (attackRangeTo(bb, getFeet(bb), null, false) <= attackRange.get() - reduceAmount.get()) {
            rangeMulti = Math.min(rangeMulti + reduceStep.get(), 1);
        } else {
            rangeMulti = Math.max(rangeMulti - reduceStep.get(), (attackRange.get() - reduceStep.get() / attackRange.get()) / attackRange.get());
        }
    }

    public enum PlaceRangeMode {
        NCP,
        Height,
        Vanilla,
        CustomBox
    }

    public enum AttackRangeMode {
        NCP,
        UpdatedNCP,
        Height,
        Vanilla,
        Middle,
        CustomBox
    }

    public enum MiningRangeMode {
        NCP,
        Height,
        Vanilla,
        CustomBox
    }

    public enum FromMode {
        Eyes,
        Middle,
        Feet
    }
}
