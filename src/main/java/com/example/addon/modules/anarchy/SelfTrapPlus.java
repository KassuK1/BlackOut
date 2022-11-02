package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Made by OLEPOSSU / Raksamies
*/

public class SelfTrapPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(3)
        .range(0, 10)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> yRange = sgGeneral.add(new IntSetting.Builder()
        .name("Range")
        .description("Range")
        .defaultValue(3)
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


    public SelfTrapPlus() {
        super(Addon.ANARCHY, "Self Trap+", "Bullies enemies (evil when it works)");
    }

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Modules.get().get(Timer.class).setOverride(1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (inHole() && mc.world.getBlockState(mc.player.getBlockPos().up(2)).getBlock().equals(Blocks.AIR)) {
                if (shouldPlace()) {
                    BlockPos pos = mc.player.getBlockPos().up(2);
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    if (!EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !(
                    entity.getBlockPos().equals(pos.up()) && entity.getType() != EntityType.ITEM))) {

                        InvUtils.swap(InvUtils.findInHotbar(itemStack -> itemStack.getItem().equals(Items.OBSIDIAN)).slot(), true);
                        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                            new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                                Direction.UP, pos, false), 0));
                        InvUtils.swapBack();
                    }
                }
            }
        }
    }

    private boolean inHole() {
        BlockPos pos = mc.player.getBlockPos();
        if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {return false;}
        for (Direction dir : horizontals) {
            if (mc.world.getBlockState(pos.offset(dir)).getBlock() == Blocks.AIR) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldPlace() {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player) {
                BlockPos pPos = mc.player.getBlockPos();
                Vec3d ePos = pl.getPos();
                if (ePos.y >= pPos.getY() + 1 && OLEPOSSUtils.distance(new Vec3d(ePos.x, 0, ePos.z), new Vec3d(pPos.getX() + 0.5,
                    0, pPos.getZ() + 0.5)) <= range.get() && ePos.y - pPos.getY() <= yRange.get()) {
                    return true;
                }
            }
        }
        return false;
    }
}
