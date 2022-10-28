package com.example.addon.modules.utils;

import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.math.Vec3d;

public class OLEPOSSUtils extends Utils {
    public static double distance(Vec3d v1, Vec3d v2) {
        double dX = Math.abs(v1.x - v2.x);
        double dY = Math.abs(v1.y - v2.y);
        double dZ = Math.abs(v1.z - v2.z);
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }
}
