package com.example.addon;

import com.example.addon.commands.BlackoutGit;
import com.example.addon.commands.GearInfo;
import com.example.addon.hud.HudExample;
import com.example.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Blackout");
    public static final HudGroup HUD_GROUP = new HudGroup("Blackout");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Blackout");

        // Modules
        Modules.get().add(new GhostCrystal());
        Modules.get().add(new AutoAnchor());
        Modules.get().add(new LegitTotem());
        Modules.get().add(new WeakAlert());
        Modules.get().add(new AutoClickerPlus());
        Modules.get().add(new OffhandPlus());
        Modules.get().add(new AutoSwitch());

        // Commands
        Commands.get().add(new BlackoutGit());
        Commands.get().add(new GearInfo());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
