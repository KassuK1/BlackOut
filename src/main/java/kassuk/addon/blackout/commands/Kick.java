package kassuk.addon.blackout.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;


public class Kick extends Command {
    public Kick() {
        super("kick", "Kicks you from the server");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player != null && mc.world != null)
                mc.world.disconnect();
            return SINGLE_SUCCESS;
        });
        }
    }

