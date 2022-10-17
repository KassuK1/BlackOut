package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoSwitch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay for switching.")
        .defaultValue(2)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("Cooldown")
        .description("Cooldown for switching.")
        .defaultValue(20)
        .range(0, 100)
        .sliderMax(100)
        .build()
    );
    protected int timer = 0;

    public AutoSwitch() {
        super(Addon.CATEGORY, "AutoSwitch", "Switches to crystal after placing obby.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        timer = -1;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (timer > 0) {
            timer--;
        } else if (timer == 0) {
            timer = -cooldown.get();
            InvUtils.swap(InvUtils.findInHotbar(Items.END_CRYSTAL).slot(), false);
        } else if (timer < -1) {
            timer++;
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    private void onBlock(BlockUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (event.newState.getBlock().equals(Blocks.OBSIDIAN) && event.oldState.getBlock().equals(Blocks.AIR)
            && mc.player.isHolding(Items.OBSIDIAN) && timer == -1) {
                timer = delay.get();
            }
        }
    }
}
