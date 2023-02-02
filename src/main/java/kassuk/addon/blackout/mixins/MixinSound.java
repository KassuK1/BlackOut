package kassuk.addon.blackout.mixins;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface MixinSound {
    @Mutable
    @Accessor("id")
    void setID(Identifier id);

    @Mutable
    @Accessor("volume")
    void setVolume(float volume);

    @Mutable
    @Accessor("pitch")
    void setPitch(float pitch);
}
