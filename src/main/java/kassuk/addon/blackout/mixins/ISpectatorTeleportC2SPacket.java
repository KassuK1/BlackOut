package kassuk.addon.blackout.mixins;

import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(SpectatorTeleportC2SPacket.class)
public interface ISpectatorTeleportC2SPacket {
    @Accessor("targetUuid")
    UUID getID();
}
