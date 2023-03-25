package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.enums.HoleType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class Hole {
    public final BlockPos pos;
    public final HoleType type;

    public Hole(BlockPos pos, HoleType type) {
        this.pos = pos;
        this.type = type;
    }

    public BlockPos[] positions() {
        switch (type) {
            case Single -> {return new BlockPos[]{pos};}
            case DoubleX -> {return new BlockPos[]{pos, pos.add(1, 0, 0)};}
            case DoubleZ -> {return new BlockPos[]{pos, pos.add(0, 0, 1)};}
            case Quad -> {return new BlockPos[]{pos, pos.add(1, 0, 0), pos.add(0, 0, 1), pos.add(1, 0, 1)};}
            default -> {return null;}
        }
    }
}
