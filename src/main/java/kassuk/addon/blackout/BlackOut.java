package kassuk.addon.blackout;

import com.mojang.logging.LogUtils;
import kassuk.addon.blackout.commands.*;
import kassuk.addon.blackout.globalsettings.*;
import kassuk.addon.blackout.hud.*;
import kassuk.addon.blackout.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

/**
 * @author OLEPOSSU
 * @author KassuK
 */

public class BlackOut extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final Category BLACKOUT = new Category("BlackOut", Items.END_CRYSTAL.getDefaultStack());
    public static final Category SETTINGS = new Category("Settings", Items.OBSIDIAN.getDefaultStack());
    public static final HudGroup HUD_BLACKOUT = new HudGroup("BlackOut");
    public static final String BLACKOUT_NAME = "BlackOut";
    public static final String BLACKOUT_VERSION = "1.0.1";
    public static final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness";

    @Override
    public void onInitialize() {
        LOG.info("Initializing Blackout");

        initializeModules(Modules.get());

        initializeSettings(Modules.get());

        initializeCommands();

        initializeHud(Hud.get());
    }

    private void initializeModules(Modules modules) {
        modules.add(new AnchorAuraPlus());
        modules.add(new AnteroTaateli());
        modules.add(new AntiAim());
        modules.add(new AntiCrawl());
        modules.add(new AutoCraftingTable());
        modules.add(new AutoCrystalPlus());
        modules.add(new AutoEz());
        modules.add(new Automation());
        modules.add(new AutoMend());
        modules.add(new AutoMine());
        modules.add(new AutoMoan());
        modules.add(new AutoPearl());
        initializeAutoPVP(modules);
        modules.add(new AutoTrapPlus());
        modules.add(new BedAuraPlus());
        modules.add(new Blocker());
        modules.add(new BurrowPlus());
        modules.add(new CustomFOV());
        modules.add(new ElytraFlyPlus());
        modules.add(new FastXP());
        modules.add(new FeetESP());
        modules.add(new FlightPlus());
        modules.add(new Fog());
        modules.add(new ForceSneak());
        modules.add(new HoleFillPlus());
        modules.add(new HoleFillRewrite());
        modules.add(new HoleSnap());
        modules.add(new JesusPlus());
        modules.add(new KillAuraPlus());
        modules.add(new LightsOut());
        modules.add(new MineESP());
        modules.add(new OffHandPlus());
        modules.add(new PacketFly());
        modules.add(new PacketLogger());
        modules.add(new PingSpoof());
        modules.add(new PistonCrystal());
        modules.add(new PistonPush());
        modules.add(new PortalGodMode());
        modules.add(new RPC());
        modules.add(new ScaffoldPlus());
        modules.add(new SelfTrapPlus());
        modules.add(new SoundModifier());
        modules.add(new SpeedPlus());
        modules.add(new SprintPlus());
        modules.add(new StepPlus());
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
        modules.add(new ServerSettings());
        modules.add(new SwingSettings());
    }

    private void initializeCommands() {
        Commands.add(new BlackoutGit());
        Commands.add(new Coords());
    }

    private void initializeHud(Hud hud) {
        hud.register(ArmorHudPlus.INFO);
        hud.register(BlackoutArray.INFO);
        hud.register(GearHud.INFO);
        hud.register(HudWaterMark.INFO);
        hud.register(Keys.INFO);
        hud.register(TargetHud.INFO);
        hud.register(Welcomer.INFO);
        hud.register(OnTope.INFO);
        hud.register(CatGirl.INFO);
    }

    private void initializeAutoPVP(Modules modules) {
        try {
            Class.forName("baritone.api.BaritoneAPI");
            modules.add(new AutoPvp());
        } catch (ClassNotFoundException ignored) {}
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
