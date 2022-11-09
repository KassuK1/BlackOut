package com.example.addon.modules.anarchy;

import com.example.addon.BlackOut;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.starscript.Script;

import java.util.ArrayList;
import java.util.List;

/*
Made by OLEPOSSU / Raksamies
*/

public class RPC extends Module {
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
        .description("Delay of refreshing the rpc (Ticks)")
        .defaultValue(100)
        .range(0, 1000)
        .sliderRange(0, 1000)
        .build()
    );
    private int ticks = 0;
    private int index1 = 0;
    private int index2 = 0;
    private static final RichPresence presence = new RichPresence();

    public RPC() {
        super(BlackOut.ANARCHY, "RPC", "Epic rpc");
    }

    @Override
    public void onActivate() {
        DiscordIPC.start(1038168991258136576L, null);
        presence.setStart(System.currentTimeMillis() / 1000L);
        UpdatePresence();
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
            UpdatePresence();
        }
    }

    public void UpdatePresence() {
        ticks = refreshDelay.get();
        index1 = index1 < l1.get().size() - 1 ? index1 + 1 : 0;
        index2 = index2 < l2.get().size() - 1 ? index2 + 1 : 0;
        presence.setState(mc.player == null ? "In Main Menu" : getMessages(l2.get()).get(index2));
        presence.setDetails(mc.player == null ? "In Main Menu" : getMessages(l1.get()).get(index1));
        presence.setLargeImage("blackout", "Very gud");
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
