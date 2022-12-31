package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Random;

/*
Made by OLEPOSSU / Raksamies
*/

public class AutoEz extends Module {
    public AutoEz() {super(BlackOut.BLACKOUT, "AutoEZ", "Sends message after enemy dies(too EZ nn's)");}
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description(".")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("Messages")
        .description("Messages to send when killing enemy")
        .defaultValue(List.of("Fucked by BlackOut!", "BlackOut on top", "BlackOut strong", "BlackOut gayming","BlackOut on top"))
        .build()
    );
    Random r = new Random();
    int lastNum;
    boolean lastState;

    @Override
    public void onActivate() {
        super.onActivate();
        lastState = false;
        lastNum = -1;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
            if (anyDead(range.get())) {
                if (!lastState) {
                    lastState = true;
                    sendMessage();
                }
            } else {
                lastState = false;
            }
        }
    }

    private boolean anyDead(double range) {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !Friends.get().isFriend(pl) && OLEPOSSUtils.distance(pl.getPos(), mc.player.getPos()) < range) {
                if (pl.getHealth() <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendMessage() {
        int num = r.nextInt(0, messages.get().size() - 1);
        if (num == lastNum) {
            num = num < messages.get().size() - 1 ? num + 1 : 0;
        }
        lastNum = num;
        ChatUtils.sendPlayerMsg(messages.get().get(num));
    }
}
