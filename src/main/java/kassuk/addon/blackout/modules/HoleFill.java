package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
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
Made by OLEPOSSU / Raksamies
*/

public class HoleFill extends Module {
    public HoleFill() {
        super(BlackOut.BLACKOUT, "Hole Filler+", "Automatically is a cunt to your enemies");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing")
        .defaultValue(true)
        .build()
    );
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
    private final Setting<Double> renderTicks = sgGeneral.add(new DoubleSetting.Builder()
        .name("Render Ticks")
        .description(".")
        .defaultValue(20)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    List<BlockPos> holes = new ArrayList<>();
    List<Render> toRender = new ArrayList<>();
    BlockTimerList timers = new BlockTimerList();
    double placeTimer = 0;
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
        if (mc.player != null && mc.world != null) {
            placeTimer = Math.min(placeTimer + event.frameTime, placeDelay.get());
            update();

            List<Render> toRemove2 = new ArrayList<>();
            if (!toRender.isEmpty()) {
                toRender.forEach(item -> {
                    if (item.isValid()) {
                        item.render(event);
                        item.update(event.frameTime * 20 / renderTicks.get());
                    } else {
                        toRemove2.add(item);
                    }
                });
            }
            toRemove2.forEach(toRender::remove);
        }
    }

    void update() {
        updateHoles(SettingUtils.getPlaceRange() + 1);
        List<BlockPos> toPlace = getValid(holes);
        int[] obsidian = findBlock(Items.OBSIDIAN);
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
        for(int x1 = (int) -Math.ceil(range); x1 <= Math.ceil(range); x1++) {
            for(int y1 = (int) -Math.ceil(range); y1 <= Math.ceil(range); y1++) {
                for(int z1 = (int) -Math.ceil(range); z1 <= Math.ceil(range); z1++) {
                    int x = x1 + mc.player.getBlockPos().getX();
                    int y = y1 + mc.player.getBlockPos().getY();
                    int z = z1 + mc.player.getBlockPos().getZ();
                    if (isHole(x, y, z) && getDistance(new BlockPos(x, y, z)) < range &&
                    (!efficient.get() || (closestDist(new BlockPos(x, y, z)) <
                        OLEPOSSUtils.distance(new Vec3d(x + 0.5, y + 0.5, z + 0.5), mc.player.getPos()) && (!above.get() || mc.player.getY() >= y)))) {
                        holes.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    public double getDistance(BlockPos pos) {
        Vec3d vec = OLEPOSSUtils.getMiddle(pos.offset(closestDir(pos)));
        return OLEPOSSUtils.distance(vec, mc.player.getEyePos());
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
                    if ((closest == -1 || dist < closest) && OLEPOSSUtils.strictDir(pos, dir)) {
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
            if (pl != mc.player && !Friends.get().isFriend(pl) && (closest < 0 || dist < closest) && inHoleCheck(pl)) {
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

    boolean isHole(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) ||
            !mc.world.getBlockState(pos.up()).getBlock().equals(Blocks.AIR)||
            !mc.world.getBlockState(pos.up(2)).getBlock().equals(Blocks.AIR) ||
            mc.world.getBlockState(pos.down()).getBlock().equals(Blocks.AIR) ||
            EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                entity.getBlockPos().equals(pos.up()) && entity.getType() != EntityType.ITEM))) {return false;}

        for (Direction dir : OLEPOSSUtils.horizontals) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.AIR)) {return false;}
        }
        if (!inRange(new BlockPos(x, y, z))) {return false;}
        for (int i = 1; i <= (int) Math.ceil(holeRange.get()); i++) {
            for (Direction dir : OLEPOSSUtils.horizontals) {
                if (isHoleInWall(pos.offset(dir).up(i), dir)) {return true;}
            }
        }
        return false;
    }

    boolean inRange(BlockPos pos) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl) && inHoleCheck(pl) && aboveCheck(pl, pos.getY() + 1)) {
                if (SettingUtils.inPlaceRange(pos)) {return true;}
            }
        }
        return false;
    }

    boolean inHoleCheck(PlayerEntity pl) {return !iHole.get() || !inHole(pl);}
    boolean aboveCheck(PlayerEntity pl, double y) {return !above.get() || pl.getY() >= y;}

    boolean isHoleInWall(BlockPos pos, Direction dir) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR && (
            (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR ||
                mc.world.getBlockState(pos.offset(dir.getOpposite())).getBlock() == Blocks.AIR) ||
            mc.world.getBlockState(pos.down()).getBlock() == Blocks.AIR);
    }

    void place(BlockPos pos) {
        timers.add(pos, delay.get());
        Direction dir = closestDir(pos);
        if (dir != null) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(OLEPOSSUtils.getMiddle(pos.offset(dir)), dir.getOpposite(), pos.offset(dir), false), 0));
        }
        if (swing.get()) {mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));}
        toRender.add(new Render(pos));
    }

    int[] findBlock(Item item) {
        int num = 0;
        int slot = 0;
        if (mc.player != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getCount() > num && stack.getItem().equals(item)) {
                    num = stack.getCount();
                    slot = i;
                }
            }
        }
        return new int[] {slot, num};
    }

    class Render {
        public final BlockPos pos;
        public float time;

        public Render(BlockPos pos) {
            this.pos = pos;
            this.time = 1;
        }
        public void update(double delta) {
            time -= delta;
        }
        public boolean isValid() {
            return time >= 0;
        }
        public void render(Render3DEvent event) {
            float progress = time / 2;
            float alpha = (float) Math.max(0, time - 0.5) * 2;
            Vec3d v = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            Box toRender = new Box(v.x - progress, v.y - progress, v.z - progress, v.x + progress, v.y + progress, v.z + progress);
            event.renderer.box(toRender,
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a / 10f * alpha)),
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.floor(color.get().a * alpha)), ShapeMode.Both, 0);
        }
    }
}
