package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.Fog;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackground {
    @Inject(method = "applyFog", at = @At("TAIL"))
    private static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        Fog fog = Modules.get().get(Fog.class);
        if (fog != null && fog.isActive() && fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
            fog.modifyFog();
        }
    }
}
