package kassuk.addon.blackout.mixins;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.Settings.class)
public interface MixinBlockSettings {

    @Accessor("material")
    Material getMaterial();

}

