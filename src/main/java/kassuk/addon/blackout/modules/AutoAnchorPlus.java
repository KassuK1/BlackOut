package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.utils.BODamageUtils;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoAnchorPlus extends Module {
    public AutoAnchorPlus() {super(BlackOut.BLACKOUT, "Auto Anchor+", ".");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtrapolation = settings.getDefaultGroup();

    //  General Page

    private final Setting<Double> placeSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("How many times should the module place per second.")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Double> minDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("Min Damage")
        .description(".")
        .defaultValue(10)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> hold = sgGeneral.add(new BoolSetting.Builder()
        .name("Hold")
        .description("Instantly places after exploding.")
        .defaultValue(true)
        .build()
    );

    public long nextPlace = -1;
    public BlockPos placePos;
    public BlockPos lastPos;
    public Direction[] placeDir;
    boolean placed = false;
    int[] slots = null;
    List<LivingEntity> targets = new ArrayList<>();

    @Override
    public void onActivate() {
        super.onActivate();
        nextPlace = -1;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRender(TickEvent.Pre event) {
        targets = findTargets();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRender(Render3DEvent event) {
        if (!targets.isEmpty()) {
            placePos = getPos();
            placeDir = SettingUtils.getPlaceDirection(placePos);
            if (placePos != null && (placeDir[0] != null || placeDir[1] != null)) {
                event.renderer.box(OLEPOSSUtils.getBox(placePos), new Color(255, 0, 0, 50), new Color(255, 0, 0, 255), ShapeMode.Both, 0);
                if (placePos.equals(lastPos) && hold.get() && placed) {
                    lastPos = placePos;
                    int prevSlot = Managers.HOLDING.getSlot();
                    slots = new int[]{
                        getSlot(Blocks.GLOWSTONE),
                        InvUtils.find(itemStack -> !(itemStack.getItem() instanceof BlockItem)).slot()
                    };

                    if (slots[0] >= 0 && slots[1] >= 0) {
                        Direction glowDir = SettingUtils.getPlaceOnDirection(placePos);
                        placeGlowStone(placePos, glowDir, slots[0]);

                        Direction expDir = SettingUtils.getPlaceOnDirection(placePos);
                        explode(placePos, expDir, slots[1]);

                        InvUtils.swap(prevSlot, false);
                    }
                }
                if (nextPlace == -1 || System.currentTimeMillis() >= nextPlace) {
                    if (mc.world.getBlockState(placePos).getBlock().equals(Blocks.AIR)) {
                        int prevSlot = Managers.HOLDING.getSlot();
                        slots = new int[]{
                            getSlot(Blocks.RESPAWN_ANCHOR),
                            getSlot(Blocks.GLOWSTONE),
                            InvUtils.find(itemStack -> !(itemStack.getItem() instanceof BlockItem)).slot()
                        };
                        if (slots[0] >= 0 && (hold.get() || (slots[1] >= 0 && slots[2] >= 0))) {
                            placeAnchor(placePos, placeDir, slots[0]);
                            if (!hold.get()) {
                                Direction glowDir = SettingUtils.getPlaceOnDirection(placePos);
                                placeGlowStone(placePos, glowDir, slots[1]);

                                Direction expDir = SettingUtils.getPlaceOnDirection(placePos);
                                explode(placePos, expDir, slots[2]);
                                placed = false;
                            }
                            InvUtils.swap(prevSlot, false);
                            if (!hold.get()) {
                                nextPlace = Math.round(System.currentTimeMillis() + 1000 / placeSpeed.get());
                            }
                        }
                    } else if (mc.world.getBlockState(placePos).getBlock().equals(Blocks.RESPAWN_ANCHOR)) {
                        int prevSlot = Managers.HOLDING.getSlot();

                        slots = new int[]{
                            getSlot(Blocks.GLOWSTONE),
                            InvUtils.find(itemStack -> !(itemStack.getItem() instanceof BlockItem)).slot(),
                            getSlot(Blocks.RESPAWN_ANCHOR)
                        };

                        if (slots[0] >= 0 && slots[1] >= 0 && (!hold.get() || slots[2] >= 0)) {
                            Direction glowDir = SettingUtils.getPlaceOnDirection(placePos);
                            placeGlowStone(placePos, glowDir, slots[0]);

                            Direction expDir = SettingUtils.getPlaceOnDirection(placePos);
                            explode(placePos, expDir, slots[1]);

                            if (hold.get()) {
                                placeAnchor(placePos, placeDir, slots[2]);
                                placed = true;
                            }
                            InvUtils.swap(prevSlot, false);
                            if (hold.get()) {
                                nextPlace = Math.round(System.currentTimeMillis() + 1000 / placeSpeed.get());
                            }
                        }
                    }
                }
            }
        }
    }

    void placeAnchor(BlockPos pos, Direction[] dir, int slot) {
        if (!Managers.HOLDING.isHolding(Items.RESPAWN_ANCHOR)) {
            InvUtils.swap(slot, false);
        }
        SettingUtils.swing(SwingSettings.SwingState.Pre, SwingSettings.SwingType.Placing);
        if (dir[1] != null) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false), 0));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.offset(dir[0]).getX() + 0.5, pos.offset(dir[0]).getY() + 0.5, pos.offset(dir[0]).getZ() + 0.5), dir[0].getOpposite(), pos.offset(dir[0]), false), 0));
        }
        SettingUtils.swing(SwingSettings.SwingState.Post, SwingSettings.SwingType.Placing);
    }
    void placeGlowStone(BlockPos pos, Direction dir, int slot) {
        if (!Managers.HOLDING.isHolding(Items.GLOWSTONE)) {
            InvUtils.swap(slot, false);
        }
        SettingUtils.swing(SwingSettings.SwingState.Pre, SwingSettings.SwingType.Placing);
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), dir, pos, false), 0));
        SettingUtils.swing(SwingSettings.SwingState.Post, SwingSettings.SwingType.Placing);
    }
    void explode(BlockPos pos, Direction dir, int slot) {
        if (Managers.HOLDING.getSlot() != slot) {
            InvUtils.swap(slot, false);
        }
        SettingUtils.swing(SwingSettings.SwingState.Pre, SwingSettings.SwingType.Placing);
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), dir, pos, false), 0));
        SettingUtils.swing(SwingSettings.SwingState.Post, SwingSettings.SwingType.Placing);
    }

    BlockPos getPos() {
        BlockPos best = null;
        double bestDmg = -1;
        int c = (int) Math.ceil(SettingUtils.getPlaceRange() + 1);
        for (int x = -c; x <= c; x++) {
            for (int y = -c; y <= c; y++) {
                for (int z = -c; z <= c; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    Block block = getBlock(pos);
                    if (block == Blocks.AIR || block == Blocks.RESPAWN_ANCHOR) {
                        Direction[] dirs = SettingUtils.getPlaceDirection(pos);
                        if (dirs[0] != null || dirs[1] != null) {
                            if (SettingUtils.inPlaceRange(pos) && (dirs[1] != null || SettingUtils.inPlaceRange(pos.offset(dirs[0])))) {
                                if (!EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(pos), entity -> !(entity instanceof EndCrystalEntity) && !(entity instanceof ItemEntity))) {
                                    double dmg = getDmg(pos);
                                    if (dmg >= minDamage.get() && dmg > bestDmg) {
                                        best = pos;
                                        bestDmg = dmg;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return best;
    }
    List<LivingEntity> findTargets() {
        Map<LivingEntity, Double> found = new HashMap<>();
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl) && pl.getHealth() > 0) {
                found.put(pl, OLEPOSSUtils.distance(mc.player.getPos(), pl.getPos()));
            }
        }
        return sort(found);
    }
    List<LivingEntity> sort(Map<LivingEntity, Double> map) {
        List<LivingEntity> list = new ArrayList<>();
        int a = Math.min(map.size(), 3);
        for (int i = 0; i < a; i++) {
            LivingEntity closestE = null;
            double closest = Double.MAX_VALUE;
            for (Map.Entry<LivingEntity, Double> entry : map.entrySet()) {
                LivingEntity rur = entry.getKey();
                Double s = entry.getValue();
                if (s < closest) {
                    closestE = rur;
                    closest = s;
                }
            }
            list.add(closestE);
        }
        return list;
    }
    Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    double getDmg(BlockPos pos) {
        double highest = -1;
        for (LivingEntity player : targets) {
            double dmg = BODamageUtils.anchorDamage(player, OLEPOSSUtils.getMiddle(pos));
            if (dmg > highest) {
                highest = dmg;
            }
        }
        return highest;
    }

    int getSlot(Block block) {
        return InvUtils.find(itemStack -> itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock().equals(block)).slot();
    }
}
