package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ForceSneak extends Module {

    public ForceSneak() {
        super(BlackOut.BLACKOUT, "ForceSneak", "Makes everyone sneak");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
}
