package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class Suicide extends BlackOutModule {
    public Suicide() {super(BlackOut.BLACKOUT, "Suicide", "Speeeeeeeed");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> offhand = sgGeneral.add(new BoolSetting.Builder()
        .name("Offhand")
        .description("Doesn't hold totem.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> disableDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable On Death")
        .description("Disables the module on death.")
        .defaultValue(true)
        .build()
    );

    @EventHandler(priority = 6969)
    private void onDeath(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen && disableDeath.get()) {
            toggle();
            sendDisableMsg("died");
        }
    }
}
