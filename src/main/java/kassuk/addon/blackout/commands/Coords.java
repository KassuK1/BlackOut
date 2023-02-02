package kassuk.addon.blackout.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;


import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Coords extends Command {

    public Coords(){super("Coords","Pastes your coords to your clipboard");}

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player != null) {
                String text = Math.floor(mc.player.getX()) + " " + Math.floor(mc.player.getY()) + " " + Math.floor(mc.player.getZ());
                mc.keyboard.setClipboard(text);
            }
            //I could make this leak ur coords for a bit of trolling
            return SINGLE_SUCCESS;
        });
    }
}
