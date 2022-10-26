package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoCraftingTable extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(5)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> minValue = sgGeneral.add(new IntSetting.Builder()
        .name("Min Value")
        .description("Don't change if you dont knwo what it does")
        .defaultValue(5)
        .range(0, 18)
        .sliderMax(12)
        .build()
    );

    BlockPos placePos;

    public AutoCraftingTable() {
        super(Addon.ANARCHY, "AutoCraftingTable", "Opens Crafting Table");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        placePos = findPos();
        if (placePos != null) {
            ChatUtils.sendMsg(Text.of(placePos.getX() + "  " + placePos.getY() + "  " + placePos.getZ()));
            place(placePos);
        } else {
            this.toggle();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null && placePos != null) {
            if (event.newState.getBlock() == Blocks.CRAFTING_TABLE) {
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()),
                        Direction.UP, placePos, false), 0));
            }
        }
    }

    private BlockPos findPos() {
        BlockPos bestPos = null;
        int value = minValue.get() - 6;
        for (int x = (int) -Math.ceil(range.get()); x <= Math.ceil(range.get()); x++) {
            for (int y = (int) -Math.ceil(range.get()); y <= Math.ceil(range.get()); y++) {
                for (int z = (int) -Math.ceil(range.get()); z <= Math.ceil(range.get()); z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    if (pos.getY() > 1 && OLEPOSSUtils.distance(mc.player.getPos(),
                        new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) <= range.get() &&
                        mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR))  {
                        if (bestPos == null) {
                            int v = getValue(pos);
                            bestPos = pos;
                            value = v;
                        } else {
                            int v = getValue(pos);
                            if (v > value) {
                                bestPos = pos;
                                value = v;
                            }
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private int getValue(BlockPos pos) {
        int value = 0;
        for (Direction dir : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.BEDROCK)) {
                value += 2;
            } else if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.OBSIDIAN)) {
                value++;
            } else if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.AIR)) {
                value--;
            }
        }
        return value;
    }


    private void place(BlockPos pos) {
        InvUtils.swap(InvUtils.findInHotbar(Items.CRAFTING_TABLE).slot(), true);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                Direction.UP, pos, false), 0));
        InvUtils.swapBack();
    }
}
