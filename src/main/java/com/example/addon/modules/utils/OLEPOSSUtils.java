package com.example.addon.modules.utils;

import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class OLEPOSSUtils extends Utils {
    public static double distance(Vec3d v1, Vec3d v2) {
        double dX = Math.abs(v1.x - v2.x);
        double dY = Math.abs(v1.y - v2.y);
        double dZ = Math.abs(v1.z - v2.z);
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static Item[] bedItems = new Item[] {
        Items.BLACK_BED,
        Items.BLUE_BED,
        Items.BROWN_BED,
        Items.CYAN_BED,
        Items.GRAY_BED,
        Items.GREEN_BED,
        Items.LIGHT_BLUE_BED,
        Items.LIGHT_GRAY_BED,
        Items.LIME_BED,
        Items.MAGENTA_BED,
        Items.ORANGE_BED,
        Items.PINK_BED,
        Items.RED_BED,
        Items.WHITE_BED,
        Items.YELLOW_BED,
        Items.PURPLE_BED
    };
    public static Block[] bedBlocks = new Block[] {
        Blocks.BLACK_BED,
        Blocks.BLUE_BED,
        Blocks.BROWN_BED,
        Blocks.CYAN_BED,
        Blocks.GRAY_BED,
        Blocks.GREEN_BED,
        Blocks.LIGHT_BLUE_BED,
        Blocks.LIGHT_GRAY_BED,
        Blocks.LIME_BED,
        Blocks.MAGENTA_BED,
        Blocks.ORANGE_BED,
        Blocks.PINK_BED,
        Blocks.RED_BED,
        Blocks.WHITE_BED,
        Blocks.YELLOW_BED,
        Blocks.PURPLE_BED
    };

    public static Item[] helmets = new Item[] {
        Items.LEATHER_HELMET,
        Items.IRON_HELMET,
        Items.GOLDEN_HELMET,
        Items.CHAINMAIL_HELMET,
        Items.DIAMOND_HELMET,
        Items.NETHERITE_HELMET,
        Items.TURTLE_HELMET,
    };
    public static Item[] chestPlates = new Item[] {
        Items.LEATHER_CHESTPLATE,
        Items.IRON_CHESTPLATE,
        Items.GOLDEN_CHESTPLATE,
        Items.CHAINMAIL_CHESTPLATE,
        Items.DIAMOND_CHESTPLATE,
        Items.NETHERITE_CHESTPLATE,
    };
    public static Item[] leggings = new Item[] {
        Items.LEATHER_LEGGINGS,
        Items.IRON_LEGGINGS,
        Items.GOLDEN_LEGGINGS,
        Items.CHAINMAIL_LEGGINGS,
        Items.DIAMOND_LEGGINGS,
        Items.NETHERITE_LEGGINGS,
    };
    public static Item[] boots = new Item[] {
        Items.LEATHER_BOOTS,
        Items.IRON_BOOTS,
        Items.GOLDEN_BOOTS,
        Items.CHAINMAIL_BOOTS,
        Items.DIAMOND_BOOTS,
        Items.NETHERITE_BOOTS,
    };

    public static boolean isBedItem(Item item) {
        for (Item i : bedItems) {
            if (i.equals(item)) {return true;}
        }
        return false;
    }
    public static boolean isBedBlock(Block block) {
        for (Block b : bedBlocks) {
            if (b.equals(block)) {return true;}
        }
        return false;
    }
    public static boolean isHelmet(Item item) {
        for (Item i : helmets) {
            if (i.equals(item)) {return true;}
        }
        return false;
    }
    public static boolean isChestPlate(Item item) {
        for (Item i : chestPlates) {
            if (i.equals(item)) {return true;}
        }
        return false;
    }
    public static boolean isLegging(Item item) {
        for (Item i : leggings) {
            if (i.equals(item)) {return true;}
        }
        return false;
    }
    public static boolean isBoot(Item item) {
        for (Item i : boots) {
            if (i.equals(item)) {return true;}
        }
        return false;
    }
    public static boolean isArmor(Item item) {
        return isHelmet(item) || isChestPlate(item) || isLegging(item) || isBoot(item);
    }
    public static String armorCategory(Item item) {
        return isHelmet(item) ? "helmet" : isChestPlate(item) ? "chestplate" : isLegging(item) ? "leggings" : isBoot(item) ? "boots" : null;
    }
}
