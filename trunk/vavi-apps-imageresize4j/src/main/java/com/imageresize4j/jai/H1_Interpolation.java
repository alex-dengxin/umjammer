package com.imageresize4j.jai;

import javax.media.jai.Interpolation;


final class H1_Interpolation extends Interpolation {

    public H1_Interpolation() {
        super(0, 0, 0, 0, 0, 0, 32, 32);
    }

    public final int interpolateH(int ai[], int i) {
        return 0;
    }

    public final float interpolateH(float af[], float f) {
        return 0.0F;
    }

    public final double interpolateH(double ad[], float f) {
        return 0.0D;
    }
}
