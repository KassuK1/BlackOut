package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.ForceSneak;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract boolean isInPose(EntityPose pose);

    @Inject(method = "isInSneakingPose", at = @At(value = "RETURN"), cancellable = true)
    private void isSneaking(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Modules.get().get(ForceSneak.class).isActive() || this.isInPose(EntityPose.CROUCHING));
    }
}
