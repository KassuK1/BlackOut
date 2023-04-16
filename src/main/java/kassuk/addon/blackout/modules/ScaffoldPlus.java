package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.BOInvUtils;
import kassuk.addon.blackout.utils.PlaceData;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/*
Made by KassuK
Updated by OLEPOSSU
*/

public class ScaffoldPlus extends BlackOutModule {
    public ScaffoldPlus() {
        super(BlackOut.BLACKOUT, "Scaffold+", "KasumsSoft blockwalk");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<ScaffoldMode> scaffoldMode = sgGeneral.add(new EnumSetting.Builder<ScaffoldMode>()
        .name("Scaffold Mode")
        .description(".")
        .defaultValue(ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> onlyConfirmed = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Confirmed")
        .description("Only places on blocks the server has confirmed to exist")
        .defaultValue(true)
        .build()
    );
    // Normal
    private final Setting<Boolean> sSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("Stop Sprint")
        .description("Stops you from sprinting")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> safeWalk = sgGeneral.add(new BoolSetting.Builder()
        .name("SafeWalk")
        .description("Should SafeWalk be used")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Boolean> useTimer = sgGeneral.add(new BoolSetting.Builder()
        .name("Use timer")
        .description("Should we use timer")
        .defaultValue(true)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .visible(useTimer::get)
        .name("Timer")
        .description("Sends more packets")
        .defaultValue(1.088)
        .min(0)
        .sliderMax(10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.SilentBypass)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.125)
        .range(0, 10)
        .sliderRange(0, 10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Integer> places = sgGeneral.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at each spot.")
        .defaultValue(0.3)
        .range(0, 5)
        .sliderRange(0, 5)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    private final Setting<Integer> extrapolation = sgGeneral.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("Predicts movement.")
        .defaultValue(3)
        .range(0, 20)
        .sliderRange(0, 20)
        .visible(() -> scaffoldMode.get() == ScaffoldMode.Normal)
        .build()
    );
    public enum ScaffoldMode {
        Normal,
        Legit
    }
    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        SilentBypass
    }
    BlockTimerList timers = new BlockTimerList();
    BlockTimerList placed = new BlockTimerList();
    Vec3d motion = null;
    double placeTimer;
    int placesLeft = 0;

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
            case Legit -> {
                mc.options.sneakKey.setPressed(false);
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (scaffoldMode.get() == ScaffoldMode.Legit) {return;}

        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        if (placeTimer >= placeDelay.get()) {
            placesLeft = places.get();
            placeTimer = 0;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (scaffoldMode.get() == ScaffoldMode.Legit) {
            mc.options.sneakKey.setPressed(mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock().equals(Blocks.AIR));
            return;
        }

        if (mc.player != null && mc.world != null) {

            FindItemResult hotbar = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            FindItemResult inventory = InvUtils.find(item -> item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock()));
            Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;

            if (hand != null || (switchMode.get() == SwitchMode.SilentBypass && inventory.slot() >= 0) ||
                ((switchMode.get() == SwitchMode.Silent || switchMode.get() == SwitchMode.Normal) && hotbar.slot() >= 0)) {

                if (safeWalk.get() && !Modules.get().get(SafeWalk.class).isActive()) {
                    Modules.get().get(SafeWalk.class).toggle();

                }

                motion = event.movement;
                if (sSprint.get()) mc.player.setSprinting(false);
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
                                case Silent, Normal -> {
                                    obsidian = hotbar.count();
                                }
                                case SilentBypass -> {
                                    obsidian = inventory.slot() >= 0 ? inventory.count() : -1;
                                }
                            }
                        }

                        if (obsidian >= 0) {
                            if (hand == null) {
                                switch (switchMode.get()) {
                                    case Silent, Normal -> {
                                        obsidian = hotbar.count();
                                        InvUtils.swap(hotbar.slot(), true);
                                    }
                                    case SilentBypass -> {
                                        obsidian = BOInvUtils.invSwitch(inventory.slot()) ? inventory.count() : -1;
                                    }
                                }
                            }

                            for (int i = 0; i < Math.min(obsidian, toPlace.size()); i++) {
                                PlaceData placeData = onlyConfirmed.get() ? SettingUtils.getPlaceData(toPlace.get(i)) : SettingUtils.getPlaceDataOR(toPlace.get(i), pos -> placed.contains(pos));
                                if (placeData.valid()) {
                                    boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(placeData.pos(), 1, RotationType.Placing);

                                    if (!rotated) {
                                        break;
                                    }
                                    place(placeData, toPlace.get(i), hand == null ? Hand.MAIN_HAND : hand);
                                }
                            }

                            if (hand == null) {
                                switch (switchMode.get()) {
                                    case Silent -> {
                                        InvUtils.swapBack();
                                    }
                                    case SilentBypass -> {
                                        BOInvUtils.swapBack();
                                    }
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
    }

    boolean isValid(ItemStack item) {
        return item.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) item.getItem()).getBlock());
    }

    boolean canPlace(BlockPos pos) {
        return onlyConfirmed.get() ? SettingUtils.getPlaceData(pos).valid() : SettingUtils.getPlaceDataOR(pos, position -> placed.contains(position)).valid();
    }

    List<BlockPos> getBlocks() {
        List<BlockPos> list = new ArrayList<>();
        double x = motion.x;
        double z = motion.z;
        Vec3d vec = mc.player.getPos();
        for (int i = 0; i < extrapolation.get(); i++) {
            vec = vec.add(x, 0, z);
            if (inside(getBox(vec))) {
                break;
            } else {
                addBlocks(list, vec);
            }
        }
        return list;
    }

    void addBlocks(List<BlockPos> list, Vec3d vec) {
        BlockPos pos = new BlockPos((int) vec.x, (int) vec.y, (int) vec.z).down();
        if (!timers.contains(pos) && air(pos) && !list.contains(pos)) {
            list.add(pos);
        }
    }

    Box getBox(Vec3d vec) {
        Box box = mc.player.getBoundingBox();
        return new Box(vec.x - 0.3, vec.y, vec.z - 0.3, vec.x + 0.3, vec.y + (box.maxY - box.minY), vec.z + 0.3);
    }

    boolean inside(Box bb) {
        return mc.world.getBlockCollisions(mc.player, bb).iterator().hasNext();
    }

    boolean air(BlockPos pos) {return mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR);}

    void place(PlaceData d, BlockPos ogPos, Hand hand) {
        timers.add(ogPos, delay.get());
        placed.add(ogPos, 1);
        placesLeft--;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
            new BlockHitResult(new Vec3d(d.pos().getX() + 0.5, d.pos().getY() + 0.5, d.pos().getZ() + 0.5),
                d.dir(), d.pos(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.end(d.pos());
        }
    }
}
