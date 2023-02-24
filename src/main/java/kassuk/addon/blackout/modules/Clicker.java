package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

import java.util.Random;

public class Clicker extends BlackOutModule {
    public Clicker() {
        super(BlackOut.BLACKOUT, "Clicker", "Clicks for you");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> cps = sgGeneral.add(new IntSetting.Builder()
        .name("CPS")
        .description("Delay that will be used for hits")
        .defaultValue(12)
        .sliderMax(20)
        .range(0, 20)
        .build()
    );
    double timer = 0;
    double delay = 0;

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(delay,timer + event.frameTime);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            delay = 1 / (float)cps.get();
            Random rand = new Random();
            int randint = rand.nextInt(101);

            if (randint == 100) {
                int currentCPS = cps.get();
                cps.set(currentCPS + (randint == 0 ? -1 : 1));
            }
            if (mc.options.attackKey.isPressed() || mc.options.attackKey.wasPressed()){
                if (timer >= delay){
                    mc.player.swingHand(Hand.MAIN_HAND);
                    Utils.leftClick();
                    info("should have clicked");
                    info("cps " + cps.get());
                }
            }
        }
    }
}
