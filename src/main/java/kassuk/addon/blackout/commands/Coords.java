package kassuk.addon.blackout.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author KassuK
 */

@SuppressWarnings("SpellCheckingInspection")
public class Coords extends Command {

    public Coords() {
        super("coords", "Copies your coordinates to your clipboard.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player != null) {
                String text = "x: " + Math.floor(mc.player.getX()) + "; y:" + Math.floor(mc.player.getY()) + "; z:" + Math.floor(mc.player.getZ()) + ";";
                info("Succesfully copied your coordinates: \n" + text);
                mc.keyboard.setClipboard(text);
            }
            return SINGLE_SUCCESS;
        });
    }
}
