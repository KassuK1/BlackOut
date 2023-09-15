package kassuk.addon.blackout.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface AccessorMinecraftClient {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);
}
