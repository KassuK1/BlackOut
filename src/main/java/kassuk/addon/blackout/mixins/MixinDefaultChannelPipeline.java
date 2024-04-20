package kassuk.addon.blackout.mixins;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPipeline;
import kassuk.addon.blackout.modules.PacketLogger;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultChannelPipeline.class)
public class MixinDefaultChannelPipeline {
    @Unique private PacketLogger packetLogger = null;

    @Inject(method = "write(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", at = @At("HEAD"))
    private void writePacket(Object msg, CallbackInfoReturnable<ChannelFuture> cir) {
        if (packetLogger == null) packetLogger = Modules.get().get(PacketLogger.class);
        else if (msg instanceof Packet<?> packet && packetLogger.isActive())
            packetLogger.onSent(packet);
    }
}
