package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import com.example.addon.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/*
Made by OLEPOSSU / Raksamies and KassuK
*/

public class OffHandPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<switchModes> switchMode = sgGeneral.add(new EnumSetting.Builder<switchModes>()
        .name("Sword Gapple")
        .description("Holds gapple when trying to eat while sword fagging")
        .defaultValue(switchModes.Inventory)
        .build()
    );
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
    private final Setting<swordGapModes> swordGapple = sgGeneral.add(new EnumSetting.Builder<swordGapModes>()
        .name("Sword Gapple")
        .description("Holds gapple when trying to eat while sword fagging")
        .defaultValue(swordGapModes.Pressing)
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
    public enum switchModes {
        Hotbar,
        Inventory,
    }
    public enum swordGapModes {
        Disabled,
        Always,
        Pressing,
        Smart
    }

    public OffHandPlus() {
        super(BlackOut.ANARCHY, "Offhand+", "Non shit offhand");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            Item item = getItem();
            if (!mc.player.getOffHandStack().getItem().equals(item) && item != null) {
                FindItemResult slot = find(item, switchMode.get().equals(switchModes.Hotbar));
                slot = slot.count() > 0 ? slot : find(item, !switchMode.get().equals(switchModes.Hotbar));
                if (slot.count() > 0) {
                    InvUtils.move().from(slot.slot()).toOffhand();
                }
            }
        }
    }

    private Item getItem() {
        if (mc.player != null) {
            boolean crystalAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).count() > 0;
            boolean totemAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.TOTEM_OF_UNDYING)).count() > 0;
            boolean gapAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)).count() > 0;
            boolean shouldGap = (swordGapple.get().equals(swordGapModes.Always) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem())) ||
                (swordGapple.get().equals(swordGapModes.Pressing) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem()) && mc.options.useKey.isPressed()) ||
                (swordGapple.get().equals(swordGapModes.Smart) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem()) && mc.options.useKey.isPressed());

            double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            boolean safe = health > hp.get() && isSafe(health);
            if (onlyTotem.get() && totemAvailable) {
                return Items.TOTEM_OF_UNDYING;
            } else if (shouldGap && gapAvailable && !swordGapple.get().equals(swordGapModes.Smart) ||
                (swordGapple.get().equals(swordGapModes.Smart) && safe && gapAvailable && shouldGap)) {
                return Items.ENCHANTED_GOLDEN_APPLE;
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
                    if (safe) {
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

    private FindItemResult find(Item i, boolean hotbar) {
        return InvUtils.find(itemStack -> itemStack.getItem().equals(i), hotbar ? 0 : 9, hotbar ? 8 : mc.player.getInventory().size());
    }

    private boolean isSafe(double playerHP) {
        return !safety.get() || PlayerUtils.possibleHealthReductions() < playerHP;
    }
}
