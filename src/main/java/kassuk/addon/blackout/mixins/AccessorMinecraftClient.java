package kassuk.addon.blackout.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface AccessorMinecraftClient {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);
}
