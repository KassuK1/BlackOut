package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

public class Sprint extends Module {

    public Sprint() {
        super(Addon.ANARCHY, "Sprint", "Non shit sprint");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            mc.player.setSprinting(true);
            mc.player.ticksSinceSprintingChanged = 100;
        }
    }
}
