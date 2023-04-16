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
import net.minecraft.entity.effect.StatusEffect;

/*
Made by KassuK
*/


public class WeakAlert extends BlackOutModule {
    public WeakAlert() {
        super(BlackOut.BLACKOUT, "WeakAlert", "Alerts you if you get weakness");
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
        .description("Delay.")
        .defaultValue(5)
        .range(0, 60)
        .sliderMax(60)
        .build()
    );
    int timer = 0;
    boolean last = false;

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (mc.player.hasStatusEffect(StatusEffect.byRawId(18))) {
                if (single.get()) {
                    if (!last) {
                        last = true;
                        info("You have Weakness!!!");
                    }
                } else {
                    if (timer > 0) {
                        timer--;
                    } else {
                        timer = delay.get();
                        last = true;
                        info("You have Weakness!!!");
                    }
                }
            } else if (last) {
                last = false;
                info("Weakness has ended");
            }
        }
    }
}


