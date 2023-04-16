package kassuk.addon.blackout;

import kassuk.addon.blackout.utils.PriorityUtils;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class BlackOutModule extends Module {

    public BlackOutModule(Category category, String name, String description) {
        super(category, name, description);
        this.priority = PriorityUtils.get(this);
    }

    public int priority;
    String prefix = Formatting.DARK_RED + "[BlackOut]";

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + (isActive() ? Formatting.GREEN + " ON" : Formatting.RED + " OFF");
            Text message = Text.of(msg);
            ((IChatHud) mc.inGameHud.getChatHud()).add(message, hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + Formatting.RED + " OFF " + Formatting.GRAY + text;
            Text message = Text.of(msg);
            ((IChatHud) mc.inGameHud.getChatHud()).add(message, hashCode());
        }
    }

    public void sendBOInfo(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + text;
            Text message = Text.of(msg);
            ((IChatHud) mc.inGameHud.getChatHud()).add(message, Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + Formatting.AQUA + text;
            Text message = Text.of(msg);
            ((IChatHud) mc.inGameHud.getChatHud()).add(message, 0);
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) {return;}
        mc.getNetworkHandler().sendPacket(packet);
    }
}
