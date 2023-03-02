package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.SwingModifier;
import kassuk.addon.blackout.modules.TickShift;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    private static boolean sent = false;
    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"))
    private void swingHand(Hand hand, CallbackInfo ci) {
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void sendPacketsHead(CallbackInfo ci) {
        sent = false;
    }
    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onSendPacket(CallbackInfo ci) {
        sent = true;
    }
    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void sendPacketsTail(CallbackInfo ci) {
        if (!sent) {
            TickShift tickShift = Modules.get().get(TickShift.class);
            if (tickShift.isActive()) {
                tickShift.unSent = Math.min(tickShift.packets.get(), tickShift.unSent + 1);
            }
        }
    }
}
