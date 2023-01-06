package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.Fog;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        Fog fog = Modules.get().get(Fog.class);
        if (fog.isActive()) {info.setReturnValue(fog.color.get().getVec3d());}
    }
}
