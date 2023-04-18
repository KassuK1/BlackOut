package kassuk.addon.blackout.utils;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;

public class ItemUtils {

    public static boolean isBedItem(Item item) {
        return item instanceof BedItem;
    }

    public static boolean isBedBlock(Block block) {
        return block instanceof BedBlock;
    }

    public static boolean isHelmet(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD;
    }

    public static boolean isChestPlate(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST;
    }

    public static boolean isLegging(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.LEGS;
    }

    public static boolean isBoot(Item item) {
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.FEET;
    }

    public static boolean isSword(Item item) {
        return item instanceof SwordItem;
    }

    public static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    public static String armorCategory(Item item) {
        if (item instanceof ArmorItem armorItem) {
            return switch (armorItem.getSlotType()) {
                case FEET -> "boots";
                case LEGS -> "leggings";
                case CHEST -> "chestplate";
                case HEAD -> "head";
                default -> null;
            };
        } else return null;
    }
}
