/*
*   This file is a part the best minecraft mod called Blackout Client (https://github.com/KassuK1/Blackout-Client)
*   and licensed under the GNU GENERAL PUBLIC LICENSE (check LICENCE file or https://www.gnu.org/licenses/gpl-3.0.html)
*   Copyright (C) 2024 KassuK and OLEPOSSU
*/

package kassuk.addon.blackout.mixins;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author OLEPOSSU
 */

@Mixin(NbtCompound.class)
public interface AccessorNbtCompound {
    @Accessor("entries")
    Map<String, NbtElement> getEntries();
}
