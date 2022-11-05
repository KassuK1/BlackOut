package com.example.addon;

import com.example.addon.commands.BlackoutGit;
import com.example.addon.commands.GearInfo;
import com.example.addon.commands.Kick;
import com.example.addon.commands.Panic;
import com.example.addon.hud.GearHud;
import com.example.addon.hud.HudExample;
import com.example.addon.hud.HudHelper;
import com.example.addon.modules.anarchy.*;
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

    public static final HudGroup HUD_BLACKOUT = new HudGroup("BlackOut");


    @Override
    public void onInitialize() {
        LOG.info("Initializing Blackout");

        // Ghost
        Modules.get().add(new AutoAnchor());
        Modules.get().add(new AutoClickerPlus());
        Modules.get().add(new GhostCrystal());
        Modules.get().add(new LegitTotem());

        //Anarchy
        Modules.get().add(new AntiAim());
        Modules.get().add(new AutoCraftingTable());
        Modules.get().add(new AutoCrystal());
        Modules.get().add(new AutoEz());
        Modules.get().add(new AutoMine());
        Modules.get().add(new AutoPearl());
        Modules.get().add(new BedBomb());
        Modules.get().add(new CevBreaker());
        Modules.get().add(new FastXP());
        Modules.get().add(new HoleSnap());
        Modules.get().add(new OffHandPlus());
        Modules.get().add(new ResetVL());
        Modules.get().add(new SelfTrapPlus());
        Modules.get().add(new SpeedPlus());
        Modules.get().add(new SprintPlus());
        Modules.get().add(new WeakAlert());
        Modules.get().add(new WebPlus());
        Modules.get().add(new ScaffoldPlus());
        Modules.get().add(new NCPDamageFly());

        // Commands
        Commands.get().add(new BlackoutGit());
        Commands.get().add(new GearInfo());
        Commands.get().add(new Kick());
        Commands.get().add(new Panic());

        // HUD
        Hud.get().register(GearHud.INFO);
        Hud.get().register(HudExample.INFO);
        Hud.get().register(HudHelper.INFO);
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
