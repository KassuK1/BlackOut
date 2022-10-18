package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;

public class OffHandPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> crystal = sgGeneral.add(new BoolSetting.Builder()
        .name("Crystal")
        .description("Should we hold a crystal if you are above the totem health")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hp = sgGeneral.add(new IntSetting.Builder()
        .name("Health")
        .description("When to switch")
        .defaultValue(5)
        .range(0, 20)
        .sliderMax(20)
        .build()
    );
    private int totems;
    private int crystals;
    private float health;

    public OffHandPlus() {
        super(Addon.CATEGORY, "Offhand+", "Non shit offhand");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        health = mc.player.getHealth() +mc.player.getAbsorptionAmount();
        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();{
            if (mc.player != null)
                if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING)
                    if (health < hp.get()){
                        InvUtils.move().from(result.slot()).toOffhand();}
        }
        FindItemResult result2 = InvUtils.find(Items.END_CRYSTAL);
        crystals = result2.count();{
            if (mc.player != null)
                if (crystal.get() && crystals > 0)
                    if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
                        if (health > hp.get()){
                            InvUtils.move().from(result2.slot()).toOffhand();}
        }
    }
}
