package com.imageresize4j.jai;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.Warp;
import javax.media.jai.WarpAffine;
import javax.media.jai.WarpOpImage;


public class ImprovedScaleWarpOpImage extends WarpOpImage {

    private static class Class_r {

        static float sub_2bb(Filter class_k, Contributor class_m, int i, int j, float af[]) {
            float f = 0.0F;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                f += class_k.sub_49e(i, j, af[class_m1.var_ec]) * class_m1.var_f4;

            return f;
        }

        static double sub_309(Filter class_k, Contributor class_m, int i, int j, double ad[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += class_k.sub_4ba(i, j, ad[class_m1.var_ec]) * class_m1.var_f4;

            return d;
        }

        static double sub_358(Contributor class_m, int i, int j, double ad[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += ad[class_m1.var_ec * i + j] * class_m1.var_f4;

            return d;
        }

        static double sub_3a4(Contributor class_m, int i, int j, float af[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += af[class_m1.var_ec * i + j] * class_m1.var_f4;

            return d;
        }

        static double sub_3f0(Contributor class_m, int i, int j, int ai[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += ai[class_m1.var_ec * i + j] * class_m1.var_f4;

            return d;
        }

        static double sub_43d(Contributor class_m, int i, int j, short aword0[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += aword0[class_m1.var_ec * i + j] * class_m1.var_f4;

            return d;
        }

        static double sub_48a(Contributor class_m, int i, int j, short aword0[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += (aword0[class_m1.var_ec * i + j] & 0xffff) * class_m1.var_f4;

            return d;
        }

        static double sub_4da(Contributor class_m, int i, int j, byte abyte0[]) {
            double d = 0.0D;
            for (Contributor class_m1 = class_m.next; class_m1 != null; class_m1 = class_m1.next)
                d += (abyte0[class_m1.var_ec * i + j] & 0xff) * class_m1.var_f4;

            return d;
        }

        private Class_r() {
        }
    }

    static class Contributor {

        Contributor next;

        int var_ec;

        float var_f4;

        Contributor() {
            next = null;
        }
    }

    private static Warp getWarp(float xScale, float yScale) {
        AffineTransform affinetransform = AffineTransform.getScaleInstance(1.0F / xScale, 1.0F / yScale);
        return new WarpAffine(affinetransform);
    }

    private static ImageLayout getImageLayout(ImageLayout imagelayout, RenderedImage renderedimage, float xScale, float yScale) {
        if (xScale <= 0.0D || yScale <= 0.0D)
            throw new IllegalArgumentException("Non-positive scale factor.");
        if (imagelayout != null && (imagelayout.getValidMask() & 0xf) != 0) {
            return imagelayout;
        } else {
            ImageLayout imagelayout1 = imagelayout != null ? (ImageLayout) imagelayout.clone() : new ImageLayout();
            imagelayout1.setMinX((int) Math.floor((double) renderedimage.getMinX() * (double) xScale));
            imagelayout1.setMinY((int) Math.floor((double) renderedimage.getMinY() * (double) yScale));
            imagelayout1.setWidth((int) Math.rint((double) renderedimage.getWidth() * (double) xScale));
            imagelayout1.setHeight((int) Math.rint((double) renderedimage.getHeight() * (double) yScale));
            return imagelayout1;
        }
    }

    ImprovedScaleWarpOpImage(RenderedImage renderedimage, ImageLayout imagelayout, Map<?, ?> map, BorderExtender borderextender, float f, float f1, ImprovedScaleInterpolation improvedscaleinterpolation) {
        super(renderedimage, getImageLayout(imagelayout, renderedimage, f, f1), map, true, borderextender, new H1_Interpolation(), getWarp(f, f1));
        filter = new Filter.Class_p();
        scaleX = f;
        scaleY = f1;
        computableBounds = getBounds();
        if (borderextender == null) {
            Rectangle rectangle = new Rectangle(renderedimage.getMinX(), renderedimage.getMinY(), renderedimage.getWidth(), renderedimage.getHeight());
            computableBounds = computableBounds.intersection(mapSourceRect(rectangle, 0));
        }
        interp = improvedscaleinterpolation;
    }

    protected void computeRect(Raster araster[], WritableRaster raster, Rectangle rectangle) {
        javax.media.jai.RasterFormatTag arasterformattag[] = getFormatTags();
        RasterAccessor rasterAccessor = new RasterAccessor(raster, rectangle, arasterformattag[1], getColorModel());
        Rectangle rectangle1 = mapDestRect(rectangle, 0).intersection(araster[0].getBounds());
        RasterAccessor rasteraccessor1 = new RasterAccessor(araster[0], rectangle1, arasterformattag[0], getSourceImage(0).getColorModel());
        switch (rasterAccessor.getDataType()) {
        case RasterAccessor.UNCOPIED:
            doUncopied(rasteraccessor1, rasterAccessor);
            break;
        case RasterAccessor.TAG_USHORT_UNCOPIED:
            doTagUshortUncopied(rasteraccessor1, rasterAccessor);
            break;
        case RasterAccessor.TAG_SHORT_UNCOPIED:
            doTagShortUncopied(rasteraccessor1, rasterAccessor);
            break;
        case RasterAccessor.TAG_INT_UNCOPIED:
            doTagIntUncopied(rasteraccessor1, rasterAccessor);
            break;
        case RasterAccessor.TAG_FLOAT_UNCOPIED:
            doTagFloatUncopied(rasteraccessor1, rasterAccessor);
            break;
        case RasterAccessor.TAG_DOUBLE_UNCOPIED:
            doTagDoubleUncopied(rasteraccessor1, rasterAccessor);
            break;
        default:
            throw new RuntimeException("Unknown Data Type.");
        }
        if (rasterAccessor.isDataCopy()) {
            rasterAccessor.clampDataArrays();
            rasterAccessor.copyDataToRaster();
        }
    }

    private Contributor[] sub_1189(int i, int j, float f, int k, int l, int i1) {
        return Class_w.sub_2d6(interp, i, j, f, k, l, i1);
    }

    private void doUncopied(RasterAccessor rasteraccessor, RasterAccessor rasteraccessor1) {
        int i = rasteraccessor.getWidth();
        int j = rasteraccessor.getHeight();
        int k = rasteraccessor1.getWidth();
        int l = rasteraccessor1.getHeight();
        int i1 = rasteraccessor1.getNumBands();
        int j1 = rasteraccessor1.getX();
        int k1 = rasteraccessor1.getY();
        Contributor aclass_m[] = sub_1189(i, k, scaleX, rasteraccessor.getX(), rasteraccessor1.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(j, l, scaleY, rasteraccessor.getY(), rasteraccessor1.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = rasteraccessor.getPixelStride();
        int i2 = rasteraccessor.getScanlineStride();
        byte abyte0[][] = rasteraccessor.getByteDataArrays();
        int ai[] = rasteraccessor.getBandOffsets();
        int j2 = rasteraccessor1.getPixelStride();
        int k2 = rasteraccessor1.getScanlineStride();
        byte abyte1[][] = rasteraccessor1.getByteDataArrays();
        int ai1[] = rasteraccessor1.getBandOffsets();
        float af[] = new float[i];
        for (int l2 = 0; l2 < i1; l2++) {
            byte abyte2[] = abyte1[l2];
            byte abyte3[] = abyte0[l2];
            int i3 = ai[l2];
            int j3 = ai1[l2];
            for (int k3 = 0; k3 < l; k3++) {
                for (int l3 = 0; l3 < i; l3++) {
                    float f = 0.0F;
                    int k4 = l3 * l1 + i3;
                    f = (float) ImprovedScaleWarpOpImage.Class_r.sub_4da(aclass_m1[k3], i2, k4, abyte3);
                    af[l3] = f;
                }

                int i4 = k3 * k2 + j3;
                int j4 = 0;
                for (int l4 = i4; j4 < k; l4 += j2) {
                    float f1 = ImprovedScaleWarpOpImage.Class_r.sub_2bb(filter, aclass_m[j4], j4 + j1, k3 + k1, af);
                    int i5 = (int) (f1 + 0.5D);
                    if (i5 < 0) {
                        i5 = 0;
                    } else if (i5 > 255) {
                        i5 = 255;
                    }
                    abyte2[l4] = (byte) i5;
                    j4++;
                }

            }

        }

    }

    private void doTagUshortUncopied(RasterAccessor src, RasterAccessor dst) {
        int w1 = src.getWidth();
        int h1 = src.getHeight();
        int w2 = dst.getWidth();
        int h2 = dst.getHeight();
        int nb = dst.getNumBands();
        int x2 = dst.getX();
        int y1 = dst.getY();
        Contributor aclass_m[] = sub_1189(w1, w2, scaleX, src.getX(), dst.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(h1, h2, scaleY, src.getY(), dst.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = src.getPixelStride();
        int i2 = src.getScanlineStride();
        short aword0[][] = src.getShortDataArrays();
        int ai[] = src.getBandOffsets();
        int j2 = dst.getPixelStride();
        int k2 = dst.getScanlineStride();
        short aword1[][] = dst.getShortDataArrays();
        int ai1[] = dst.getBandOffsets();
        double ad[] = new double[w1];
        for (int l2 = 0; l2 < nb; l2++) {
            short aword2[] = aword1[l2];
            short aword3[] = aword0[l2];
            int i3 = ai[l2];
            int j3 = ai1[l2];
            for (int k3 = 0; k3 < h2; k3++) {
                for (int l3 = 0; l3 < w1; l3++) {
                    double d = 0.0D;
                    int k4 = l3 * l1 + i3;
                    d = ImprovedScaleWarpOpImage.Class_r.sub_48a(aclass_m1[k3], i2, k4, aword3);
                    ad[l3] = d;
                }

                int i4 = k3 * k2 + j3;
                for (int j4 = 0; j4 < w2; j4++) {
                    double d1 = ImprovedScaleWarpOpImage.Class_r.sub_309(filter, aclass_m[j4], j4 + x2, k3 + y1, ad);
                    int l4 = (int) (d1 + 0.5D);
                    if (l4 < 0) {
                        l4 = 0;
                    } else if (l4 > 0x10000) {
                        l4 = 0x10000;
                    }
                    int i5 = j4 * j2 + i4;
                    aword2[i5] = (short) (l4 & 0xffff);
                }

            }

        }

    }

    private void doTagShortUncopied(RasterAccessor src, RasterAccessor dst) {
        int i = src.getWidth();
        int j = src.getHeight();
        int k = dst.getWidth();
        int l = dst.getHeight();
        int i1 = dst.getNumBands();
        int j1 = dst.getX();
        int k1 = dst.getY();
        Contributor aclass_m[] = sub_1189(i, k, scaleX, src.getX(), dst.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(j, l, scaleY, src.getY(), dst.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = src.getPixelStride();
        int i2 = src.getScanlineStride();
        short aword0[][] = src.getShortDataArrays();
        int ai[] = src.getBandOffsets();
        int j2 = dst.getPixelStride();
        int k2 = dst.getScanlineStride();
        short aword1[][] = dst.getShortDataArrays();
        int ai1[] = dst.getBandOffsets();
        double ad[] = new double[i];
        for (int l2 = 0; l2 < i1; l2++) {
            short aword2[] = aword1[l2];
            short aword3[] = aword0[l2];
            int i3 = ai[l2];
            int j3 = ai1[l2];
            for (int k3 = 0; k3 < l; k3++) {
                for (int l3 = 0; l3 < i; l3++) {
                    double d = 0.0D;
                    int k4 = l3 * l1 + i3;
                    d = ImprovedScaleWarpOpImage.Class_r.sub_43d(aclass_m1[k3], i2, k4, aword3);
                    ad[l3] = d;
                }

                int i4 = k3 * k2 + j3;
                for (int j4 = 0; j4 < k; j4++) {
                    double d1 = ImprovedScaleWarpOpImage.Class_r.sub_309(filter, aclass_m[j4], j4 + j1, k3 + k1, ad);
                    int l4 = (int) (d1 + 0.5D);
                    if (l4 < -32768) {
                        l4 = -32768;
                    } else if (l4 > 32767) {
                        l4 = 32767;
                    }
                    int i5 = j4 * j2 + i4;
                    aword2[i5] = (short) l4;
                }

            }

        }

    }

    private void doTagIntUncopied(RasterAccessor src, RasterAccessor dst) {
        int i = src.getWidth();
        int j = src.getHeight();
        int k = dst.getWidth();
        int l = dst.getHeight();
        int i1 = dst.getNumBands();
        int j1 = dst.getX();
        int k1 = dst.getY();
        Contributor aclass_m[] = sub_1189(i, k, scaleX, src.getX(), dst.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(j, l, scaleY, src.getY(), dst.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = src.getPixelStride();
        int i2 = src.getScanlineStride();
        int ai[][] = src.getIntDataArrays();
        int ai1[] = src.getBandOffsets();
        int j2 = dst.getPixelStride();
        int k2 = dst.getScanlineStride();
        int ai2[][] = dst.getIntDataArrays();
        int ai3[] = dst.getBandOffsets();
        double ad[] = new double[i];
        for (int l2 = 0; l2 < i1; l2++) {
            int ai4[] = ai2[l2];
            int ai5[] = ai[l2];
            int i3 = ai1[l2];
            int j3 = ai3[l2];
            for (int k3 = 0; k3 < l; k3++) {
                for (int l3 = 0; l3 < i; l3++) {
                    double d = 0.0D;
                    int k4 = l3 * l1 + i3;
                    d = ImprovedScaleWarpOpImage.Class_r.sub_3f0(aclass_m1[k3], i2, k4, ai5);
                    ad[l3] = d;
                }

                int i4 = k3 * k2 + j3;
                for (int j4 = 0; j4 < k; j4++) {
                    double d1 = ImprovedScaleWarpOpImage.Class_r.sub_309(filter, aclass_m[j4], j4 + j1, k3 + k1, ad);
                    int l4 = (int) (d1 + 0.5D);
                    int i5 = j4 * j2 + i4;
                    ai4[i5] = l4;
                }

            }

        }

    }

    private void doTagFloatUncopied(RasterAccessor src, RasterAccessor dst) {
        int i = src.getWidth();
        int j = src.getHeight();
        int k = dst.getWidth();
        int l = dst.getHeight();
        int i1 = dst.getNumBands();
        int j1 = dst.getX();
        int k1 = dst.getY();
        Contributor aclass_m[] = sub_1189(i, k, scaleX, src.getX(), dst.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(j, l, scaleY, src.getY(), dst.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = src.getPixelStride();
        int i2 = src.getScanlineStride();
        float af[][] = src.getFloatDataArrays();
        int ai[] = src.getBandOffsets();
        int j2 = dst.getPixelStride();
        int k2 = dst.getScanlineStride();
        float af1[][] = dst.getFloatDataArrays();
        int ai1[] = dst.getBandOffsets();
        double ad[] = new double[i];
        for (int l2 = 0; l2 < i1; l2++) {
            float af2[] = af1[l2];
            float af3[] = af[l2];
            int i3 = ai[l2];
            int j3 = ai1[l2];
            for (int k3 = 0; k3 < l; k3++) {
                for (int l3 = 0; l3 < i; l3++) {
                    double d = 0.0D;
                    int k4 = l3 * l1 + i3;
                    d = ImprovedScaleWarpOpImage.Class_r.sub_3a4(aclass_m1[k3], i2, k4, af3);
                    ad[l3] = d;
                }

                int i4 = k3 * k2 + j3;
                for (int j4 = 0; j4 < k; j4++) {
                    double d1 = ImprovedScaleWarpOpImage.Class_r.sub_309(filter, aclass_m[j4], j4 + j1, k3 + k1, ad);
                    double d2 = d1;
                    if (d2 > 3.4028234663852886E+38D) {
                        d2 = 3.4028234663852886E+38D;
                    } else if (d2 < -3.4028234663852886E+38D) {
                        d2 = -3.4028234663852886E+38D;
                    }
                    int l4 = j4 * j2 + i4;
                    af2[l4] = (float) d2;
                }
            }
        }
    }

    private void doTagDoubleUncopied(RasterAccessor src, RasterAccessor dst) {
        int i = src.getWidth();
        int j = src.getHeight();
        int k = dst.getWidth();
        int l = dst.getHeight();
        int i1 = dst.getNumBands();
        int j1 = dst.getX();
        int k1 = dst.getY();
        Contributor aclass_m[] = sub_1189(i, k, scaleX, src.getX(), dst.getX(), computableBounds == null ? -1 : (int) computableBounds.getWidth());
        Contributor aclass_m1[] = sub_1189(j, l, scaleY, src.getY(), dst.getY(), computableBounds == null ? -1 : (int) computableBounds.getHeight());
        int l1 = src.getPixelStride();
        int i2 = src.getScanlineStride();
        double ad[][] = src.getDoubleDataArrays();
        int ai[] = src.getBandOffsets();
        int j2 = dst.getPixelStride();
        int k2 = dst.getScanlineStride();
        double ad1[][] = dst.getDoubleDataArrays();
        int ai1[] = dst.getBandOffsets();
        double ad2[] = new double[i];
        for (int l2 = 0; l2 < i1; l2++) {
            double ad3[] = ad1[l2];
            double ad4[] = ad[l2];
            int i3 = ai[l2];
            int j3 = ai1[l2];
            for (int k3 = 0; k3 < l; k3++) {
                for (int l3 = 0; l3 < i; l3++) {
                    double d = 0.0D;
                    int k4 = l3 * l1 + i3;
                    d = ImprovedScaleWarpOpImage.Class_r.sub_358(aclass_m1[k3], i2, k4, ad4);
                    ad2[l3] = d;
                }

                int i4 = k3 * k2 + j3;
                for (int j4 = 0; j4 < k; j4++) {
                    double d1 = ImprovedScaleWarpOpImage.Class_r.sub_309(filter, aclass_m[j4], j4 + j1, k3 + k1, ad2);
                    int l4 = j4 * j2 + i4;
                    ad3[l4] = d1;
                }

            }

        }

    }

    public Rectangle mapDestRect(Rectangle rectangle, int i) {
        Rectangle rectangle1 = super.mapDestRect(rectangle, i);
        PlanarImage planarimage;
        if ((planarimage = getSourceImage(i)).getBounds().intersects(rectangle1))
            return planarimage.getBounds().intersection(rectangle1);
        else
            return rectangle1;
    }

    static final float var_ee6 = 5E-05F;

    protected float scaleX;

    protected float scaleY;

    protected ImprovedScaleInterpolation interp;

    private Filter filter;
}
