package com.example.addon.modules.ghost;

import com.example.addon.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;

public class LegitScaffold extends Module {
    public LegitScaffold() {super(BlackOut.GHOST, "LegitScaffold", "It do be kinda speed bridging doe");}

    @Override
    public void onDeactivate() {
        mc.options.sneakKey.setPressed(false);
    }
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null){
            mc.options.sneakKey.setPressed(mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock().equals(Blocks.AIR));
        }
    }
}
