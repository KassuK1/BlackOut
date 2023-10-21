package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.AccessorMinecraftClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;

/**
 * @author KassuK
 */

public class FastXP extends BlackOutModule {
    public FastXP() {
        super(BlackOut.BLACKOUT, "Fast XP", "XP spamming moment.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> yeetDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Throw Delay")
        .description("Delay between throws.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Should we do a bit of rotating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<RotationMode> rotMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("Rotation mode")
        .description("ken i put mi balls in yo jawzz")
        .defaultValue(RotationMode.Silent)
        .visible(rotate::get)
        .build()
    );
    private final Setting<Integer> pitch = sgGeneral.add(new IntSetting.Builder()
        .name("Pitch")
        .description("Where to set pitch.")
        .defaultValue(90)
        .range(-90, 90)
        .sliderMax(90)
        .visible(rotate::get)
        .build()
    );


    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE  && mc.options.useKey.isPressed()){
            ((AccessorMinecraftClient) mc).setItemUseCooldown(yeetDelay.get());

            if (rotMode.get() == RotationMode.Silent && rotate.get())
                Rotations.rotate(mc.player.getYaw(), pitch.get());

            if (rotMode.get() == RotationMode.Vanilla && rotate.get())
                mc.player.setPitch(pitch.get());
        }
    }

    public enum RotationMode {
        Silent,
        Vanilla,
    }
}
