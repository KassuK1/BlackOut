package com.example.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GearInfo extends Command {

    private int totem;
    private int crystal;
    private int obby;
    private int gapple;
    private int crapple;
    public GearInfo() {
        super("GearInfo", "Tells you how much stuff you have");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totem = result.count();
        FindItemResult result2 = InvUtils.find(Items.END_CRYSTAL);
        crystal = result2.count();
        FindItemResult result3 = InvUtils.find(Items.OBSIDIAN);
        obby = result3.count();
        FindItemResult result4 = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE);
       gapple = result4.count();
        FindItemResult result5 = InvUtils.find(Items.GOLDEN_APPLE);
        crapple = result5.count();

        builder.executes(context -> {
            info("Totems " + totem);
            info("Crystals  " + crystal);
            info("Obsidian  " + obby);
            info("Gapples " +gapple );
            info("Crapples" + crapple);
            info("Bitches " + 0);
            return SINGLE_SUCCESS;
        });
    }
}
