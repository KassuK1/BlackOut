package kassuk.addon.blackout.globalsettings;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU
*/

public class RangeSettings extends Module {
    public RangeSettings() {
        super(BlackOut.SETTINGS, "Range", "Global range settings for every blackout module");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Placing");
    private final SettingGroup sgAttack = settings.createGroup("Attacking");

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
        .description("place for attacking entities behind blocks.")
        .defaultValue(5.2)
        .range(0, 6)
        .sliderRange(0, 6)
        .build()
    );
    private final Setting<FromMode> placeRangeFrom = sgPlace.add(new EnumSetting.Builder<FromMode>()
        .name("Place Range From")
        .description("Where to calculate ranges from.")
        .defaultValue(FromMode.Eyes)
        .build()
    );
    private final Setting<PlaceRangeMode> placeRangeMode = sgPlace.add(new EnumSetting.Builder<PlaceRangeMode>()
        .name("Place Range Mode")
        .description("Where to calculate ranges from.")
        .defaultValue(PlaceRangeMode.NCP)
        .build()
    );
    private final Setting<Double> blockWidth = sgPlace.add(new DoubleSetting.Builder()
        .name("Block Width")
        .description("How wide should the box be for closest range.")
        .defaultValue(2)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> placeRangeMode.get().equals(PlaceRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> blockHeight = sgPlace.add(new DoubleSetting.Builder()
        .name("Block Height")
        .description("How tall should the box be for closest range.")
        .defaultValue(2)
        .range(0, 3)
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
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> closestAttackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Closest Attack Height")
        .description("How tall should the box be for closest range.")
        .defaultValue(1)
        .range(0, 3)
        .sliderRange(0, 3)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.CustomBox))
        .build()
    );
    private final Setting<Double> attackHeight = sgAttack.add(new DoubleSetting.Builder()
        .name("Attack Height")
        .description("The height above feet to calculate ranges from.")
        .defaultValue(1)
        .sliderRange(-1, 2)
        .visible(() -> attackRangeMode.get().equals(AttackRangeMode.Height))
        .build()
    );
    public enum PlaceRangeMode {
        NCP,
        Height,
        Vanilla,
        CustomBox
    }
    public enum AttackRangeMode {
        NCP,
        Height,
        Vanilla,
        Middle,
        CustomBox
    }
    public enum FromMode {
        Eyes,
        Middle,
        Feet
    }

    // Place Range Checks
    public boolean inPlaceRange(BlockPos pos) {
        if (mc.player == null) {return false;}

        double dist = placeRangeTo(pos);
        return dist >= 0 && dist <= (SettingUtils.placeTrace(pos) ? placeRange.get() : placeRangeWalls.get());
    }
    public boolean inPlaceRangeNoTrace(BlockPos pos) {
        if (mc.player == null) {return false;}

        double dist = placeRangeTo(pos);
        return dist >= 0 && dist <= Math.max(placeRange.get(), placeRangeWalls.get());
    }

    public double placeRangeTo(BlockPos pos) {
        Box pBB = mc.player.getBoundingBox();
        Vec3d from = mc.player.getEyePos();
        Vec3d pPos = mc.player.getPos();
        switch (placeRangeFrom.get()) {
            case Middle -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / 2, (pBB.minY + pBB.maxY) / 2, (pBB.minX + pBB.maxX) / 2);
            case Feet -> ((IVec3d)from).set(pPos.x, pPos.y, pPos.z);
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
    public boolean inAttackRange(Box bb, double eyeHeight) {
        return inAttackRange(bb, eyeHeight, getFeet(bb));
    }
    public boolean inAttackRange(Box bb, double eyeHeight, Vec3d feet) {
        if (mc.player == null) {return false;}

        return attackRangeTo(bb, eyeHeight, feet) <= (SettingUtils.attackTrace(bb) ? attackRange.get() : attackRangeWalls.get());
    }
    public boolean inAttackRangeNoTrace(Box bb, double eyeHeight, Vec3d feet) {
        if (mc.player == null) {return false;}

        return attackRangeTo(bb, eyeHeight, feet) <= Math.max(attackRange.get(), attackRangeWalls.get());
    }

    public double attackRangeTo(Box bb, double eyeHeight, Vec3d feet) {
        Box pBB = mc.player.getBoundingBox();
        Vec3d from = mc.player.getEyePos();
        switch (attackRangeFrom.get()) {
            case Middle -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / 2, (pBB.minY + pBB.maxY) / 2, (pBB.minX + pBB.maxX) / 2);
            case Feet -> ((IVec3d)from).set((pBB.minX + pBB.maxX) / 2, pBB.minY, (pBB.minX + pBB.maxX) / 2);
        }

        switch (attackRangeMode.get()) {
            case Height -> {
                return getRange(from, feet.add(0, attackHeight.get(), 0));
            }
            case NCP -> {
                return getRange(from, new Vec3d(feet.x, Math.min(Math.max(from.getY(), bb.minY), bb.minY + eyeHeight), feet.z));
            }
            case Vanilla -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX), Math.abs(bb.minY - bb.maxY)));
            }
            case Middle -> {
                return getRange(from, new Vec3d((bb.minX + bb.maxX) / 2, (bb.minY + bb.maxY) / 2, (bb.minX + bb.maxX) / 2));
            }
            case CustomBox -> {
                return getRange(from, OLEPOSSUtils.getClosest(mc.player.getEyePos(), feet, Math.abs(bb.minX - bb.maxX) * closestAttackWidth.get(), Math.abs(bb.minY - bb.maxY) * closestAttackHeight.get()));
            }
        }
        return -1;
    }

    double getRange(Vec3d from, Vec3d to) {
        double x = Math.abs(from.x - to.x);
        double y = Math.abs(from.y - to.y);
        double z = Math.abs(from.z - to.z);

        return Math.sqrt(x * x + y * y + z * z);
    }

    Vec3d getFeet(Box bb) {
        return new Vec3d((bb.minX + bb.maxX) / 2, bb.minY, (bb.minX + bb.maxX) / 2);
    }
}
