package com.example.addon;

import com.example.addon.commands.BlackoutGit;
import com.example.addon.commands.GearInfo;
import com.example.addon.commands.Kick;
import com.example.addon.hud.HudExample;
import com.example.addon.modules.anarchy.FastXP;
import com.example.addon.modules.anarchy.OffHandPlus;
import com.example.addon.modules.anarchy.WeakAlert;
import com.example.addon.modules.ghost.*;
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
    public static final Category GHOST = new Category("Ghost");
    public static final Category ANARCHY = new Category("Anarchy");
    public static final HudGroup HUD_GHOST = new HudGroup("Ghost");
    public static final HudGroup HUD_ANARCHY = new HudGroup("Anarchy");


    @Override
    public void onInitialize() {
        LOG.info("Initializing Blackout");

        // Ghost
        Modules.get().add(new AutoAnchor());
        Modules.get().add(new AutoClickerPlus());
        Modules.get().add(new GhostCrystal());
        Modules.get().add(new LegitTotem());

        //Anarchy
        Modules.get().add(new OffHandPlus());
        Modules.get().add(new WeakAlert());
        Modules.get().add(new FastXP());

        // Commands
        Commands.get().add(new BlackoutGit());
        Commands.get().add(new GearInfo());
        Commands.get().add(new Kick());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(GHOST);
        Modules.registerCategory(ANARCHY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}
