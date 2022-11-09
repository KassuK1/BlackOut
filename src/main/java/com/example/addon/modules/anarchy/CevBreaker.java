package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CevBreaker extends Module {

    public CevBreaker() {super(BlackOut.ANARCHY, "CevBreaker", "CevBreak");}
    private Item[] pickaxes = new Item[] {
        Items.NETHERITE_PICKAXE,
        Items.DIAMOND_PICKAXE,
        Items.IRON_PICKAXE,
        Items.STONE_PICKAXE,
        Items.GOLDEN_PICKAXE,
        Items.WOODEN_PICKAXE,
    };
    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerEntity target = findTarget();
        BlockPos pos = target.getBlockPos().up(2);

        FindItemResult result1 = InvUtils.findInHotbar(Items.OBSIDIAN);
        int obsidian = result1.count();

        FindItemResult result2 = InvUtils.findInHotbar(Items.END_CRYSTAL);
        int crystal = result2.count();

        FindItemResult result3 = InvUtils.findInHotbar(pickaxes);
        int pick = result3.count();

        if (mc.player != null && mc.world != null && target != null){
            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR && obsidian !=0){
                InvUtils.swap(result1.slot(), false);
                mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                    new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false), 0));
                }

                    if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN && crystal != 0){
                        InvUtils.swap(result2.slot(),false);
                        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
                            new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false), 0));
                        InvUtils.swap(result3.slot(), false);
                        if (mc.player.getMainHandStack().getItem() == pickaxes[1]){
                            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                            pos, Direction.UP));
                            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                            pos, Direction.UP));}
                    }
            }
    }
    private PlayerEntity findTarget() {
        PlayerEntity closest = null;
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player) {
                if (closest == null) {
                    closest = pl;
                } else if (OLEPOSSUtils.distance(mc.player.getPos(), pl.getPos()) < OLEPOSSUtils.distance(mc.player.getPos(), closest.getPos())) {
                    closest = pl;
                }
            }
        }
        return closest;
    }
}
