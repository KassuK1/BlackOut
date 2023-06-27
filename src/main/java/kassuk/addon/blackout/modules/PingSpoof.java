package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.enums.SwingHand;
import kassuk.addon.blackout.globalsettings.SwingSettings;
import kassuk.addon.blackout.mixins.MixinInteractEntityC2SPacket;
import kassuk.addon.blackout.mixins.MixinSpectatorTeleportC2SPacket;
import kassuk.addon.blackout.utils.SettingUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.TorchBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author OLEPOSSU
 */

public class PingSpoof extends BlackOutModule {
    public PingSpoof() {
        super(BlackOut.BLACKOUT, "Ping Spoof", "Increases your ping.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder()
        .name("Bonus Ping")
        .description("Increases your ping by this much.")
        .defaultValue(69)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );
}
