package kassuk.addon.blackout.modules.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BB {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public BB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
    public BB(Box box) {
        this.minX = box.minX;
        this.minY = box.minY;
        this.minZ = box.minZ;
        this.maxX = box.maxX;
        this.maxY = box.maxY;
        this.maxZ = box.maxZ;
    }
    public boolean intersects(BB bb) {
        return ((bb.maxX > minX && bb.maxX < maxX) || (bb.minX > minX && bb.minX < maxX)) &&
            ((bb.maxZ > minZ && bb.maxZ < maxZ) || (bb.minZ > minZ && bb.minZ < maxZ)) &&
            ((bb.maxY > minY && bb.maxY < maxY) || (bb.minY > minY && bb.minY < maxY)) ||
            ((maxX > bb.minX && maxX < bb.maxX) || (minX > bb.minX && minX < bb.maxX)) &&
                ((maxZ > bb.minZ && maxZ < bb.maxZ) || (minZ > bb.minZ && minZ < bb.maxZ)) &&
                ((maxY > bb.minY && maxY < bb.maxY) || (minY > bb.minY && minY < bb.maxY));
    }
    public boolean intersects(Box bb) {
        return ((bb.maxX > minX && bb.maxX < maxX) || (bb.minX > minX && bb.minX < maxX)) &&
            ((bb.maxZ > minZ && bb.maxZ < maxZ) || (bb.minZ > minZ && bb.minZ < maxZ)) &&
            ((bb.maxY >= minY && bb.maxY <= maxY) || (bb.minY >= minY && bb.minY <= maxY)) ||
            ((maxX >= bb.minX && maxX <= bb.maxX) || (minX >= bb.minX && minX <= bb.maxX)) &&
                ((maxZ > bb.minZ && maxZ < bb.maxZ) || (minZ > bb.minZ && minZ < bb.maxZ)) &&
                ((maxY >= bb.minY && maxY <= bb.maxY) || (minY >= bb.minY && minY <= bb.maxY));
    }
    public boolean intersectsWithEntity(Predicate<Entity> predicate) {
        if (mc.world == null) {return false;}
        for (Entity en : mc.world.getEntities()) {
            if (predicate.test(en) && intersects(en.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }
}
