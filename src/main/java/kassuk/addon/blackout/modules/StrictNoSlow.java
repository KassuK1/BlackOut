package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.managers.Managers;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

/**
 * @author OLEPOSSU
 */

public class StrictNoSlow extends BlackOutModule {
    public StrictNoSlow() {
        super(BlackOut.BLACKOUT, "Strict No Slow", "Should only be used on very strict servers. Requires any other noslow to work.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> onlyGap = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Gapples")
        .description("Only sends packets when eating gapples.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> single = sgGeneral.add(new BoolSetting.Builder()
        .name("Single Packet")
        .description("Only sends 1 switch packet after starting to eat. Works on most servers that require this module.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Tick delay between switch packets.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 20)
        .visible(() -> !single.get())
        .build()
    );

    private int timer = 0;

    @EventHandler
    private void onSend(PacketEvent.Sent event) {
        if (mc.player != null && event.packet instanceof PlayerInteractItemC2SPacket packet) {
            if (shouldSend(packet.getHand() == Hand.MAIN_HAND ? Managers.HOLDING.getStack() : mc.player.getOffHandStack())) {
                send();
                timer = 0;
            }
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        timer++;
        if (timer > delay.get() && !single.get()) {
            send();
            timer = 0;
        }
    }

    private void send() {
        sendPacket(new UpdateSelectedSlotC2SPacket(Managers.HOLDING.slot));
    }

    private boolean shouldSend(ItemStack stack) {
        return mc.player != null && (onlyGap.get() || (stack != null && !stack.isEmpty() && stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE || stack.getItem() == Items.GOLDEN_APPLE));
    }
}
