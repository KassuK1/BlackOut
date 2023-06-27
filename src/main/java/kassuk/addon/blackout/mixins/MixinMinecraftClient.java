package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.AutoMine;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Redirect(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;attackBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean attackBlock(ClientPlayerInteractionManager instance, BlockPos pos, Direction direction) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) return instance.attackBlock(pos, direction);
        autoMine.handleAttack(pos);
        return true;
    }
}
