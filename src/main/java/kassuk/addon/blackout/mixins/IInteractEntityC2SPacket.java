package kassuk.addon.blackout.mixins;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface IInteractEntityC2SPacket {
    @Accessor("entityId")
    @Final
    @Mutable
    void setId(final int id);

    @Accessor("entityId")
    int getId();

    @Accessor("type")
    PlayerInteractEntityC2SPacket.InteractTypeHandler getType();
}

