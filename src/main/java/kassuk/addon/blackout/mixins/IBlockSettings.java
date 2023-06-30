package kassuk.addon.blackout.mixins;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.Settings.class)
public interface IBlockSettings {
    @Accessor("replaceable")
    boolean replaceable();
}

