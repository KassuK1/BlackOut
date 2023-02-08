package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.SwingModifier;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"))
    private void swingHand(Hand hand, CallbackInfo ci) {
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }
}
