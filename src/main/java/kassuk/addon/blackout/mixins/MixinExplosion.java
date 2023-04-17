package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.SoundModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class MixinExplosion {
    @Redirect(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private void redirect(World instance, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        SoundModifier m = Modules.get().get(SoundModifier.class);

        if (m.isActive()) {
            if (m.expSound.get()) {
                instance.playSound(x, y, z, sound, category, (float) (volume * m.explosionVolume.get()), (float) (pitch * m.explosionPitch.get()), useDistance);
            }
            return;
        }
        instance.playSound(x, y, z, sound, category, volume, pitch, useDistance);
    }
}
