package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/*
Made by OLEPOSSU / Raksamies and KassuK(KassuK's version was better)
*/

public class OffHandPlus extends BlackOutModule {
    public OffHandPlus() {
        super(BlackOut.BLACKOUT, "Offhand+", "Better offhand");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> onlyInInv = sgGeneral.add(new BoolSetting.Builder()
        .name("Only in inventory")
        .description("Will only switch if you are in your inventory")
        .defaultValue(false)
        .build()
    );
    private final Setting<switchModes> switchMode = sgGeneral.add(new EnumSetting.Builder<switchModes>()
        .name("Prefer")
        .description("Where do we pick items from")
        .defaultValue(switchModes.Inventory)
        .build()
    );
    private final Setting<itemModes> itemMode = sgGeneral.add(new EnumSetting.Builder<itemModes>()
        .name("Item")
        .description("")
        .defaultValue(itemModes.Crystal)
        .build()
    );
    private final Setting<swordGapModes> swordGapple = sgGeneral.add(new EnumSetting.Builder<swordGapModes>()
        .name("Sword Gapple")
        .description("Holds gapples while swording")
        .defaultValue(swordGapModes.Pressing)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay between switches.")
        .defaultValue(0.05)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> safety = sgGeneral.add(new BoolSetting.Builder()
        .name("Safety")
        .description("Tries to prevent offhand fails by switching while in danger")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> safetyHealth = sgGeneral.add(new IntSetting.Builder()
        .name("Safety Health")
        .description("Holds totem if you would have under this health after getting damage.")
        .defaultValue(0)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );

    private final Setting<Integer> hp = sgGeneral.add(new IntSetting.Builder()
        .name("Health")
        .description("At what health value we will switch")
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
    public enum itemModes {
        Crystal,
        Gapple,
        Totem
    }

    double timer = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
        if (mc.player != null && mc.world != null) {
            Item item = getItem();
            if (!mc.player.getOffHandStack().getItem().equals(item) && item != null) {
                if (onlyInInv.get() && !(mc.currentScreen instanceof InventoryScreen)){
                    return;
                }
                FindItemResult slot = find(item, switchMode.get().equals(switchModes.Hotbar));
                slot = slot.count() > 0 ? slot : find(item, !switchMode.get().equals(switchModes.Hotbar));
                if (slot.count() > 0 && timer >= delay.get()) {
                    timer = 0;
                    InvUtils.move().from(slot.slot()).toOffhand();
                }
            }
        }
    }

    Item getItem() {
        if (mc.player != null) {
            Suicide suicide = Modules.get().get(Suicide.class);

            boolean crystalAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.END_CRYSTAL)).count() > 0;
            boolean totemAvailable = !(suicide.isActive() && suicide.offhand.get()) && InvUtils.find(itemStack -> itemStack.getItem().equals(Items.TOTEM_OF_UNDYING)).count() > 0;
            boolean gapAvailable = InvUtils.find(itemStack -> itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)).count() > 0;
            boolean shouldGap = (swordGapple.get().equals(swordGapModes.Always) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem())) ||
                (swordGapple.get().equals(swordGapModes.Pressing) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem()) && mc.options.useKey.isPressed()) ||
                (swordGapple.get().equals(swordGapModes.Smart) && OLEPOSSUtils.isSword(mc.player.getMainHandStack().getItem()) && mc.options.useKey.isPressed());
            boolean firstAvailable = itemMode.get().equals(itemModes.Crystal) ? crystalAvailable : gapAvailable;
            boolean secondAvailable = itemMode.get().equals(itemModes.Crystal) ? gapAvailable : crystalAvailable;

            double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            boolean safe = health > hp.get() && isSafe(health);
            if (shouldGap && gapAvailable && !swordGapple.get().equals(swordGapModes.Smart) ||
                (swordGapple.get().equals(swordGapModes.Smart) && safe && gapAvailable && shouldGap)) {
                return Items.ENCHANTED_GOLDEN_APPLE;
            } else if (itemMode.get().equals(itemModes.Totem) && totemAvailable) {
                return Items.TOTEM_OF_UNDYING;
            } else {
                if (!totemAvailable) {
                    if (firstAvailable) {
                        return itemMode.get().equals(itemModes.Crystal) ? Items.END_CRYSTAL : Items.ENCHANTED_GOLDEN_APPLE;
                    } else if (secondAvailable) {
                        return itemMode.get().equals(itemModes.Crystal) ? Items.ENCHANTED_GOLDEN_APPLE : Items.END_CRYSTAL;
                    } else {
                        return null;
                    }
                } else {
                    if (safe) {
                        if (firstAvailable) {
                            return itemMode.get().equals(itemModes.Crystal) ? Items.END_CRYSTAL : Items.ENCHANTED_GOLDEN_APPLE;
                        } else if (secondAvailable) {
                            return itemMode.get().equals(itemModes.Crystal) ? Items.ENCHANTED_GOLDEN_APPLE : Items.END_CRYSTAL;
                        }
                    } else {
                        return Items.TOTEM_OF_UNDYING;
                    }
                }
            }
        }
        return null;
    }

    FindItemResult find(Item i, boolean hotbar) {
        return InvUtils.find(itemStack -> itemStack.getItem().equals(i), hotbar ? 0 : 9, hotbar ? 8 : mc.player.getInventory().size());
    }

    boolean isSafe(double playerHP) {
        return !safety.get() || PlayerUtils.possibleHealthReductions() < playerHP - safetyHealth.get();
    }
}
