package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class JumpModify extends BlackOutModule {
    public JumpModify() {super(BlackOut.BLACKOUT, "JumpModify", "Allows you to modify jumping");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> jumpForce = sgGeneral.add(new DoubleSetting.Builder()
        .name("JumpForce")
        .description("How hard to jump")
        .defaultValue(0.42)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder()
        .name("AutoJump")
        .description("Should we automatically jump when were on ground")
        .defaultValue(false)
        .build()
    );
    @EventHandler
    private void onMove(PlayerMoveEvent event){
        if (mc.player != null && mc.world != null){
            if (mc.options.jumpKey.isPressed() && mc.player.isOnGround() || autoJump.get() && mc.player.isOnGround())
                ((IVec3d) event.movement).set(((Vec3d) event.movement).getX(), jumpForce.get(), ((Vec3d) event.movement).getZ());
        }
    }
}
