package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.SwingModifier;
import kassuk.addon.blackout.modules.TickShift;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    @Unique
    private static boolean sent = false;

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"))
    private void swingHand(Hand hand, CallbackInfo ci) {
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void sendPacketsHead(CallbackInfo ci) {
        sent = false;
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
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

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 2))
    private void sendPacketFull(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onFull((PlayerMoveC2SPacket.Full) packet));
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 3))
    private void sendPacketPosGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onPositionOnGround((PlayerMoveC2SPacket.PositionAndOnGround) packet));
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 4))
    private void sendPacketLookGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        PlayerMoveC2SPacket toSend = Managers.ROTATION.onLookAndOnGround((PlayerMoveC2SPacket.LookAndOnGround) packet);
        if (toSend != null) {
            networkHandler.sendPacket(toSend);
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 5))
    private void sendPacketGround(ClientPlayNetworkHandler instance, Packet<?> packet) {
        networkHandler.sendPacket(Managers.ROTATION.onOnlyOnground((PlayerMoveC2SPacket.OnGroundOnly) packet));
    }
}
