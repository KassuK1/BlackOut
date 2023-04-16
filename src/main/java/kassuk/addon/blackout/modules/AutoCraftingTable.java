package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCraftingTable extends BlackOutModule {
    public AutoCraftingTable() {
        super(BlackOut.BLACKOUT, "AutoCraftingTable", "Automatically places and opens an Crafting table");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant")
        .description("Opens the table before server has confirmed placing")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Speed")
        .description("Places x times every second")
        .defaultValue(1)
        .min(0)
        .sliderMin(0)
        .build()
    );
    private final Setting<Double> interactSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Interact Speed")
        .description("Sends open packet x times every second")
        .defaultValue(1)
        .min(0)
        .sliderMin(0)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.SilentBypass)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> tColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Table Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public final Setting<SettingColor> tLineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Table Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    BlockPos placePos;
    PlaceData placeData;
    BlockPos tablePos;
    Direction tableDir;
    double placeTimer = 0;
    double interactTimer = 0;
    long lastTime = -1;

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        SilentBypass
    }

    @Override
    public void onActivate() {
        super.onActivate();

    }
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
    void update() {
        if (screenUpdate()) {return;}
        placeUpdate();
        interactUpdate();
    }
    boolean screenUpdate() {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;

        if (screenHandler instanceof CraftingScreenHandler) {
            toggle();
            sendDisableMsg("opened crafting table");
            return true;
        }
        return false;
    }
    void placeUpdate() {
        if (placePos != null && placeData != null && placeData.valid()) {
            if (placeTimer < 1 / placeSpeed.get()) {return;}

            if (place()) {
                placeTimer = 0;
                tablePos = placePos;
            }
        }
    }
    void interactUpdate() {
        if (tablePos != null) {
            tableDir = SettingUtils.getPlaceOnDirection(tablePos);
            if (tableDir == null) {return;}

            if (interactTimer < 1 / interactSpeed.get()) {return;}

            if (interact()) {
                interactTimer = 0;
            }
        }
    }
    boolean interact() {
        boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(tablePos, priority - 0.1, RotationType.Interact);
        if (!rotated) {return false;}

        sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(OLEPOSSUtils.getMiddle(tablePos), tableDir, tablePos, false), 0));

        return true;
    }
    boolean place() {
        Hand hand = Managers.HOLDING.isHolding(Items.CRAFTING_TABLE) ? Hand.MAIN_HAND :
            mc.player.getOffHandStack().getItem() == Items.CRAFTING_TABLE ? Hand.OFF_HAND : null;

        boolean canSwitch = switch (switchMode.get()) {
            case Disabled -> hand != null;
            case Silent, Normal -> InvUtils.findInHotbar(Items.CRAFTING_TABLE).found();
            case SilentBypass -> InvUtils.find(Items.CRAFTING_TABLE).found();
        };


        if (!canSwitch) {return false;}

        boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(placeData.pos(), priority, RotationType.Placing);
        if (!rotated) {return false;}

        boolean switched = false;
        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    InvUtils.swap(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot(), true);
                    switched = true;
                }
                case SilentBypass -> switched = BOInvUtils.invSwitch(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot());
            }
        } else {
            switched = true;
        }

        if (!switched) {return false;}

        sendPacket(new PlayerInteractBlockC2SPacket(hand != null ? hand : Hand.MAIN_HAND, new BlockHitResult(OLEPOSSUtils.getMiddle(placeData.pos()), placeData.dir(), placeData.pos(), false), 0));

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case SilentBypass -> BOInvUtils.swapBack();
            }
        }
        return true;
    }
    PlaceData findPos() {
        int i = (int) Math.ceil(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        PlaceData closestData = null;
        BlockPos closestPos = null;
        double closestDist = 1000;
        double closestEnemyDist = 0;
        double closestVal = 0;
        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    BlockPos pos = new BlockPos(tympanicUtils.vec3dToVec3i(mc.player.getEyePos())).add(x, y, z);

                    if (getBlock(pos) != Blocks.AIR) {continue;}
                    if (getBlock(pos) == Blocks.CRAFTING_TABLE) {
                        tablePos = pos;
                        return null;
                    }

                    PlaceData data = SettingUtils.getPlaceData(pos);
                    if (!data.valid()) {continue;}
                    if (SettingUtils.getPlaceOnDirection(pos) == null) {continue;}

                    double distance = SettingUtils.placeRangeTo(data.pos());
                    if (distance > closestDist) {continue;}

                    double val = value(pos);
                    if (val < closestVal) {continue;}

                    double eDist = distToEnemySQ(pos);
                    if (val == closestVal && eDist < closestEnemyDist) {continue;}

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
    double value(BlockPos pos) {
        double val = 0;
        for (Direction dir : Direction.values()) {
            val += getBlastRes(getBlock(pos.offset(dir)));
        }
        return val;
    }
    double getBlastRes(Block block) {
        return block == Blocks.BEDROCK ? 1500 : block.getBlastResistance();
    }
    double distToEnemySQ(BlockPos pos) {
        double closest = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) {continue;}
            if (Friends.get().isFriend(player)) {continue;}

            double dist = OLEPOSSUtils.distance(player.getEyePos(), OLEPOSSUtils.getMiddle(pos));

            if (dist < closest) {
                closest = dist;
            }
        }

        return closest;
    }
    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }
}

