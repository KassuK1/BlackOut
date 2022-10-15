package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class GhostCrystal extends Module {

    protected BlockPos placePos;
    public GhostCrystal() {
        super(Addon.CATEGORY, "AutoCrystal", "Breaks crystals automately.");
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInteract(InteractBlockEvent event) {
        if (mc.player != null) {
            ChatUtils.sendMsg(Text.of("rurr"));
            BlockHitResult result = event.result;
            boolean holding = mc.player.getStackInHand(event.hand).equals(new ItemStack(Items.END_CRYSTAL));
            if (holding) {
                placePos = result.getBlockPos();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onSpawn(EntityAddedEvent event) {
        if (mc.player != null) {
            BlockPos position = new BlockPos(event.entity.getBlockPos());
            if (position.equals(placePos)) {
                mc.player.attack(event.entity);
            }
        }
    }
}
