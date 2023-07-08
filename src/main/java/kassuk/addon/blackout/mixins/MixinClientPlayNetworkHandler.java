package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import kassuk.addon.blackout.modules.PingSpoof;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onKeepAlive", at = @At("HEAD"), cancellable = true)
    private void keepAlive(KeepAliveS2CPacket packet, CallbackInfo ci) {
        if (!Modules.get().isActive(PingSpoof.class)) return;

        ci.cancel();
        Managers.KEEP_ALIVE.add(packet.getId());
    }
}
