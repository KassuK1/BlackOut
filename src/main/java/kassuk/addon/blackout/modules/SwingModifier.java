package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

/**
 * @author OLEPOSSU
 */

public class SwingModifier extends BlackOutModule {
    public SwingModifier() {
        super(BlackOut.BLACKOUT, "Swing Modifier", "Modifies swing rendering.");
    }

    private final SettingGroup sgMainHand = settings.createGroup("Main Hand");
    private final SettingGroup sgOffHand = settings.createGroup("Off Hand");

    //--------------------Main-Hand--------------------//
    private final Setting<Double> mSpeed = sgMainHand.add(new DoubleSetting.Builder()
        .name("Main Speed")
        .description("Speed of swinging.")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> mStart = sgMainHand.add(new DoubleSetting.Builder()
        .name("Main Start Progress")
        .description("Starts swing at this progress.")
        .defaultValue(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> mEnd = sgMainHand.add(new DoubleSetting.Builder()
        .name("Main End Progress")
        .description("Swings until reaching this progress.")
        .defaultValue(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> myStart = sgMainHand.add(new DoubleSetting.Builder()
        .name("Main Start Y")
        .description("Hand Y value in the beginning.")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .build()
    );
    private final Setting<Double> myEnd = sgMainHand.add(new DoubleSetting.Builder()
        .name("Main End Y")
        .description("Hand Y value in the end.")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .build()
    );
    private final Setting<Boolean> mReset = sgMainHand.add(new BoolSetting.Builder()
        .name("Reset")
        .description("Resets swing when swinging again.")
        .defaultValue(false)
        .build()
    );

    //--------------------Off-Hand--------------------//
    private final Setting<Double> oSpeed = sgOffHand.add(new DoubleSetting.Builder()
        .name("Off Speed")
        .description("Speed of swinging for offhand")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> oStart = sgOffHand.add(new DoubleSetting.Builder()
        .name("Off Start Progress")
        .description("Starts swing at this progress.")
        .defaultValue(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> oEnd = sgOffHand.add(new DoubleSetting.Builder()
        .name("Off End Progress")
        .description("Swings until reaching this progress.")
        .defaultValue(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Double> oyStart = sgOffHand.add(new DoubleSetting.Builder()
        .name("Off Start Y")
        .description("Start Y value for offhand.")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .build()
    );
    private final Setting<Double> oyEnd = sgOffHand.add(new DoubleSetting.Builder()
        .name("Off End Y")
        .description("End Y value for offhand.")
        .defaultValue(0)
        .sliderRange(-10, 10)
        .build()
    );
    private final Setting<Boolean> oReset = sgOffHand.add(new BoolSetting.Builder()
        .name("Reset")
        .description("Resets swing when swinging again.")
        .defaultValue(false)
        .build()
    );

    private static boolean mainSwinging = false;
    private float mainProgress = 0;

    private boolean offSwinging = false;
    private float offProgress = 0;

    public void startSwing(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            if (mReset.get() || !mainSwinging) {
                mainProgress = 0;
                mainSwinging = true;
            }
        } else {
            if (oReset.get() || !offSwinging) {
                offProgress = 0;
                offSwinging = true;
            }
        }
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (mainSwinging) {
            if (mainProgress >= 1) {
                mainSwinging = false;
                mainProgress = 0;
            } else {
                mainProgress += event.frameTime * mSpeed.get();
            }
        }

        if (offSwinging) {
            if (offProgress >= 1) {
                offSwinging = false;
                offProgress = 0;
            } else {
                offProgress += event.frameTime * oSpeed.get();
            }
        }
    }

    public float getSwing(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return (float) (mStart.get() + (mEnd.get() - mStart.get()) * mainProgress);
        }
        return (float) (oStart.get() + (oEnd.get() - oStart.get()) * offProgress);
    }

    public float getY(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return (float) (myStart.get() + (myEnd.get() - myStart.get()) * mainProgress) / -10f;
        }
        return (float) (oyStart.get() + (oyEnd.get() - oyStart.get()) * offProgress) / -10f;
    }
}
