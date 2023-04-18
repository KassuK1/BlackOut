package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * @author KassuK
 * @author OLEPOSSU
 * @author ccetl
 */
public class SurroundPlus extends BlackOutModule {
    public SurroundPlus() {
        super(BlackOut.BLACKOUT, "Surround+", "KasumsSoft surround");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgToggle = settings.createGroup("Toggle");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyConfirmed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Confirmed")
        .description("Only places on blocks the server has confirmed to exist")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<Boolean> floor = sgGeneral.add(new BoolSetting.Builder()
        .name("Floor")
        .description("Places blocks under your feet.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0).range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgGeneral.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place")
        .defaultValue(1).range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between placing at each spot.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //  Toggle Page
    private final Setting<Boolean> toggleMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description("Toggles when you move horizontally")
        .defaultValue(true)
        .build()
    );
    private final Setting<ToggleYMode> toggleY = sgToggle.add(new EnumSetting.Builder<ToggleYMode>()
        .name("Toggle Y")
        .description("Toggles when you move vertically")
        .defaultValue(ToggleYMode.Full)
        .build()
    );
    private final Setting<Boolean> toggleSneak = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Sneak")
        .description("Toggles when you sneak")
        .defaultValue(false)
        .build()
    );

    //  Render Page
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<ShapeMode> supportShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Support Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> supportLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 150))
        .build()
    );
    private final Setting<SettingColor> supportSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Side Color")
        .description(".")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final BlockTimerList timers = new BlockTimerList();
    private BlockPos startPos = null;
    private double placeTimer = 0;
    private int placesLeft = 0;
    private List<Render> render = new ArrayList<>();
    public static List<BlockPos> attack = new ArrayList<>();
    private final BlockTimerList placed = new BlockTimerList();
    private boolean lastSneak = false;
    public static boolean placing = false;

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null || mc.world == null) {
            toggle();
        }
        startPos = mc.player.getBlockPos();
        placesLeft = places.get();
        placeTimer = 0;
        render = new ArrayList<>();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }
        render.forEach(item -> event.renderer.box(WorldUtils.getBox(item.pos), item.support ? supportSideColor.get() : sideColor.get(), item.support ? supportLineColor.get() : lineColor.get(), item.support ? supportShapeMode.get() : shapeMode.get(), 0));
        update();
    }

    private void update() {
        if (mc.player == null && mc.world == null) {
            return;
        }

        placing = false;

        // Move Check
        if (toggleMove.get() && (mc.player.getBlockPos().getX() != startPos.getX() || mc.player.getBlockPos().getZ() != startPos.getZ())) {
            sendDisableMsg("moved");
            toggle();
            return;
        }

        // Y Check
        switch (toggleY.get()) {
            case Full -> {
                if (mc.player.getBlockPos().getY() != startPos.getY()) {
                    sendDisableMsg("moved vertically");
                    toggle();
                    return;
                }
            }
            case Up -> {
                if (mc.player.getBlockPos().getY() > startPos.getY()) {
                    sendDisableMsg("moved up");
                    toggle();
                    return;
                }
            }
            case Down -> {
                if (mc.player.getBlockPos().getY() < startPos.getY()) {
                    sendDisableMsg("moved down");
                    toggle();
                    return;
                }
            }
        }

        // Sneak Check
        if (toggleSneak.get()) {
            boolean isClicked = mc.options.sneakKey.isPressed();
            if (isClicked && !lastSneak) {
                sendDisableMsg("sneaked");
                toggle();
                return;
            }
            lastSneak = isClicked;
        }

        List<BlockPos> placements = check();
        if (placements == null) return;

        FindItemResult hotbar = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
        FindItemResult inventory = InvUtils.find(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
        Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;

        if ((hand != null || (switchMode.get() == SwitchMode.SilentBypass && inventory.slot() >= 0) || ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0)) && (!pauseEat.get() || !mc.player.isUsingItem()) && placesLeft > 0 && !placements.isEmpty()) {

            Map<PlaceData, BlockPos> toPlace = new HashMap<>();
            for (BlockPos placement : placements) {
                PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(placement) : SettingUtils.getPlaceDataOR(placement, placed::contains);
                if (toPlace.size() < placesLeft && data.valid()) {
                    toPlace.put(data, placement);
                }
            }
            toPlace = sort(toPlace);

            if (!toPlace.isEmpty()) {
                switchItems(hand, hotbar, inventory, toPlace);
            }
        }

    }

    private void switchItems(Hand hand, FindItemResult hotbar, FindItemResult inventory, Map<PlaceData, BlockPos> toPlace) {
        @SuppressWarnings("DataFlowIssue")
        int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : -1;

        switch (switchMode.get()) {
            case Silent, Normal -> obsidian = hotbar.count();
            case SilentBypass -> obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
        }

        if (obsidian >= 0) {
            boolean switched = false;
            int i = 0;
            placing = true;
            for (Map.Entry<PlaceData, BlockPos> entry : toPlace.entrySet()) {
                if (i == Math.min(obsidian, toPlace.size())) {
                    continue;
                }

                PlaceData placeData = entry.getKey();
                BlockPos pos = entry.getValue();
                if (placeData.valid()) {
                    boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(placeData.pos(), 1, RotationType.Placing);

                    if (!rotated) {
                        break;
                    }
                    if (!switched) {
                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent, Normal -> {
                                    InvUtils.swap(hotbar.slot(), true);
                                    switched = true;
                                }
                                case SilentBypass -> switched = BOInvUtils.invSwitch(inventory.slot());
                            }
                        }
                    }
                    place(placeData, pos, hand == null ? Hand.MAIN_HAND : hand);
                }
            }

            if (switched) {
                switch (switchMode.get()) {
                    case Silent -> InvUtils.swapBack();
                    case SilentBypass -> BOInvUtils.swapBack();
                }
            }
        }
    }

    private void place(PlaceData d, BlockPos ogPos, Hand hand) {
        timers.add(ogPos, delay.get());
        if (onlyConfirmed.get()) {
            placed.add(ogPos, 1);
        }

        placeTimer = 0;
        placesLeft--;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);

        assert mc.player != null;
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(new Vec3d(d.pos().getX() + 0.5, d.pos().getY() + 0.5, d.pos().getZ() + 0.5), d.dir(), d.pos(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.end(d.pos());
        }
    }

    private boolean isValid(ItemStack item) {
        return item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock());
    }

    private List<BlockPos> check() {
        if (mc.player == null && mc.world == null) {
            return null;
        }

        List<BlockPos> list = new LinkedList<>();
        List<Render> renders = new LinkedList<>();
        List<BlockPos> toAttack = new LinkedList<>();

        getBlocks(getSize())
            .stream()//maybe use a parallel stream here
            .filter(pos -> BlockUtils.replaceable(pos) && !placed.contains(pos))
            .forEach(position -> {
                if (!timers.contains(position)) {
                    PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(position) : SettingUtils.getPlaceDataOR(position, placed::contains);
                    if (data.valid()) {
                        if ((EntityUtils.intersectsWithEntity(WorldUtils.getBox(position), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) && (EntityUtils.intersectsWithEntity(WorldUtils.getBox(position), entity -> entity instanceof EndCrystalEntity))) {
                            toAttack.add(position);
                        } else {
                            list.add(position);
                        }
                    } else {
                        Direction best = null;
                        int value = -1;
                        double dist = Double.MAX_VALUE;
                        for (Direction dir : Direction.values()) {
                            if (!BlockUtils.replaceable(position.offset(dir))) {
                                continue;
                            }

                            PlaceData placeData = onlyConfirmed.get() ? SettingUtils.getPlaceData(position.offset(dir)) : SettingUtils.getPlaceDataOR(position.offset(dir), placed::contains);

                            if (!placeData.valid()) {
                                continue;
                            }

                            if (!EntityUtils.intersectsWithEntity(WorldUtils.getBox(position.offset(dir)), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                                double distance = DistanceUtils.distance(WorldUtils.getMiddle(position.offset(dir)), mc.player.getPos());
                                if (distance < dist || value <= 1) {
                                    dist = distance;
                                    best = dir;
                                    value = 2;
                                }
                            } else if (!EntityUtils.intersectsWithEntity(WorldUtils.getBox(position.offset(dir)), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
                                if (value > 1) {
                                    continue;
                                }

                                double distance = DistanceUtils.distance(WorldUtils.getMiddle(position.offset(dir)), mc.player.getPos());
                                if (distance < dist || value <= 0) {
                                    value = 1;
                                    best = dir;
                                    dist = distance;
                                }
                            }
                        }

                        if (best != null) {
                            if (!timers.contains(position.offset(best))) {
                                list.add(position.offset(best));
                            }
                            renders.add(new Render(position.offset(best), true));
                        }
                    }
                }
                renders.add(new Render(position, false));
            });

        attack = toAttack;
        render = renders;
        return list;
    }

    private int[] getSize() {
        if (mc.player == null || mc.world == null) {
            return new int[]{0, 0, 0, 0};
        }

        Vec3d offset = mc.player.getPos().add(-mc.player.getBlockX(), -mc.player.getBlockY(), -mc.player.getBlockZ());
        return new int[]{offset.x < 0.3 ? -1 : 0, offset.x > 0.7 ? 1 : 0, offset.z < 0.3 ? -1 : 0, offset.z > 0.7 ? 1 : 0};
    }

    private List<BlockPos> getBlocks(int[] size) {
        List<BlockPos> list = new ArrayList<>();
        if (mc.player != null && mc.world != null) {
            BlockPos pPos = mc.player.getBlockPos();
            for (int x = size[0] - 1; x <= size[1] + 1; x++) {
                for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                    boolean isX = x == size[0] - 1 || x == size[1] + 1;
                    boolean isZ = z == size[2] - 1 || z == size[3] + 1;
                    boolean ignore = (isX && !isZ ? (!BlockUtils.replaceable(pPos.add(DistanceUtils.closerToZero(x), 0, z)) || placed.contains(pPos.add(DistanceUtils.closerToZero(x), 0, z))) : !isX && isZ && (!BlockUtils.replaceable(pPos.add(x, 0, DistanceUtils.closerToZero(z)))) && !(x == 0 && z == 0) || placed.contains(pPos.add(x, 0, DistanceUtils.closerToZero(z))));
                    if (isX != isZ && !ignore) {
                        list.add(pPos.add(x, 0, z));
                    } else if (!isX && !isZ && floor.get() && BlockUtils.replaceable(pPos.add(x, 0, z)) && !placed.contains(pPos.add(x, 0, z))) {
                        list.add(pPos.add(x, -1, z));
                    }
                }
            }
        }
        return list;
    }

    // Very shitty sorting
    private Map<PlaceData, BlockPos> sort(Map<PlaceData, BlockPos> original) {
        Map<PlaceData, BlockPos> map = new HashMap<>();
        List<PlaceData> ignored = new ArrayList<>();
        double lowest;
        PlaceData lData;
        BlockPos lPos;
        for (int i = 0; i < original.size(); i++) {
            lowest = Double.MAX_VALUE;
            lData = null;
            lPos = null;

            for (Map.Entry<PlaceData, BlockPos> entry : original.entrySet()) {
                if (ignored.contains(entry.getKey())) {
                    continue;
                }
                double yaw = MathHelper.wrapDegrees(Rotations.getYaw(entry.getValue())) + 360;
                if (yaw < lowest) {
                    lowest = yaw;
                    lData = entry.getKey();
                    lPos = entry.getValue();
                }
            }
            ignored.add(lData);
            map.put(lData, lPos);
        }

        return map;
    }

    private record Render(BlockPos pos, boolean support) {}

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        SilentBypass
    }

    public enum ToggleYMode {
        Disabled,
        Up,
        Down,
        Full
    }
}
