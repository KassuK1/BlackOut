package kassuk.addon.blackout.mixins;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RaycastContext.class)
public interface IRaycastContext {
    @Mutable
    @Accessor("start")
    void setStart(Vec3d start);

    @Mutable
    @Accessor("end")
    void setEnd(Vec3d end);
}
