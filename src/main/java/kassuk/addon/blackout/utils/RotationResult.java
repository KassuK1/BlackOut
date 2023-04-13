package kassuk.addon.blackout.utils;

import net.minecraft.util.math.Box;

public class RotationResult {
    public static Box lastBox = null;
    public final boolean valid;
    public final long id;
    public final Box box;
    public RotationResult(boolean valid, Box box, long id) {
        this.valid = valid;
        this.box = box;

        this.id = id;
    }
}
