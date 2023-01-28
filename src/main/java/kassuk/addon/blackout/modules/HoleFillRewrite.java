package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.enums.RotationType;
import kassuk.addon.blackout.enums.SwingState;
import kassuk.addon.blackout.enums.SwingType;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.timers.BlockTimerList;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
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
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class HoleFillRewrite extends Module {
    public HoleFillRewrite() {
        super(BlackOut.BLACKOUT, "Hole Fill+", "Automatically is a cunt to your enemies");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> silent = sgGeneral.add(new BoolSetting.Builder()
        .name("Silent")
        .description("Places even when you arent holding")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> efficient = sgGeneral.add(new BoolSetting.Builder()
        .name("Efficient")
        .description("Only places if the hole is closer to target")
        .defaultValue(true)
        .build()
    );
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Blocks to use.")
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
        .description("Doesn't place if enemy is sitting in a hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> holeDepth = sgGeneral.add(new IntSetting.Builder()
        .name("Hole Depth")
        .description(".")
        .defaultValue(3)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> placeDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Place Delay")
        .description(".")
        .defaultValue(0)
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
        .description(".")
        .defaultValue(0.3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<FaceMode> facing = sgGeneral.add(new EnumSetting.Builder<FaceMode>()
        .description("Facing")
        .defaultValue(FaceMode.Range)
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
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description(".")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> renderTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Time")
        .description("How long the box should remain in full alpha.")
        .defaultValue(0.3)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> fadeTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Fade Time")
        .description("How long the fading should take.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    List<BlockPos> holes = new ArrayList<>();
    BlockTimerList timers = new BlockTimerList();
    double placeTimer = 0;
    Map<BlockPos, Double[]> toRender = new HashMap<>();
    Direction[] directions = new Direction[]{
        Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH
    };

    public enum FaceMode {
        Normal,
        Strict,
        Range
    }

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
        List<BlockPos> toPlace = getValid(holes);
        FindItemResult result = InvUtils.find(itemStack -> itemStack.getItem() instanceof BlockItem && blocks.get().contains(((BlockItem) itemStack.getItem()).getBlock()));
        int[] obsidian = new int[]{result.slot(), result.count()};
        if (!toPlace.isEmpty() && obsidian[1] > 0 && (!pauseEat.get() || !mc.player.isUsingItem()) && placeTimer >= placeDelay.get()) {
            if (Managers.HOLDING.isHolding(Items.OBSIDIAN) || silent.get()) {
                boolean swapped = false;
                if (!Managers.HOLDING.isHolding(Items.OBSIDIAN)) {
                    InvUtils.swap(obsidian[0], true);
                    swapped = true;
                }
                for (int i = 0; i < Math.min(Math.min(toPlace.size(), places.get()), obsidian[1]); i++) {
                    placeTimer = 0;
                    place(toPlace.get(i));
                }
                if (swapped) {InvUtils.swapBack();}
            }
        }
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

                    if (OLEPOSSUtils.isHole(pos, mc.world, holeDepth.get()) && !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(pos), entity -> !entity.isSpectator() && !(entity instanceof ItemEntity))) {
                        double closest = closestDist(pos);
                        Direction dir = closestDir(pos);
                        if (closest >= 0 && closest <= holeRange.get() && (!efficient.get() || OLEPOSSUtils.distance(mc.player.getPos(), OLEPOSSUtils.getMiddle(pos)) > closest)) {
                            if (SettingUtils.inPlaceRange(pos.offset(dir))) {
                                holes.add(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    public Direction closestDir(BlockPos pos) {
        double closest = -1;
        Direction cDir = null;
        switch (facing.get()) {
            case Normal -> {
                return Direction.UP;
            }
            case Strict -> {
                for (Direction dir : directions) {
                    double dist = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos.offset(dir)), mc.player.getEyePos());
                    if ((closest == -1 || dist < closest) && OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite())) {
                        cDir = dir;
                        closest = dist;
                    }
                }
                return cDir;
            }
            case Range -> {
                for (Direction dir : directions) {
                    double dist = OLEPOSSUtils.distance(OLEPOSSUtils.getMiddle(pos.offset(dir)), mc.player.getEyePos());
                    if (closest == -1 || dist < closest) {
                        cDir = dir;
                        closest = dist;
                    }
                }
                return cDir;
            }
        }
        return null;
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

    void place(BlockPos pos) {
        Direction dir = closestDir(pos);
        if (dir == null) {return;}

        timers.add(pos, delay.get());

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.start(pos.offset(dir), 2);
        }

        SettingUtils.swing(SwingState.Pre, SwingType.Placing);

        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(OLEPOSSUtils.getMiddle(pos.offset(dir)), dir.getOpposite(), pos.offset(dir), false), 0));

        SettingUtils.swing(SwingState.Post, SwingType.Placing);

        if (SettingUtils.shouldRotate(RotationType.Placing)) {
            Managers.ROTATION.start(pos.offset(dir), 2);
        }

        if (!toRender.containsKey(pos)) {
            toRender.put(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
        } else {
            toRender.replace(pos, new Double[]{fadeTime.get() + renderTime.get(), fadeTime.get()});
        }
    }
}
