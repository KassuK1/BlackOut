package kassuk.addon.blackout.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BlackoutGit extends Command {
    public BlackoutGit() {
        super("BlackoutInfo", "Gives the Blackout GitHub");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info("https://github.com/KassuK1/BlackOut");
            return SINGLE_SUCCESS;
        });
    }
}
