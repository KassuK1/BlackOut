package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

public class SwingModifier extends Module {
    public SwingModifier() {super(BlackOut.BLACKOUT, "Swing Modifier", "Modifies swing rendering");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Speed of swinging")
        .defaultValue(0.1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> start = sgGeneral.add(new DoubleSetting.Builder()
        .name("Start Progress")
        .description(".")
        .defaultValue(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> end = sgGeneral.add(new DoubleSetting.Builder()
        .name("End Progress")
        .description(".")
        .defaultValue(1)
        .sliderMax(10)
        .build()
    );

    public static boolean mainSwinging = false;
    public float mainProgress = 0;

    public boolean offSwinging = false;
    public float offProgress = 0;

    public void startSwing(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            if (!mainSwinging) {
                mainProgress = 0;
                mainSwinging = true;
            }
        } else {
            if (!offSwinging) {
                offProgress = 0;
                offSwinging = true;
            }
        }
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (mainSwinging) {
            mainProgress += event.frameTime * speed.get();
            if (mainProgress >= 1) {
                mainSwinging = false;
                mainProgress = 0;
            }
        }

        if (offSwinging) {
            offProgress += event.frameTime * speed.get();
            if (offProgress >= 1) {
                offSwinging = false;
                offProgress = 0;
            }
        }
    }

    public float getSwing(Hand hand) {
        return (float) (start.get() + (end.get() - start.get()) * (hand == Hand.MAIN_HAND ? mainProgress : offProgress) );
    }
}
