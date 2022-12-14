package com.example.addon.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HoldingManager {
    public int slot;
    public HoldingManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        slot = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacket(PacketEvent event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            slot = ((UpdateSelectedSlotC2SPacket) event.packet).getSelectedSlot();
        }
        if (event.packet instanceof UpdateSelectedSlotS2CPacket) {
            slot = ((UpdateSelectedSlotS2CPacket) event.packet).getSlot();
        }
    }

    public ItemStack getStack() {
        if (mc.player == null) {return null;}
        return mc.player.getInventory().getStack(slot);
    }

    public int getSlot() {
        return slot;
    }

    public boolean isHolding(Item item) {
        ItemStack stack = getStack();
        if (stack == null) {return false;}
        return stack.getItem().equals(item);
    }
}
