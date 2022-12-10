package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.managers.Managers;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
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

public class SurroundPlus extends Module {
    public SurroundPlus() {super(BlackOut.ANARCHY, "Surround+", "KasumsSoft surround");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> itemSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Switch")
        .description("Should we switch to obby")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
        .name("Center")
        .description("Should we center on da hole")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> floor = sgGeneral.add(new BoolSetting.Builder()
        .name("Floor")
        .description(".")
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
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("U blind?")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .build()
    );

    List<PlacedTimer> timers = new ArrayList<>();
    private BlockPos startPos = null;
    double placeTimer = 0;
    private List<BlockPos> render = new ArrayList<>();
    private List<BlockPos>[] check(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        List<BlockPos> renders = new ArrayList<>();
        List<BlockPos> blocks = getBlocks(getSize(pos));
        if (mc.player != null && mc.world != null) {
            for (BlockPos position : blocks) {
                if (mc.world.getBlockState(position).getBlock().equals(Blocks.AIR)) {
                    if (!isPlaced(position) && !EntityUtils.intersectsWithEntity(OLEPOSSUtils.getBox(position), entity -> !entity.isSpectator() && entity.getType() != EntityType.ITEM)) {
                        list.add(position);
                    }
                    renders.add(position);
                }
            }
        }
        return new List[] {list, renders};
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player == null || mc.world == null) {toggle();}
        startPos = mc.player.getBlockPos();
        render = new ArrayList<>();
    }

    @EventHandler
    private void onRender(Render3DEvent event){
        placeTimer = Math.min(placeDelay.get(), placeTimer + event.frameTime);
        List<PlacedTimer> toRemove = new ArrayList<>();
            timers.forEach(item -> {
                item.update((float) event.frameTime);
                if (!item.isValid()) {
                    toRemove.add(item);
                }
            });
        toRemove.forEach(timers::remove);
        render.forEach(item -> event.renderer.box(OLEPOSSUtils.getBox(item), new Color(color.get().r, color.get().g, color.get().b,
            (int) Math.floor(color.get().a / 5f)), color.get(), ShapeMode.Both, 0));
        update();
    }

    private void update() {
        if (mc.player != null && mc.world != null) {
            if (!mc.player.getBlockPos().equals(startPos)) {
                toggle();
            }
            else if (placeTimer >= placeDelay.get()) {
                List<BlockPos>[] blocks = check(mc.player.getBlockPos());
                render = blocks[1];
                List<BlockPos> placements = blocks[0];
                FindItemResult result = InvUtils.findInHotbar(Items.OBSIDIAN);
                int[] obsidian = findObby();
                if (obsidian[1] > 0 && (Managers.HOLDING.isHolding(Items.OBSIDIAN) || itemSwitch.get()) && !placements.isEmpty() &&
                    (!pauseEat.get() || !mc.player.isUsingItem())) {
                    boolean swapped = false;
                    if (!Managers.HOLDING.isHolding(Items.OBSIDIAN) && itemSwitch.get()) {
                        InvUtils.swap(result.slot(), true);
                        swapped = true;
                    }
                    if (center.get()) {
                        PlayerUtils.centerPlayer();
                    }
                    for (int i = 0; i < Math.min(Math.min(obsidian[1], places.get()), placements.size()); i++) {
                        BlockPos toPlace = placements.get(i);
                        timers.add(new PlacedTimer(toPlace, delay.get()));
                        placeTimer = 0;
                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                            new BlockHitResult(new Vec3d(toPlace.getX() + 0.5, toPlace.getY() + 0.5, toPlace.getZ() + 0.5), Direction.UP, toPlace, false), 0));
                        if (swing.get()) {
                            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                        }
                    }
                    if (swapped) {
                        InvUtils.swapBack();
                    }
                }
            }
        }
    }

    private int[] findObby() {
        int num = 0;
        int slot = 0;
        if (mc.player != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getCount() > num && stack.getItem().equals(Items.OBSIDIAN)) {
                    num = stack.getCount();
                    slot = i;
                }
            }
        }
        return new int[] {slot, num};
    }

    private boolean isPlaced(BlockPos pos) {
        for (PlacedTimer pt : timers) {
            if (pt.pos.equals(pos)) {return true;}
        }
        return false;
    }

    private int[] getSize(BlockPos pos) {
        int minX = 0;
        int maxX = 0;
        int minZ = 0;
        int maxZ = 0;
        if (mc.player != null && mc.world != null) {
            Box box = mc.player.getBoundingBox();
            if (box.intersects(OLEPOSSUtils.getBox(pos.north()))) {
                minZ--;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.south()))) {
                maxZ++;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.west()))) {
                minX--;
            }
            if (box.intersects(OLEPOSSUtils.getBox(pos.east()))) {
                maxX++;
            }
        }
        return new int[]{minX, maxX, minZ, maxZ};
    }

    private List<BlockPos> getBlocks(int[] size) {
        List<BlockPos> list = new ArrayList<>();
        if (mc.player != null && mc.world != null) {
            for (int x = size[0] - 1; x <= size[1] + 1; x++) {
                for (int z = size[2] - 1; z <= size[3] + 1; z++) {
                    boolean isX = x == size[0] - 1 || x == size[1] + 1;
                    boolean isZ = z == size[2] - 1 || z == size[3] + 1;
                    if (isX != isZ) {
                        list.add(mc.player.getBlockPos().add(x, 0, z));
                    } else if (!isX && floor.get()) {
                        list.add(mc.player.getBlockPos().add(x, -1, z));
                    }
                }
            }
        }
        return list;
    }


    private static class PlacedTimer {
        public BlockPos pos;
        public double time;

        public PlacedTimer(BlockPos pos, double time) {
            this.pos = pos;
            this.time = time;
        }

        public void update(float delta) {time -= delta;}
        public boolean isValid() {return time > 0;}
    }
}
