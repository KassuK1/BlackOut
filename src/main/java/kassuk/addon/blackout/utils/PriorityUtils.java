package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.modules.*;

public class PriorityUtils {
    // Tell me a better way to do this pls
    public static int get(Object module) {
        if (module instanceof AntiAim) {return 12;}
        if (module instanceof AnchorAuraPlus) {return 9;}
        if (module instanceof AutoCraftingTable) {return 4;}
        if (module instanceof AutoCrystalRewrite) {return 10;}
        if (module instanceof AutoMend) {return 4;}
        if (module instanceof AutoMine) {return 9;}
        if (module instanceof AutoPearl) {return 6;}
        if (module instanceof AutoTrapPlus) {return 5;}
        if (module instanceof BedAuraPlus) {return 8;}
        if (module instanceof ButtonAura) {return 11;}
        if (module instanceof FastXP) {return 3;}
        if (module instanceof HoleFillRewrite) {return 7;}
        if (module instanceof ScaffoldPlus) {return 2;}
        if (module instanceof SelfTrapPlus) {return 1;}
        if (module instanceof SurroundPlus) {return 0;}

        return 100;
    }
}
