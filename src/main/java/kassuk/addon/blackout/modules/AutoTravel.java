package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

public class AutoTravel extends Module {
    public AutoTravel() {super(BlackOut.BLACKOUT, "AutoTravel", "Basically autistic baritone");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> sprint = sgGeneral.add(new BoolSetting.Builder()
        .name("Sprint")
        .description("Should we switch be sprinting or not")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> healthlog = sgGeneral.add(new BoolSetting.Builder()
        .name("HP log")
        .description("Should we disconnect when were low")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> possibledeath = sgGeneral.add(new BoolSetting.Builder()
        .name("Log on possible death")
        .description("Should we disconnect if its possible for us to die")
        .defaultValue(true)
        .visible(healthlog::get)
        .build()
    );
    private final Setting<Double> health = sgGeneral.add(new DoubleSetting.Builder()
        .name("Disconnect health")
        .description("At what point to disconnect")
        .defaultValue(16)
        .range(20, 1)
        .sliderMin(1)
        .visible(healthlog::get)
        .sliderMax(20)
        .build()
    );
    @Override
    public void onDeactivate() {
    mc.options.forwardKey.setPressed(false);
    mc.options.jumpKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            mc.player.setSprinting(sprint.get());
            mc.options.forwardKey.setPressed(true);
            if (mc.player.isTouchingWater()){
                mc.options.jumpKey.setPressed(true);
            }
            if (!mc.player.isTouchingWater()){
                mc.options.jumpKey.setPressed(false);
            }

            if (healthlog.get()){
                if (PlayerUtils.getTotalHealth() <= PlayerUtils.possibleHealthReductions() && possibledeath.get() || PlayerUtils.getTotalHealth() <= health.get()){
                    mc.world.disconnect();
                    this.toggle();
                }
            }

            if (mc.player.isOnGround()) {
                double x = (mc.player.getX() - mc.player.prevX);
                double z = (mc.player.getZ() - mc.player.prevZ);
                if (Math.sqrt(x * x + z * z) >= 0.1 && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(x, 0, z)).iterator().hasNext()) {
                    mc.player.jump();
                }
            }
        }
    }
}
