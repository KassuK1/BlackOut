package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;


//Made by KassuK
public class AnteroTaateli extends BlackOutModule {
    public AnteroTaateli() {super(BlackOut.BLACKOUT, "AutoAndrewTate", "What colour is your bugatti?");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> iFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description("Doesn't send messages when there is only friends nearby.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Tick delay between messages.")
        .defaultValue(10)
        .range(0, 60)
        .sliderRange(0, 60)
        .build()
    );

    double timer = 0;
    String[] messages = new String[] {
        "Hey brokies top G here",
        "Top G eats raw meat and breathes air",
        "I hate dead people all you do is fucking laying down like pussies",
        "Get up and do some push-ups",
        "Top G is never late time is just running ahead of schedule",
        "<NAME>, what color is your Bugatti?"
    };
    Random r = new Random();

    @EventHandler
    private void onRender(Render3DEvent event){
        timer = Math.min(delay.get(), timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (mc.player != null && mc.world != null){
            PlayerEntity bugatti = getClosest();
            if (timer >=delay.get() && bugatti != null){
                timer = 0;
                ChatUtils.sendPlayerMsg(getMessage(bugatti));
            }
        }
    }
    String getMessage(PlayerEntity pl) {
        int index = r.nextInt(0, messages.length);
        String msg = messages[index];
        return msg.replace("<NAME>", pl.getName().getString());
    }
    PlayerEntity getClosest() {
        PlayerEntity closest = null;
        float distance = -1;
        if (!mc.world.getPlayers().isEmpty()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && (!iFriends.get() || !Friends.get().isFriend(player))) {
                    if (closest == null || OLEPOSSUtils.distance(mc.player.getPos(), player.getPos()) < distance) {
                        closest = player;
                        distance = (float) OLEPOSSUtils.distance(mc.player.getPos(), player.getPos());
                    }
                }
            }
        }
        return closest;
    }
}
