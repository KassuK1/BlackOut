package com.example.addon.modules.anarchy;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class OffHandPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> onlyTotem = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Totem")
        .description("Should we hold a crystal if you are above the totem health")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> safety = sgGeneral.add(new BoolSetting.Builder()
        .name("Safety")
        .description("Doesn't fail")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hp = sgGeneral.add(new IntSetting.Builder()
        .name("Health")
        .description("When to switch")
        .defaultValue(14)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );

    public OffHandPlus() {
        super(Addon.ANARCHY, "Offhand+", "Non shit offhand");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            Item item = getItem();
            if (!mc.player.getOffHandStack().getItem().equals(item) && item != null) {
                InvUtils.move().from(InvUtils.find(item).slot()).toOffhand();
            }
        }
    }

    private Item getItem() {
        if (mc.player != null) {
            boolean crystalAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).count() > 0;
            boolean totemAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.TOTEM_OF_UNDYING)).count() > 0;
            boolean gapAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)).count() > 0;
            double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (onlyTotem.get() && totemAvailable) {
                return Items.TOTEM_OF_UNDYING;
            } else {
                if (!totemAvailable) {
                    if (crystalAvailable) {
                        return Items.END_CRYSTAL;
                    } else if (gapAvailable) {
                        return Items.ENCHANTED_GOLDEN_APPLE;
                    } else {
                        return null;
                    }
                } else {
                    if (health > hp.get() && isSafe(health)) {
                        if (crystalAvailable) {
                            return Items.END_CRYSTAL;
                        } else {
                            return Items.TOTEM_OF_UNDYING;
                        }
                    } else {
                        return Items.TOTEM_OF_UNDYING;
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafe(double playerHP) {
        return !safety.get() || PlayerUtils.possibleHealthReductions() < playerHP;
    }
}
