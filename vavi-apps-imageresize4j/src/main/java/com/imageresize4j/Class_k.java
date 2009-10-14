package com.imageresize4j;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;


final class Class_k {

    Class_k() {
    }

    static float[] sub_40e(ImageResizeProcessor.Contributor contributor, int i, byte abyte0[], int ai[]) {
        float af[] = new float[ai.length];
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            float f = aContributor.var_fc;
            int j = aContributor.var_e4 + i;
            for (int k = 0; k < af.length; k++) {
                af[k] += (abyte0[j + ai[k]] & 0xff) * f;
            }
        }

        return af;
    }

    static float[] getRgb(ImageResizeProcessor.Contributor contributor, int x, int y, byte abyte0[], float reds[], float green[], float blues[], int k) {
        float rgbs[] = new float[4];
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            int l = abyte0[aContributor.var_e4 * x + y];
            if ((l & 0xff) != k) {
                rgbs[0] += reds[l] * aContributor.var_fc;
                rgbs[1] += green[l] * aContributor.var_fc;
                rgbs[2] += blues[l] * aContributor.var_fc;
                rgbs[3] += 255F * aContributor.var_fc;
            } else {
                rgbs[0] += 255F * aContributor.var_fc;
                rgbs[1] += 255F * aContributor.var_fc;
                rgbs[2] += 255F * aContributor.var_fc;
                rgbs[3] += 255F * aContributor.var_fc;
            }
        }

        return rgbs;
    }

    static float[] sub_552(Filter filter, ImageResizeProcessor.Contributor contributor, int i, int j, float af[][]) {
        float af1[] = new float[af.length];
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            int k = aContributor.var_e4;
            float f = aContributor.var_fc;
            for (int l = 0; l < af.length; l++) {
                af1[l] += filter.sub_426(i, j, af[l][k]) * f;
            }
        }

        return af1;
    }

    private static float sub_5c3(ImageResizeProcessor.Contributor contributor, ColorModel colorModel, WritableRaster raster, int i, int j) {
        float f = 0.0F;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            int ai[] = colorModel.getComponents(raster.getSample(i, aContributor.var_e4, 0), null, 0);
            f += ai[j] * aContributor.var_fc;
        }

        return f;
    }

    static double sub_61c(ImageResizeProcessor.Contributor contributor, ColorModel colormodel, WritableRaster writableraster, int i, int j) {
        double d = 0.0D;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            int ai[] = colormodel.getComponents(writableraster.getSample(i, aContributor.var_e4, 0), null, 0);
            d += ai[j] * aContributor.var_fc;
        }

        return d;
    }

    static float sub_676(ImageResizeProcessor.Contributor contributor, WritableRaster raster, int i, int j) {
        float f = 0.0F;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            f += raster.getSample(i, aContributor.var_e4, j) * aContributor.var_fc;
        }
        return f;
    }

    static double sub_6c2(ImageResizeProcessor.Contributor contributor, WritableRaster raster, int i, int j) {
        double d = 0.0D;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            d += raster.getSample(i, aContributor.var_e4, j) * aContributor.var_fc;
        }
        return d;
    }

    static float sub_70f(Filter filter, ImageResizeProcessor.Contributor contributor, int i, int j, float af[]) {
        float f = 0.0F;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            f += filter.sub_426(i, j, af[aContributor.var_e4]) * aContributor.var_fc;
        }
        return f;
    }

    static double sub_75d(Filter filter, ImageResizeProcessor.Contributor contributor, int i, int j, double ad[]) {
        double d = 0.0D;
        for (ImageResizeProcessor.Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
            d += filter.sub_442(i, j, ad[aContributor.var_e4]) * aContributor.var_fc;
        }
        return d;
    }
}
