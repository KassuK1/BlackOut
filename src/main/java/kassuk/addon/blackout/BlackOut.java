package kassuk.addon.blackout;

import com.mojang.logging.LogUtils;
import kassuk.addon.blackout.commands.*;
import kassuk.addon.blackout.globalsettings.*;
import kassuk.addon.blackout.hud.*;
import kassuk.addon.blackout.modules.*;
import kassuk.addon.blackout.modules.CustomFOV;
import kassuk.addon.blackout.modules.FeetESP;
import kassuk.addon.blackout.modules.Fog;
import kassuk.addon.blackout.modules.SwingModifier;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class BlackOut extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final Category BLACKOUT = new Category("BlackOut", Items.END_CRYSTAL.getDefaultStack());
    public static final Category SETTINGS = new Category("Settings", Items.OBSIDIAN.getDefaultStack());
    public static final HudGroup HUD_BLACKOUT = new HudGroup("BlackOut");
    public static final String BLACKOUT_NAME = "BlackOut";
    public static final String BLACKOUT_VERSION = "0.3.2";
    public static final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness";

    @Override
    public void onInitialize() {
        LOG.info("Initializing Blackout");

        initializeModules(Modules.get());

        initializeSettings(Modules.get());

        initializeCommands(Commands.get());

        initializeHud(Hud.get());
    }

    private void initializeModules(Modules modules) {
        modules.add(new AnchorAuraPlus());
        modules.add(new AnteroTaateli());
        modules.add(new AntiAim());
        modules.add(new AntiCrawl());
        modules.add(new AutoCraftingTable());
        modules.add(new AutoCrystalRewrite());
        modules.add(new AutoEz());
        modules.add(new AutoMend());
        modules.add(new AutoMine());
        modules.add(new AutoMoan());
        modules.add(new AutoPearl());
        modules.add(new AutoTrapPlus());
        modules.add(new BedAuraPlus());
        modules.add(new CustomFOV());
        modules.add(new FastXP());
        modules.add(new FeetESP());
        modules.add(new FlightPlus());
        modules.add(new Fog());
        modules.add(new HoleFillRewrite());
        modules.add(new HoleSnap());
        modules.add(new KassuKAura());
        modules.add(new KillAuraPlus());
        modules.add(new LightsOut());
        modules.add(new OffHandPlus());
        modules.add(new PacketCrash());
        modules.add(new PacketFly());
        modules.add(new ForceSneak());
        modules.add(new PurpleSpinnyThingBlowerUpererAndPlacer());
        modules.add(new ResetVL());
        modules.add(new RPC());
        modules.add(new ScaffoldPlus());
        modules.add(new SelfTrapPlus());
        modules.add(new SoundModifier());
        modules.add(new SpeedPlus());
        modules.add(new SprintPlus());
        modules.add(new StrictNoSlow());
        modules.add(new Suicide());
        modules.add(new SurroundPlus());
        modules.add(new SwingModifier());
        modules.add(new TickShift());
        modules.add(new WeakAlert());
    }

    private void initializeSettings(Modules modules) {
        modules.add(new FacingSettings());
        modules.add(new RangeSettings());
        modules.add(new RaytraceSettings());
        modules.add(new RotationSettings());
        modules.add(new SwingSettings());
    }

    private void initializeCommands(Commands commands) {
        commands.add(new BlackoutGit());
        commands.add(new Coords());
    }

    private void initializeHud(Hud hud) {
        hud.register(BlackoutArray.INFO);
        hud.register(GearHud.INFO);
        hud.register(HudWaterMark.INFO);
        hud.register(Keys.INFO);
        hud.register(TargetHud.INFO);
        hud.register(Welcomer.INFO);
        hud.register(OnTope.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(BLACKOUT);
        Modules.registerCategory(SETTINGS);
    }

    @Override
    public String getPackage() {
        return "kassuk.addon.blackout";
    }
}
