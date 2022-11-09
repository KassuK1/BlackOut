package com.example.addon.modules.ghost;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoAnchor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swings.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> glowDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Glowstone Delay")
        .description("Delay for placing glowstone.")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> breakDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Break Delay")
        .description("Delay for breaking.")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Boolean> replace = sgGeneral.add(new BoolSetting.Builder()
        .name("Replace")
        .description("Replaces anchor when placed over enemy's head.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> replaceDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Replace Delay")
        .description("Delay for replacing.")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    protected BlockPos placePos = null;
    protected boolean canBreak = false;
    protected int glowTimer = 0;
    protected int breakTimer = 0;
    protected int replaceTimer = 0;

    public AutoAnchor() {
        super(BlackOut.GHOST, "AutoAnchor", "Blows up anchors.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        placePos = null;
        canBreak = false;
        glowTimer = 0;
        breakTimer = 0;
        replaceTimer = 0;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInteract(InteractBlockEvent event) {
        if (mc.player != null && mc.world != null) {
            BlockHitResult result = event.result;
            boolean holding = mc.player.isHolding(Items.RESPAWN_ANCHOR);
            if (holding) {
                if (placePos == null) {
                    placePos = result.getBlockPos().offset(event.result.getSide());
                    canBreak = true;
                } else if (mc.world.getBlockState(placePos).getBlock().equals(Blocks.AIR) ||
                !result.getBlockPos().equals(placePos)) {
                    placePos = result.getBlockPos().offset(event.result.getSide());
                    canBreak = true;
                } else {
                    if (canBreak) {
                        event.cancel();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (glowTimer > 0) {
            glowTimer -= 1;
        } else if (glowTimer == 0) {
            glowTimer = -1;
            InvUtils.swap(InvUtils.findInHotbar(Items.GLOWSTONE).slot(), false);
            place();
        }
        if (breakTimer > 0) {
            breakTimer -= 1;
        } else if (breakTimer == 0) {
            breakTimer = -1;
            InvUtils.swap(InvUtils.findInHotbar(Items.RESPAWN_ANCHOR).slot(), false);
            place();
        }
        if (replaceTimer > 0) {
            replaceTimer -= 1;
        } else if (replaceTimer == 0) {
            replaceTimer = -1;
            InvUtils.swap(InvUtils.findInHotbar(Items.RESPAWN_ANCHOR).slot(), false);
            place();
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    private void onBlock(BlockUpdateEvent event) {

        if (mc.player != null && placePos != null) {

            if (event.newState.getBlock().equals(Blocks.RESPAWN_ANCHOR) && !event.oldState.getBlock().equals(Blocks.RESPAWN_ANCHOR)) {

                BlockPos position = event.pos;
                if (placePos != null) {

                    if (position.equals(placePos)) {

                        glowTimer = glowDelay.get();
                        breakTimer = glowDelay.get() + breakDelay.get();
                    }
                }
            } else if (event.newState.getBlock().equals(Blocks.AIR) && event.oldState.getBlock().equals(Blocks.RESPAWN_ANCHOR) &&
            replace.get() && onTop(placePos)) {
                BlockPos position = event.pos;
                if (placePos != null) {
                    if (position.equals(placePos)) {
                        replaceTimer = replaceDelay.get();
                    }
                }
            }
        }
    }

    private void place() {
        if (mc.player != null && mc.world != null && placePos != null) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false), 0));
            if (swing.get()) {
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    private boolean onTop(BlockPos pos) {
        if (mc.player != null && mc.world != null) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                BlockPos playerPos = new BlockPos(Math.floor(player.getPos().x), Math.round(player.getPos().y), Math.floor(player.getPos().z));
                if (playerPos.offset(Direction.UP, 2).equals(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNextTo(BlockPos pos1, BlockPos pos2) {
        for (Direction dir : Direction.values()) {
            if (pos1.offset(dir).equals(pos2)) {
                return true;
            }
        }
        return false;
    }
}
