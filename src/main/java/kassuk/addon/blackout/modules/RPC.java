package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.starscript.Script;

import java.util.ArrayList;
import java.util.List;

/**
 * @author OLEPOSSU
 */

public class RPC extends BlackOutModule {
    public RPC() {
        super(BlackOut.BLACKOUT, "RPC", "Epic rpc.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> l1 = sgGeneral.add(new StringListSetting.Builder()
        .name("Line 1")
        .description(".")
        .defaultValue("Playing on {server}", "{player}")
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );
    private final Setting<List<String>> l2 = sgGeneral.add(new StringListSetting.Builder()
        .name("Line 2")
        .description(".")
        .defaultValue("{server.player_count} Players online", "{round(player.health, 1)}hp")
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );
    private final Setting<Integer> refreshDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Refresh Delay")
        .description("Ticks between refreshing.")
        .defaultValue(100)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .build()
    );

    private int ticks = 0;
    private int index1 = 0;
    private int index2 = 0;
    private static final RichPresence presence = new RichPresence();

    @Override
    public void onActivate() {
        DiscordIPC.start(1038168991258136576L, null);
        presence.setStart(System.currentTimeMillis() / 1000L);
        updatePresence();
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.Pre event) {
        if (ticks > 0) {
            ticks--;
        } else {
            updatePresence();
        }
    }

    public void updatePresence() {
        ticks = refreshDelay.get();
        List<String> messages1 = getMessages(l1.get());
        List<String> messages2 = getMessages(l2.get());
        index1 = index1 < messages1.size() - 1 ? index1 + 1 : 0;
        index2 = index2 < messages2.size() - 1 ? index2 + 1 : 0;
        presence.setDetails(mc.player == null ? "In Main Menu" : messages1.get(index1));
        presence.setState(mc.player == null ? "In Main Menu" : messages2.get(index2));
        presence.setLargeImage("logo1", "v." + BlackOut.BLACKOUT_VERSION);
        DiscordIPC.setActivity(presence);
    }

    private List<String> getMessages(List<String> stateList) {
        List<String> messages = new ArrayList<>();
        for (String msg : stateList) {
            Script script = MeteorStarscript.compile(msg);
            if (script != null) {
                messages.add(MeteorStarscript.run(script));
            }
        }
        return messages;
    }
}
