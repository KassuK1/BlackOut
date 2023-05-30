package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KassuK
 * @author OLEPOSSU
 * @author ccetl
 */

public class SurroundPlus extends BlackOutModule {
    public SurroundPlus() {
        super(BlackOut.BLACKOUT, "Surround+", "Places blocks around your legs to protect from explosions.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgToggle = settings.createGroup("Toggle");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onlyConfirmed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Confirmed")
        .description("Only places on blocks the server has confirmed to exist.")
        .defaultValue(false)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<List<Block>> sBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Secondary Blocks")
        .description("Blocks to use.")
        .defaultValue()
        .build()
    );
    private final Setting<Boolean> floor = sgGeneral.add(new BoolSetting.Builder()
        .name("Floor")
        .description("Places blocks under your feet.")
        .defaultValue(true)
        .build()
    );

    //--------------------Placing--------------------//
    private final Setting<SwitchMode> switchMode = sgPlacing.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0).range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgPlacing.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place.")
        .defaultValue(1).range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between placing at each spot.")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //--------------------Toggle--------------------//
    private final Setting<Boolean> toggleMove = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Move")
        .description("Toggles when you move horizontally.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ToggleYMode> toggleY = sgToggle.add(new EnumSetting.Builder<ToggleYMode>()
        .name("Toggle Y")
        .description("Toggles when you move vertically.")
        .defaultValue(ToggleYMode.Full)
        .build()
    );
    private final Setting<Boolean> toggleSneak = sgToggle.add(new BoolSetting.Builder()
        .name("Toggle Sneak")
        .description("Toggles when you sneak.")
        .defaultValue(false)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Boolean> renderPlaced = sgRender.add(new BoolSetting.Builder()
        .name("Render Placed")
        .description("Renders blocks after they have been placed.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ShapeMode> placedShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Placed Shape Mode")
        .description("Which parts of render boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(renderPlaced::get)
        .build()
    );
    private final Setting<SettingColor> placedLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Placed Line Color")
        .description("Color of the outlines")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(renderPlaced::get)
        .build()
    );
    private final Setting<SettingColor> placedSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Placed Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .visible(renderPlaced::get)
        .build()
    );
    private final Setting<ShapeMode> supportShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Support Shape Mode")
        .description("Which parts of render boxes should be rendered for support blocks.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> supportLineColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Line Color")
        .description("Color of support block outlines")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> supportSideColor = sgRender.add(new ColorSetting.Builder()
        .name("Support Side Color")
        .description("Color of support block sides.")
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
    public static long attacked = 0;

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
    private void onTick(TickEvent.Pre event) {
        attacked--;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }
        render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item.pos),
            switch (item.type) {
                case Normal -> sideColor.get();
                case Support -> supportSideColor.get();
                case Placed -> placedSideColor.get();
            }, switch (item.type) {
                case Normal -> lineColor.get();
                case Support -> supportLineColor.get();
                case Placed -> placedLineColor.get();
            }, switch (item.type) {
                case Normal -> shapeMode.get();
                case Support -> supportShapeMode.get();
                case Placed -> placedShapeMode.get();
            }, 0));
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

        FindItemResult hotbar = InvUtils.findInHotbar(item -> validItem(item.getItem(), shouldSecondary(false)));
        FindItemResult inventory = InvUtils.find(item -> validItem(item.getItem(), shouldSecondary(true)));

        Hand hand = validItem(Managers.HOLDING.getStack().getItem(), shouldSecondary(false)) ? Hand.MAIN_HAND : validItem(mc.player.getOffHandStack().getItem(), false) ? Hand.OFF_HAND : null;

        if ((hand != null || ((switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSwitch) && inventory.slot() >= 0) || ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0)) && (!pauseEat.get() || !mc.player.isUsingItem()) && placesLeft > 0 && !placements.isEmpty()) {

            Map<PlaceData, BlockPos> toPlace = new HashMap<>();
            for (BlockPos placement : placements) {
                PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(placement) : SettingUtils.getPlaceDataOR(placement, placed::contains);
                if (toPlace.size() < placesLeft && data.valid()) {
                    toPlace.put(data, placement);
                }
            }
            toPlace = sort(toPlace);

            if (!toPlace.isEmpty()) {
                placeBlocks(hand, hotbar, inventory, toPlace);
            }
        }

    }

    private boolean validItem(Item item, boolean secondary) {
        if (!(item instanceof BlockItem block)) {return false;}

        if (secondary) {
            return sBlocks.get().contains(block.getBlock());
        }
        return blocks.get().contains(block.getBlock());
    }

    private boolean shouldSecondary(boolean inv) {
        for (Block b : blocks.get()) {
            if (inv) {
                if (InvUtils.find(b.asItem()).found()) {
                    return false;
                }
                continue;
            }
            if (InvUtils.findInHotbar(b.asItem()).found()) {
                return false;
            }
        }
        return true;
    }

    private void placeBlocks(Hand hand, FindItemResult hotbar, FindItemResult inventory, Map<PlaceData, BlockPos> toPlace) {
        @SuppressWarnings("DataFlowIssue")
        int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() : hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : -1;

        switch (switchMode.get()) {
            case Silent, Normal -> obsidian = hotbar.count();
            case PickSilent, InvSwitch -> obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
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
                                case PickSilent -> switched = BOInvUtils.pickSwitch(inventory.slot());
                                case InvSwitch -> switched = BOInvUtils.invSwitch(inventory.slot());
                            }
                        }
                    }
                    place(placeData, pos, hand == null ? Hand.MAIN_HAND : hand);
                }
            }

            if (switched) {
                switch (switchMode.get()) {
                    case Silent -> InvUtils.swapBack();
                    case PickSilent -> BOInvUtils.pickSwapBack();
                    case InvSwitch -> BOInvUtils.swapBack();
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

    private List<BlockPos> check() {
        if (mc.player == null && mc.world == null) {
            return null;
        }

        List<BlockPos> list = new ArrayList<>();
        List<Render> renders = new ArrayList<>();
        List<BlockPos> toAttack = new ArrayList<>();

        getBlocks(getSize())
            .forEach(position -> {
                PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(position) : SettingUtils.getPlaceDataOR(position, placed::contains);

                if (data.valid()) {
                    if (!timers.contains(position)
                        && !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> isAlive() && !entity.isSpectator() && entity.getType() != EntityType.ITEM)
                        && OLEPOSSUtils.replaceable(position)) {
                        list.add(position);
                    }
                    if (EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> isAlive() && entity instanceof EndCrystalEntity) && !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> !entity.isSpectator() && !(entity instanceof EndCrystalEntity || entity instanceof ItemEntity))) {
                        toAttack.add(position);
                    }
                } else if (OLEPOSSUtils.replaceable(position)) {
                    Direction support = getSupport(position);

                    if (support != null) {
                        if (!timers.contains(position.offset(support))) {
                            list.add(position.offset(support));
                        }
                        addRender(renders, new Render(position.offset(support), RenderType.Support));
                    }
                }
                if (!renderPlaced.get() && !OLEPOSSUtils.replaceable(position) || mc.world.getBlockState(position).getBlock() == Blocks.BEDROCK) {return;}

                addRender(renders, new Render(position, OLEPOSSUtils.replaceable(position) ? RenderType.Normal : RenderType.Placed));
            });

        attack = toAttack;
        render = renders;
        return list;
    }

    private Direction getSupport(BlockPos position) {
        Direction cDir = null;
        double cDist = 1000;
        int value = -1;

        for (Direction dir : Direction.values()) {
            if (!OLEPOSSUtils.replaceable(position.offset(dir))) {continue;}

            PlaceData data = onlyConfirmed.get() ? SettingUtils.getPlaceData(position.offset(dir)) : SettingUtils.getPlaceDataOR(position.offset(dir), placed::contains);

            if (!data.valid() || !SettingUtils.inPlaceRange(data.pos())) {continue;}

            if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position.offset(dir)), entity -> isAlive() && !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                double dist = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(position.offset(dir)));

                if (dist < cDist || value < 2) {
                    value = 2;
                    cDir = dir;
                    cDist = dist;
                }
            }

            if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position.offset(dir)), entity -> isAlive() && !entity.isSpectator() && entity.getType() != EntityType.ITEM && entity.getType() != EntityType.END_CRYSTAL)) {
                double dist = OLEPOSSUtils.distance(mc.player.getEyePos(), OLEPOSSUtils.getMiddle(position.offset(dir)));

                if (dist < cDist || value < 1) {
                    value = 1;
                    cDir = dir;
                    cDist = dist;
                }
            }

        }
        return cDir;
    }

    private void addRender(List<Render> renders, Render render) {
        for (Render r : renders) {
            if (r.pos != render.pos) {continue;}

            if (render.type.value < r.type.value) {return;}

            if (r.type.value < render.type.value) {
                renders.remove(r);
                break;
            }
        }
        renders.add(render);
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

                    boolean ignore = (isX && !isZ ? (!OLEPOSSUtils.replaceable(pPos.add(OLEPOSSUtils.closerToZero(x), 0, z)) || placed.contains(pPos.add(OLEPOSSUtils.closerToZero(x), 0, z))) : !isX && isZ && (!OLEPOSSUtils.replaceable(pPos.add(x, 0, OLEPOSSUtils.closerToZero(z)))) && !(x == 0 && z == 0) || placed.contains(pPos.add(x, 0, OLEPOSSUtils.closerToZero(z))));

                    if (isX != isZ && !ignore) {
                        list.add(pPos.add(x, 0, z));
                    } else if (!isX && !isZ && floor.get() && OLEPOSSUtils.replaceable(pPos.add(x, 0, z)) && !placed.contains(pPos.add(x, 0, z))) {
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
        double lowest;
        PlaceData lData;
        BlockPos lPos;
        for (int i = 0; i < original.size(); i++) {
            lowest = Double.MAX_VALUE;
            lData = null;
            lPos = null;

            for (Map.Entry<PlaceData, BlockPos> entry : original.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    continue;
                }
                double yaw = MathHelper.wrapDegrees(Rotations.getYaw(entry.getValue()));
                if (yaw < lowest) {
                    lowest = yaw;
                    lData = entry.getKey();
                    lPos = entry.getValue();
                }
            }
            map.put(lData, lPos);
        }

        return map;
    }

    private boolean isAlive() {
        return attacked < 0;
    }

    private record Render(BlockPos pos, RenderType type) {}

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum ToggleYMode {
        Disabled,
        Up,
        Down,
        Full
    }
    public enum RenderType {
        Normal(3),
        Support(1),
        Placed(2);

        private final int value;
        RenderType(int value) {
            this.value = value;
        }
    }
}
