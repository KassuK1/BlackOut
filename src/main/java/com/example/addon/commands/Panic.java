package com.example.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.ArrayList;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;


public class Panic extends Command {
    public Panic() {
        super("Panic", "Toggles every module off");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            //I wonder will anyone ever use this probably not.
            new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);
            //need to add a thing to toggle hud also if it is possible
            info("Successful panic attack");
            return SINGLE_SUCCESS;
        });
    }
}
