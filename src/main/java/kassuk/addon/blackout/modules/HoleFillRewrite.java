package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.HoleType;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.*;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class HoleFillRewrite extends BlackOutModule {
    public HoleFillRewrite() {
        super(BlackOut.BLACKOUT, "Hole Fill+", "Automatically is a cunt to your enemies");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgHole = settings.createGroup("Hole");

    //   General Page
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch Mode")
        .description(".")
        .defaultValue(SwitchMode.SilentBypass)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Which blocks to use.")
        .defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> efficient = sgGeneral.add(new BoolSetting.Builder()
        .name("Efficient")
        .description("Only places if the hole is closer to target")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> above = sgGeneral.add(new BoolSetting.Builder()
        .name("Above")
        .description("Only places if target is above the hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> iHole = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Hole")
        .description("Doesn't place if enemy is in a hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> holeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Hole Range")
        .description("Places when enemy is close enough to target hole")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description("Delay between places.")
        .defaultValue(0.125)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Integer> places = sgGeneral.add(new IntSetting.Builder()
        .name("Places")
        .description("Blocks placed per place")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between places at single spot.")
        .defaultValue(1)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    //   Hole Page
    private final Setting<Boolean> single = sgHole.add(new BoolSetting.Builder()
        .name("Single")
        .description("Fills 1x1 holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> doubleHole = sgHole.add(new BoolSetting.Builder()
        .name("Double")
        .description("Fills 2x1 block holes")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> quad = sgHole.add(new BoolSetting.Builder()
        .name("Quad")
        .description("Fills 2x2 block holes")
        .defaultValue(true)
        .build()
    );

    //   Render Page
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    public enum SwitchMode {
        Disabled,
        Silent,
        SilentBypass
    }
    List<BlockPos> holes = new ArrayList<>();
    BlockTimerList timers = new BlockTimerList();
    double placeTimer = 0;
    Map<BlockPos, Double[]> toRender = new HashMap<>();

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        double d = event.frameTime;
        if (mc.player != null && mc.world != null) {
            placeTimer = Math.min(placeTimer + event.frameTime, placeDelay.get());
            update();

            List<BlockPos> toRemove = new ArrayList<>();
            for (Map.Entry<BlockPos, Double[]> entry : toRender.entrySet()) {
                BlockPos pos = entry.getKey();
                Double[] alpha = entry.getValue();
                if (alpha[0] <= d) {
                    toRemove.add(pos);
                } else {
                    event.renderer.box(OLEPOSSUtils.getBox(pos),
                        new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, alpha[0] / alpha[1]))),
                        new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, alpha[0] / alpha[1]))), shapeMode.get(), 0);
                    entry.setValue(new Double[]{alpha[0] - d, alpha[1]});
                }
            }
            toRemove.forEach(toRender::remove);
        }
    }

    void update() {
        updateHoles(Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()) + 1);
        List<BlockPos> placements = getValid(holes);

        FindItemResult result = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) itemStack.getItem()).getBlock()));
        FindItemResult invResult = InvUtils.find(itemStack -> itemStack.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) itemStack.getItem()).getBlock()));
        Hand hand = isValid(Managers.HOLDING.getStack()) ? Hand.MAIN_HAND : isValid(mc.player.getOffHandStack()) ? Hand.OFF_HAND : null;

        if (!placements.isEmpty() && (!pauseEat.get() || !mc.player.isUsingItem()) && placeTimer >= placeDelay.get()) {
            if (hand != null || (switchMode.get() == SwitchMode.Silent && result.slot() >= 0) || (switchMode.get() == SwitchMode.SilentBypass && invResult.slot() >= 0)) {

                List<BlockPos> toPlace = new ArrayList<>();
                for (BlockPos pos : placements) {
                    if (toPlace.size() < places.get() && canPlace(pos)) {
                        toPlace.add(pos);
                    }
                }

                if (!toPlace.isEmpty()) {
                    int obsidian = hand == Hand.MAIN_HAND ? Managers.HOLDING.getStack().getCount() :
                        hand == Hand.OFF_HAND ? mc.player.getOffHandStack().getCount() : -1;

                    if (hand == null) {
                        switch (switchMode.get()) {
                            case Silent -> {
                                obsidian = result.count();
                            }
                            case SilentBypass -> {
                                obsidian = invResult.slot() >= 0 ? invResult.count() : -1;
                            }
                        }
                    }

                    if (obsidian >= 0) {
                        if (hand == null) {
                            switch (switchMode.get()) {
                                case Silent -> {
                                    obsidian = result.count();
                                    InvUtils.swap(result.slot(), true);
                                }
                                case SilentBypass -> {
                                    obsidian = BOInvUtils.invSwitch(invResult.slot()) ? invResult.count() : -1;
                                }
                            }
                        }

                        placeTimer = 0;

                        for (int i = 0; i < Math.min(obsidian, toPlace.size()); i++) {
                            PlaceData placeData = SettingUtils.getPlaceData(toPlace.get(i));
                            if (placeData.valid()) {
                                boolean rotated = !SettingUtils.shouldRotate(RotationType.Placing) || Managers.ROTATION.start(placeData.pos(), priority, RotationType.Placing);

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
        }
    }

    boolean isValid(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) itemStack.getItem()).getBlock());
    }

    List<BlockPos> getValid(List<BlockPos> positions) {
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (!timers.contains(pos)) {
                list.add(pos);
            }
        }
        return list;
    }

    void updateHoles(double range) {
        holes = new ArrayList<>();
        for(int x = (int) -Math.ceil(range); x <= Math.ceil(range); x++) {
            for(int y = (int) -Math.ceil(range); y <= Math.ceil(range); y++) {
                for(int z = (int) -Math.ceil(range); z <= Math.ceil(range); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);

                    Hole h = HoleUtils.getHole(pos, single.get(), doubleHole.get(), quad.get(), 3);

                    if (h.type != HoleType.NotHole) {
                        for (BlockPos p : h.positions()) {
                            if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(p), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                                double closest = closestDist(p);

                                PlaceData d = SettingUtils.getPlaceData(p);
                                if (d.valid() && closest >= 0 && closest <= holeRange.get() && (!efficient.get() || OLEPOSSUtils.distance(mc.player.getPos(), OLEPOSSUtils.getMiddle(p)) > closest)) {
                                    if (SettingUtils.inPlaceRange(d.pos())) {
                                        holes.add(p);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    double closestDist(BlockPos pos) {
        double closest = -1;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            double dist = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos), pl.getPos());

            if (/* In hole check */ (!iHole.get() || !inHole(pl)) &&
                /* Above Check */ (!above.get() || pl.getY() > pos.getY()) &&
                pl != mc.player && !Friends.get().isFriend(pl) && (closest < 0 || dist < closest)) {
                closest = dist;
            }
        }
        return closest;
    }

    boolean inHole(PlayerEntity pl) {
        for (Direction dir : OLEPOSSUtils.horizontals) {
            if (mc.world.getBlockState(pl.getBlockPos().offset(dir)).getBlock().equals(Blocks.AIR)) {
                return false;
            }
        }
        return true;
    }

    boolean canPlace(BlockPos pos) {
        return SettingUtils.getPlaceData(pos).valid();
    }

    void place(PlaceData d, BlockPos ogPos, Hand hand) {
        timers.add(ogPos, delay.get());

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
            new BlockHitResult(new Vec3d(d.pos().getX() + 0.5, d.pos().getY() + 0.5, d.pos().getZ() + 0.5),
                d.dir(), d.pos(), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.end(d.pos());
        }



        if (!toRender.containsKey(ogPos)) {
            toRender.put(ogPos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
        } else {
            toRender.replace(ogPos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
        }
    }
}
