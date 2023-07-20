package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.TimerList;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author KassuK
 * @author OLEPOSSU
 */

public class ScaffoldPlus extends BlackOutModule {
    public ScaffoldPlus() {
        super(BlackOut.BLACKOUT, "Scaffold+", "KasumsSoft blockwalk.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacing = settings.createGroup("Placing");
    private final SettingGroup sgRender = settings.createGroup("Render");

    //--------------------General--------------------//
    private final Setting<ScaffoldMode> scaffoldMode = sgGeneral.add(new EnumSetting.Builder<ScaffoldMode>()
        .name("Scaffold Mode")
        .description("Mode for scaffold.")
        .defaultValue(ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
        .name("Smart")
        .description("Only places on blocks that you can reach.")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> tower = sgGeneral.add(new BoolSetting.Builder()
        .name("Tower")
        .description("Flies up with blocks.")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> sSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("Stop Sprint")
        .description("Stops you from sprinting.")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> safeWalk = sgGeneral.add(new BoolSetting.Builder()
        .name("SafeWalk")
        .description("Should SafeWalk be used.")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> useTimer = sgGeneral.add(new BoolSetting.Builder()
        .name("Use timer")
        .description("Should we use timer.")
        .defaultValue(false)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .visible(useTimer::get)
        .name("Timer")
        .description("Sends more packets.")
        .defaultValue(1.088)
        .min(0)
        .sliderMax(10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal && useTimer.get())
        .build()
    );

    //--------------------Placing--------------------//
    private final Setting<SwitchMode> switchMode = sgPlacing.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description("Method of switching. Silent is the most reliable.")
        .defaultValue(SwitchMode.Silent)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<List<Block>> blocks = sgPlacing.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> placeDelay = sgPlacing.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.125)
        .range(0, 10)
        .sliderRange(0, 10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Integer> places = sgPlacing.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place.")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> cooldown = sgPlacing.add(new DoubleSetting.Builder()
        .name("Cooldown")
        .description("Delay between places at each spot.")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Integer> extrapolation = sgPlacing.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("Predicts movement.")
        .defaultValue(3)
        .range(1, 20)
        .sliderRange(0, 20)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when placing a block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(BlackOut.COLOR)
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final TimerList<BlockPos> timers = new TimerList<>();
    private Vec3d motion = null;
    private double placeTimer;
    private int placesLeft = 0;
    public static boolean shouldStopSprinting = false;
    private final List<Render> render = new ArrayList<>();
    private int jumpProgress = -1;

    private final double[] velocities = new double[]{0.42, 0.33319999999999994, 0.2468};

    @Override
    public void onDeactivate() {
        switch (scaffoldMode.get()) {
            case Normal -> {
                placeTimer = 0;
                placesLeft = places.get();
                Modules.get().get(Timer.class).setOverride(1);
                if (Modules.get().get(SafeWalk.class).isActive()) {
                    Modules.get().get(SafeWalk.class).toggle();
                }
            }
            case Legit -> mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        timers.update();

        if (scaffoldMode.get() == ScaffoldMode.Legit) {return;}

        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }

        render.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        render.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 500) / 500d;

            event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });
    }

    @EventHandler(priority = 10000)
    private void onTick(TickEvent.Pre event) {
        if (scaffoldMode.get() == ScaffoldMode.Legit) {
            mc.options.sneakKey.setPressed(mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() instanceof AirBlock);
        }
    }

    @EventHandler(priority = 10000)
    private void onMove(PlayerMoveEvent event) {
        shouldStopSprinting = false;
        if (scaffoldMode.get() == ScaffoldMode.Legit) {
            return;
        }

        if (mc.player == null || mc.world == null) {return;}

        FindItemResult hotbar = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
        FindItemResult inventory = InvUtils.find(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
        Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;

        if (hand != null || ((switchMode.get() == SwitchMode.PickSilent || switchMode.get() == SwitchMode.InvSwitch) && inventory.slot() >= 0) ||
            ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0)) {

            if (safeWalk.get() && !Modules.get().get(SafeWalk.class).isActive()) {
                Modules.get().get(SafeWalk.class).toggle();
            }

            motion = event.movement;

            yVel();

            if (sSprint.get()) {
                shouldStopSprinting = true;
                mc.player.setSprinting(false);
            }

            if (useTimer.get()) Modules.get().get(Timer.class).setOverride(timer.get());

            List<BlockPos> placements = getBlocks();

            if (!placements.isEmpty() && placesLeft > 0) {
                List<BlockPos> toPlace = new ArrayList<>();

                for (BlockPos placement : placements) {
                    if (toPlace.size() < placesLeft && canPlace(placement)) {
                        toPlace.add(placement);
                    }
                }

                if (!toPlace.isEmpty()) {
                    int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() :
                        hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : -1;


                    if (hand == null) {
                        switch (switchMode.get()) {
                            case Silent, Normal -> obsidian = hotbar.count();
                            case PickSilent, InvSwitch -> obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
                        }
                    }

                    if (obsidian >= 0) {
                        Block block = null;
                        if (hand == Hand.MAIN_HAND) {
                            block = ((BlockItem) Managers.HOLDING.getStack().getItem()).getBlock();
                        }
                        if (hand == Hand.OFF_HAND) {
                            block = ((BlockItem) mc.player.getOffHandStack().getItem()).getBlock();
                        } else {
                            switch (switchMode.get()) {
                                case Silent, Normal -> {
                                    obsidian = hotbar.count();
                                    InvUtils.swap(hotbar.slot(), true);
                                    block = ((BlockItem) mc.player.getInventory().getStack(hotbar.slot()).getItem()).getBlock();
                                }
                                case InvSwitch -> {
                                    obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                                    block = ((BlockItem) mc.player.getInventory().getStack(inventory.slot()).getItem()).getBlock();
                                }
                                case PickSilent -> {
                                    obsidian = BOInvUtils.pickSwitch(inventory.slot()) ? inventory.count() : -1;
                                    block = ((BlockItem) mc.player.getInventory().getStack(inventory.slot()).getItem()).getBlock();
                                }
                            }
                        }

                        for (int i = 0; i < Math.min(obsidian, toPlace.size()); i++) {
                            PlaceData placeData = SettingUtils.getPlaceData(toPlace.get(i));
                            if (placeData.valid()) {
                                boolean rotated = !SettingUtils.shouldRotate(RotationType.BlockPlace) || Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

                                if (!rotated) {
                                    break;
                                }
                                place(placeData, toPlace.get(i), hand == null ? Hand.MAIN_HAND : hand, block);
                            }
                        }

                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent -> InvUtils.swapBack();
                                case PickSilent -> BOInvUtils.pickSwapBack();
                                case InvSwitch -> BOInvUtils.swapBack();
                            }
                        }
                    }
                }
            }
        } else {
            if (safeWalk.get() && Modules.get().get(SafeWalk.class).isActive()) {
                Modules.get().get(SafeWalk.class).toggle();
            }
        }
    }

    void yVel() {
        if (!tower.get()) return;

        if (mc.options.jumpKey.isPressed() && mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) {
            if (mc.player.isOnGround() || jumpProgress == 3) {
                jumpProgress = 0;
            }

            if (jumpProgress > -1) {
                if (jumpProgress < 3) {
                    ((IVec3d) motion).setXZ(0, 0);
                    ((IVec3d) motion).setY(velocities[jumpProgress]);
                    ((IVec3d) mc.player.getVelocity()).setY(velocities[jumpProgress]);
                    jumpProgress++;
                }
            }
        } else {
            jumpProgress = -1;
        }
    }

    private boolean isValid(ItemStack item) {
        return item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock());
    }

