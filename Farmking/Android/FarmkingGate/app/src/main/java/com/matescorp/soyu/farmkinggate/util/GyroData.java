package com.matescorp.soyu.farmkinggate.util;

/**
 * Created by soyu on 17. 10. 10.
 */

public class GyroData {
    private float x;
    private float y;
    private float z;

    public GyroData() {
        x = 0;
        y = 0;
        z = 0;
    }


    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
