package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.effect.StatusEffects;

/**
 * @author KassuK
 */

public class WeakAlert extends BlackOutModule {
    public WeakAlert() {
        super(BlackOut.BLACKOUT, "Weak Alert", "Alerts you if you get weakness.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> single = sgGeneral.add(new BoolSetting.Builder()
        .name("Single")
        .description("Only sends the message once.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Tick delay between sending the message.")
        .defaultValue(5)
        .range(0, 60)
        .sliderMax(60)
        .visible(() -> !single.get())
        .build()
    );

    private int timer = 0;
    private boolean last = false;

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
                if (single.get()) {
                    if (!last) {
                        last = true;
                        sendBOInfo("you have weakness!!!");
                    }
                } else {
                    if (timer > 0) {
                        timer--;
                    } else {
                        timer = delay.get();
                        last = true;
                        sendBOInfo("you have weakness!!!");
                    }
                }
            } else if (last) {
                last = false;
                sendBOInfo("weakness has ended");
            }
        }
    }
}