    private boolean canPlace(BlockPos pos) {
        return SettingUtils.getPlaceData(pos).valid();
    }

    private List<BlockPos> getBlocks() {
        List<BlockPos> list = new ArrayList<>();

        Vec3d vec = mc.player.getPos();
        for (int i = 0; i < extrapolation.get() * 10; i++) {
            vec = vec.add(motion.x / 10, 0, motion.z / 10);

            if (smart.get() && inside(getBox(vec))) {
                break;
            } else {
                BlockPos pos = BlockPos.ofFloored(vec).down();

                if (!timers.contains(pos) && OLEPOSSUtils.replaceable(pos) && !list.contains(pos) &&
                    !mc.player.getBoundingBox().intersects(Box.from(new BlockBox(pos)))) {
                    list.add(pos);
                }
            }
        }
        return list;
    }

    private Box getBox(Vec3d vec) {
        Box box = mc.player.getBoundingBox();
        return new Box(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.y + (box.maxY - box.minY), vec.z + 0.3);
    }

    private boolean inside(Box bb) {
        return mc.world.getBlockCollisions(mc.player, bb).iterator().hasNext();
    }


    private void place(PlaceData d, BlockPos ogPos, Hand hand, Block block) {
        timers.add(ogPos, cooldown.get());
        render.add(new Render(ogPos, System.currentTimeMillis()));
        placesLeft--;

        placeBlock(hand, d.pos().toCenterPos(), d.dir(), d.pos());

        if (placeSwing.get()) clientSwing(placeHand.get(), hand);

        mc.world.setBlockState(ogPos, block.getDefaultState());

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }
    }

    public record Render(BlockPos pos, long time) {}

    public enum ScaffoldMode {
        Normal,
        Legit
    }

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSilent,
        InvSwitch
    }
}
