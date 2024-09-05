package kassuk.addon.blackout.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

// IDK but shoutout cattyan again
public class EnchantmentUtils {

    public static int getLevel(RegistryKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantment : stack.getEnchantments().getEnchantmentEntries()) {
            if (enchantment.getKey().matchesKey(key)) return enchantment.getIntValue();
        }
        return 0;
    }

}
