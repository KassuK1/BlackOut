package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * @author OLEPOSSU
 */

public class Suicide extends BlackOutModule {
    public Suicide() {
        super(BlackOut.BLACKOUT, "Suicide", "Kills yourself. Recommended.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> disableDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable On Death")
        .description("Disables the module on death.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> enableCA = sgGeneral.add(new BoolSetting.Builder()
        .name("Enable Auto Crystal")
        .description("Enables auto crystal when enabled.")
        .defaultValue(true)
        .build()
    );

    @Override
    public void onActivate() {
        if (enableCA.get() && !Modules.get().isActive(AutoCrystalPlus.class)) {
            Modules.get().get(AutoCrystalPlus.class).toggle();
        }
    }

    @EventHandler(priority = 6969)
    private void onDeath(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen && disableDeath.get()) {
            toggle();
            sendDisableMsg("died");
        }
    }
}
