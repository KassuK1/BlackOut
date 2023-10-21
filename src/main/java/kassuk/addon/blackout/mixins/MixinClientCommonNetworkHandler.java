package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class MixinClientCommonNetworkHandler {
    @Inject(method = "onKeepAlive", at = @At("HEAD"), cancellable = true)
    private void keepAlive(KeepAliveS2CPacket packet, CallbackInfo ci) {
        if (!Modules.get().isActive(PingSpoof.class) || !Modules.get().get(PingSpoof.class).keepAlive.get()) return;

        ci.cancel();
        Managers.PING_SPOOF.addKeepAlive(packet.getId());
    }

    @Inject(method = "onPing", at = @At("HEAD"), cancellable = true)
    private void pong(CommonPingS2CPacket packet, CallbackInfo ci) {
        if (!Modules.get().isActive(PingSpoof.class) || !Modules.get().get(PingSpoof.class).pong.get()) return;

        ci.cancel();
        Managers.PING_SPOOF.addPong(packet.getParameter());
    }
}
