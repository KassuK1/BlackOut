package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class WebPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(2)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private Direction[] horizontals = new Direction[] {
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    };

    public WebPlus() {
        super(Addon.ANARCHY, "Web+", "Evil");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (isAt(mc.player.getBlockPos()) && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.AIR) {
                place(mc.player.getBlockPos());
            }
        }
    }

    private boolean isAt(BlockPos pos) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && (pl.getBlockPos().up(1) == pos || pl.getBlockPos().up(2) == pos)) {
                if (isAbove(2, pl)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void place(BlockPos pos) {
        if (InvUtils.findInHotbar(Items.COBWEB).count() > 0) {
            InvUtils.swap(InvUtils.findInHotbar(Items.COBWEB).slot(), true);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(pos.getX(), pos.getY() - 1, pos.getZ()),
                    Direction.UP, pos.down(), false), 0));
            InvUtils.swapBack();
        }
    }

    private boolean isAbove(int range, PlayerEntity pl) {
        for (int i = 1; i <= range; i++) {
            if (pl.getBlockPos() == mc.player.getBlockPos().up(i)) {
                return true;
            }
        }
        return false;
    }
}
