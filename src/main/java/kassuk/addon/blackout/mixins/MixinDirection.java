package kassuk.addon.blackout.mixins;

import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Direction.class)
public interface MixinDirection {
    @Accessor("ALL")
    static Direction[] getAll() {
        throw new AssertionError();
    }

    @Accessor("HORIZONTAL")
    static Direction[] getHorizontal() {
        throw new AssertionError();
    }
}
