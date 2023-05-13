package kassuk.addon.blackout;

import kassuk.addon.blackout.utils.PriorityUtils;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class BlackOutModule extends Module {
    private final String prefix = Formatting.DARK_RED + "[BlackOut]";
    public int priority;

    public BlackOutModule(Category category, String name, String description) {
        super(category, name, description);
        this.priority = PriorityUtils.get(this);
    }

    //  Messages
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + (isActive() ? Formatting.GREEN + " ON" : Formatting.RED + " OFF");
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + (isActive() ? Formatting.GREEN + " ON " : Formatting.RED + " OFF ") + Formatting.GRAY + message;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + Formatting.RED + " OFF " + Formatting.GRAY + text;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendBOInfo(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + text;
            sendMessage(Text.of(msg), Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + Formatting.AQUA + text;
            sendMessage(Text.of(msg), 0);
        }
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).add(text, id);
    }

    //  Packets
    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) {return;}
        mc.getNetworkHandler().sendPacket(packet);
    }

    //  Settings
    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }
}
