package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.modules.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

//Made by KassuK
public class AnteroTaateli extends Module {
    public AnteroTaateli() {super(BlackOut.ANARCHY, "AutoAndrewTate", "What colour is your bugatti?");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> iFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay.")
        .defaultValue(20)
        .range(0, 60)
        .sliderRange(0, 60)
        .build()
    );

    double timer = 0;

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(delay.get(),timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            String bugatti = getClosest();
            if (timer >=delay.get() && bugatti != null){
                timer = 0;
                ChatUtils.sendPlayerMsg(bugatti + ", what colour is your Bugatti?");
            }
        }
    }
    private String getClosest() {
        String closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && (!iFriends.get() || !Friends.get().isFriend(player))) {
                    if (closest == null || OLEPOSSUtils.distance(mc.player.getPos(), player.getPos()) < distance) {
                        closest = player.getName().getString();
                        distance = (float) OLEPOSSUtils.distance(mc.player.getPos(), player.getPos());
                    }
                }
            }
        }
        return closest;
    }
}
