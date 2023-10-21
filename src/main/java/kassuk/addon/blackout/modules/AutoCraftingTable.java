package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

import static kassuk.addon.blackout.utils.OLEPOSSUtils.replaceable;

/**
 * @author OLEPOSSU
 */

public class AutoCraftingTable extends BlackOutModule {
    public AutoCraftingTable() {
        super(BlackOut.BLACKOUT, "Auto Crafting Table", "Automatically places and opens an Crafting table.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Switching method. Silent is the most reliable but doesn't work everywhere..")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Double> placeSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description("Tries to place this many times every second.")
        .defaultValue(1)
        .min(0)
        .sliderMin(0)
        .build()
    );
    private final Setting<Double> interactSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Interact Speed")
        .description("Tries to open the crafting table this many times every second.")
        .defaultValue(1)
        .min(0)
        .sliderMin(0)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing the crafting table.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> interactSwing = sgRender.add(new BoolSetting.Builder()
        .name("Interact Swing")
        .description("Renders swing animation when interacting with a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> interactHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Interact Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(interactSwing::get)
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private BlockPos placePos;
    private PlaceData placeData;
    private BlockPos tablePos;
    private Direction tableDir;
    private double placeTimer = 0;
    private double interactTimer = 0;
    private long lastTime = -1;

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (lastTime < 0) {
            lastTime = System.currentTimeMillis();
        }
        double delta = (System.currentTimeMillis() - lastTime) / 1000f;
        placeTimer += delta;
        interactTimer += delta;
        placeData = findPos();
        if (placePos != null) {
            event.renderer.box(placePos, color.get(), lineColor.get(), ShapeMode.Both, 0);
        }
        update();
        lastTime = System.currentTimeMillis();
    }

    private void update() {
        if (screenUpdate()) {
            return;
        }
        placeUpdate();
        interactUpdate();
    }

    private boolean screenUpdate() {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;

        if (screenHandler instanceof CraftingScreenHandler) {
            toggle();
            sendDisableMsg("opened crafting table");
            return true;
        }
        return false;
    }

    private void placeUpdate() {
        if (placePos != null && placeData != null && placeData.valid()) {
            if (placeTimer < 1 / placeSpeed.get()) {
                return;
            }

            if (place()) {
                placeTimer = 0;
                tablePos = placePos;
            }
        }
    }

    private void interactUpdate() {
        if (tablePos != null) {
            tableDir = SettingUtils.getPlaceOnDirection(tablePos);
            if (tableDir == null) {
                return;
            }

            if (interactTimer < 1 / interactSpeed.get()) {
                return;
            }

            if (interact()) {
                interactTimer = 0;
            }
        }
    }

    private boolean interact() {
        boolean rotated = !SettingUtils.shouldRotate(RotationType.Interact) || Managers.ROTATION.start(tablePos, priority - 0.1, RotationType.Interact, Objects.hash(name + "interact"));
        if (!rotated) {
            return false;
        }

        interactBlock(Hand.MAIN_HAND, tablePos.toCenterPos(), tableDir, tablePos);

        if (SettingUtils.shouldRotate(RotationType.Interact)) Managers.ROTATION.end(Objects.hash(name + "interact"));
        if (interactSwing.get()) clientSwing(interactHand.get(), Hand.MAIN_HAND);

        return true;
    }

    private boolean place() {
        Hand hand = Managers.HOLDING.isHolding(Items.CRAFTING_TABLE) ? Hand.MAIN_HAND :
            mc.player.getOffHandStack().getItem() == Items.CRAFTING_TABLE ? Hand.OFF_HAND : null;

        boolean canSwitch = switch (switchMode.get()) {
            case Disabled -> hand != null;
            case Silent, Normal -> InvUtils.findInHotbar(Items.CRAFTING_TABLE).found();
            case PickSilent, InvSwitch -> InvUtils.find(Items.CRAFTING_TABLE).found();
        };


        if (!canSwitch) {
            return false;
        }

        boolean rotated = !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"));
        if (!rotated) {
            return false;
        }

        boolean switched = false;
        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot(), true);
                    switched = true;
                }
                case PickSilent -> switched = BOInvUtils.pickSwitch(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot());
                case InvSwitch -> switched = BOInvUtils.invSwitch(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot());
            }
        } else {
            switched = true;
        }

        if (!switched) {
            return false;
        }

        Hand rHand = hand != null ? hand : Hand.MAIN_HAND;

        placeBlock(rHand, placeData.pos().toCenterPos(), placeData.dir(), placeData.pos());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) Managers.ROTATION.end(Objects.hash(name + "placing"));
        if (placeSwing.get()) clientSwing(placeHand.get(), rHand);

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private PlaceData findPos() {
        int i = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        PlaceData closestData = null;
        BlockPos closestPos = null;
        double closestDist = 1000;
        double closestEnemyDist = 0;
        double closestVal = 0;
        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    BlockPos pos = BlockPos.ofFloored(mc.player.getEyePos()).add(x, y, z);

                    if (!replaceable(pos)) continue;

                    if (getBlock(pos) == Blocks.CRAFTING_TABLE) {
                        tablePos = pos;
                        return null;
                    }

                    PlaceData data = SettingUtils.getPlaceData(pos);

                    if (!data.valid() || SettingUtils.getPlaceOnDirection(pos) == null) continue;

                    double distance = SettingUtils.placeRangeTo(data.pos());
                    if (distance > closestDist) continue;


                    double val = value(pos);
                    if (val < closestVal) continue;


                    double eDist = distToEnemySQ(pos);
                    if (val == closestVal && eDist < closestEnemyDist) continue;


                    if (EntityUtils.intersectsWithEntity(new Box(pos), entity -> !(entity instanceof ItemEntity) && !entity.isSpectator()))
                        continue;


                    closestData = data;
                    closestPos = pos;
                    closestDist = distance;
                    closestEnemyDist = eDist;
                    closestVal = val;
                }
            }
        }
        placePos = closestPos;
        return closestData;
    }

    private double value(BlockPos pos) {
        double val = 0;
        for (Direction dir : Direction.values()) {
            val += getBlastRes(getBlock(pos.offset(dir)));
        }
        return val;
    }

    private double getBlastRes(Block block) {
        return block == Blocks.BEDROCK ? 1500 : block.getBlastResistance();
    }

    private double distToEnemySQ(BlockPos pos) {
        double closest = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) {
                continue;
            }
            if (Friends.get().isFriend(player)) {
                continue;
            }

            double dist = player.getEyePos().distanceTo(Vec3d.ofCenter(pos));

            if (dist < closest) {
                closest = dist;
            }
        }

        return closest;
    }

    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
}
