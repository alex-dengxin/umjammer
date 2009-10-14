package com.imageresize4j;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;


public class ImageResizeProcessor {
    static class Class_k {

        static float[] sub_40e(Contributor contirbutor, int i, byte abyte0[], int ai[]) {
            float af[] = new float[ai.length];
            for (Contributor aContributor = contirbutor.next; aContributor != null; aContributor = aContributor.next) {
                float f = aContributor.var_fc;
                int j = aContributor.var_e4 + i;
                for (int k = 0; k < af.length; k++) {
                    af[k] += (abyte0[j + ai[k]] & 0xff) * f;
                }
            }

            return af;
        }

        static float[] sub_480(Contributor contirbutor, int i, int j, byte abyte0[], float af[], float af1[], float af2[], int k) {
            float af3[] = new float[4];
            for (Contributor aContributor = contirbutor.next; aContributor != null; aContributor = aContributor.next) {
                int l;
                if ((l = abyte0[aContributor.var_e4 * i + j] & 0xff) != k) {
                    af3[0] += af[l] * aContributor.var_fc;
                    af3[1] += af1[l] * aContributor.var_fc;
                    af3[2] += af2[l] * aContributor.var_fc;
                    af3[3] += 255F * aContributor.var_fc;
                } else {
                    af3[0] += 255F * aContributor.var_fc;
                    af3[1] += 255F * aContributor.var_fc;
                    af3[2] += 255F * aContributor.var_fc;
                    af3[3] += 255F * aContributor.var_fc;
                }
            }

            return af3;
        }

        static float[] sub_552(Filter filetr, Contributor contirbutor, int i, int j, float af[][]) {
            float af1[] = new float[af.length];
            for (Contributor aContributor = contirbutor.next; aContributor != null; aContributor = aContributor.next) {
                int k = aContributor.var_e4;
                float f = aContributor.var_fc;
                for (int l = 0; l < af.length; l++) {
                    af1[l] += filetr.sub_426(i, j, af[l][k]) * f;
                }
            }

            return af1;
        }

        private static float sub_5c3(Contributor contirbutor, ColorModel colormodel, WritableRaster writableraster, int i, int j) {
            float f = 0.0F;
            for (Contributor aContributor = contirbutor.next; aContributor != null; aContributor = aContributor.next) {
                int ai[] = colormodel.getComponents(writableraster.getSample(i, aContributor.var_e4, 0), null, 0);
                f += ai[j] * aContributor.var_fc;
            }

            return f;
        }

        static double sub_61c(Contributor contributor, ColorModel colormodel, WritableRaster writableraster, int i, int j) {
            double d = 0.0D;
            for (Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next) {
                int ai[] = colormodel.getComponents(writableraster.getSample(i, aContributor.var_e4, 0), null, 0);
                d += ai[j] * aContributor.var_fc;
            }

            return d;
        }

        static float sub_676(Contributor contirbutor, WritableRaster writableraster, int i, int j) {
            float f = 0.0F;
            for (Contributor class_b1 = contirbutor.next; class_b1 != null; class_b1 = class_b1.next)
                f += writableraster.getSample(i, class_b1.var_e4, j) * class_b1.var_fc;

            return f;
        }

        static double sub_6c2(Contributor contirbutor, WritableRaster writableraster, int i, int j) {
            double d = 0.0D;
            for (Contributor class_b1 = contirbutor.next; class_b1 != null; class_b1 = class_b1.next)
                d += writableraster.getSample(i, class_b1.var_e4, j) * class_b1.var_fc;

            return d;
        }

        static float sub_70f(Filter filter, Contributor contributor, int i, int j, float af[]) {
            float f = 0.0F;
            for (Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next)
                f += filter.sub_426(i, j, af[aContributor.var_e4]) * aContributor.var_fc;

            return f;
        }

        static double sub_75d(Filter filter, Contributor contributor, int i, int j, double ad[]) {
            double d = 0.0D;
            for (Contributor aContributor = contributor.next; aContributor != null; aContributor = aContributor.next)
                d += filter.sub_442(i, j, ad[aContributor.var_e4]) * aContributor.var_fc;

            return d;
        }

        Class_k() {
        }
    }

    static class Contributor {

        Contributor next;
        int var_e4;
        int var_ec;
        int var_f4;
        float var_fc;

        Contributor() {
            next = null;
        }
    }

    public ImageResizeProcessor(int type) {
        filter = null;
        switch (type) {
        case TYPE_NEAREST_NEIGHBOR:
            filter = new Filter.NearestNeighborFilter();
            break;
        case TYPE_BILINEAR:
            filter = new Filter.BiLinearFilter();
            break;
        case TYPE_BICUBIC:
            filter = new Filter.BiCubicFilter();
            break;
        case TYPE_LANCZOS3:
            filter = new Filter.Lanczos3Filter();
            break;
        case TYPE_BLACKMAN:
            filter = new Filter.BlackManFilter();
            break;
        case TYPE_HAMMING_4:
            filter = new Filter.HammingFilter(4F, 0.5155802F);
            break;
        case TYPE_HAMMING_5:
            filter = new Filter.HammingFilter(5F, 0.509716F);
            break;
        case TYPE_HANN:
            filter = new Filter.HammingFilter(5F, 0.5F);
            break;
        case TYPE_COMBINED_2:
            filter = new Filter.CombinedFilter(2D, 1.31534990379957D, 0.51537647558777899D);
            break;
        case TYPE_COMBINED_3:
            filter = new Filter.CombinedFilter(3D, 1.3228197602021701D, 0.39966284026594701D);
            break;
        case TYPE_COMBINED_4:
            filter = new Filter.CombinedFilter(4D, 1.90277105527298D, 0.44135190006120201D);
            break;
        case TYPE_COMBINED_5:
            filter = new Filter.CombinedFilter(5D, 2.3085236363567798D, 0.45498285354921902D);
            break;
        case TYPE_HIPASS_2:
            filter = new Filter.HiPass2Filter();
            break;
        case TYPE_HIPASS_3:
            filter = new Filter.HiPass4Filter();
            break;
        case TYPE_HIPASS_5:
            filter = new Filter.HiPass5Filter();
            break;
        case TYPE_SHARP_LIGHT_3:
            filter = new Filter.SharpLight3Filter();
            break;
        case TYPE_SHARP_LIGHT_5:
            filter = new Filter.SharpLight5Filter();
            break;
        case TYPE_SHARP_3:
            filter = new Filter.Sharp3Filter();
            break;
        case TYPE_SHARP_5:
            filter = new Filter.Sharp5Filter();
            break;
        case TYPE_SHARP_MORE_3:
            filter = new Filter.SharpMore3Filter();
            break;
        case TYPE_SHARP_MORE_5:
            filter = new Filter.SharpMore5Filter();
            break;
        case TYPE_IDEAL_2:
            filter = new Filter.Ideal2Filter();
            break;
        case TYPE_IDEAL_3:
            filter = new Filter.Ideal3Filter();
            break;
        case TYPE_IDEAL_5:
            filter = new Filter.Ideal5Filter();
            break;
        }
        if (filter == null) {
            throw new IllegalArgumentException("Unknown interpolation type.");
        } else {
            return;
        }
    }

    private static boolean setImage(BufferedImage image) {
        return var_121c.isSupported(image);
    }

    protected Contributor[] computeContributors(int h1, int h2, float yScale, int scanStride) {
        return Class_A1.sub_255(filter, h1, h2, yScale, scanStride);
    }

    public BufferedImage resize(BufferedImage source, int newWidth, int newHeight) {
        if (source == null) {
            throw new NullPointerException("Image is null!");
        }
        if (newWidth <= 0 && newHeight <= 0) {
            throw new IllegalArgumentException("destination width & height are both <=0!");
        }
        float f = (float) newWidth / (float) source.getWidth();
        if (newHeight <= 0) {
            newHeight = (int) Math.rint((double) f * (double) source.getHeight());
        }
        if (newWidth <= 0) {
            newWidth = (int) Math.rint((double) f * (double) source.getWidth());
        }
        ColorModel cm;
        BufferedImage newImage;
        if ((cm = source.getColorModel()) instanceof IndexColorModel) {
            if (cm.getTransparency() != 1) {
                newImage = new BufferedImage(newWidth, newHeight, 2);
            } else {
                newImage = new BufferedImage(newWidth, newHeight, 5);
            }
        } else {
            newImage = new BufferedImage(cm, source.getRaster().createCompatibleWritableRaster(newWidth, newHeight), cm.isAlphaPremultiplied(), null);
        }
        int w2 = newImage.getWidth();
        int h2 = newImage.getHeight();
        int w1 = source.getWidth();
        int h1 = source.getHeight();
        float xscale = (float) w2 / (float) w1;
        float yscale = (float) h2 / (float) h1;
        return process(source, newImage, xscale, yscale);
    }

    public BufferedImage scale(BufferedImage source, float xscale, float yscale) {
        if (source == null) {
            throw new NullPointerException("Image is null!");
        }
        if (xscale <= 0.0F && yscale <= 0.0F) {
            throw new IllegalArgumentException("destination width & height are both <=0!");
        }
        if (xscale <= 0.0F) {
            xscale = yscale;
        }
        if (yscale <= 0.0F) {
            yscale = xscale;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        width = (int) Math.rint(width * xscale);
        height = (int) Math.rint(height * yscale);
        ColorModel cm;
        BufferedImage newImage;
        if ((cm = source.getColorModel()) instanceof IndexColorModel) {
            if (cm.getTransparency() != 1) {
                newImage = new BufferedImage(width, height, 2);
            } else {
                newImage = new BufferedImage(width, height, 5);
            }
        } else {
            newImage = new BufferedImage(cm, source.getRaster().createCompatibleWritableRaster(width, height), cm.isAlphaPremultiplied(), null);
        }
        int w2 = newImage.getWidth();
        int h2 = newImage.getHeight();
        int w1 = source.getWidth();
        int h1 = source.getHeight();
        xscale = (float) w2 / (float) w1;
        yscale = (float) h2 / (float) h1;
        return process(source, newImage, xscale, yscale);
    }

    protected BufferedImage process(BufferedImage source, BufferedImage newImage, float xscale, float yscale) {
        ColorModel colormodel = source.getColorModel();
        int w1 = source.getWidth();
        int h1 = source.getHeight();
        int w2 = newImage.getWidth();
        int h2 = newImage.getHeight();
        Contributor[] contributors = computeContributors(w1, w2, xscale, 1);
        int scanStride = 1;
        boolean flag = setImage(source);
        if (flag) {
            scanStride = var_121c.getScanlineStride(source);
        }
        Contributor[] contributors2 = computeContributors(h1, h2, yscale, scanStride);
        WritableRaster raster1 = source.getRaster();
        WritableRaster raster2 = newImage.getRaster();
        int numComponents = colormodel.getNumComponents();
        int[] components = new int[numComponents];
        int[] componentSize = colormodel.getComponentSize();
        for (int k1 = 0; k1 < numComponents; k1++)
            if (componentSize != null) {
                components[k1] = (1 << componentSize[k1]) - 1;
            } else {
                components[k1] = (1 << colormodel.getPixelSize()) - 1;
            }
        if (flag) {
            var_121c.sub_1eb(raster1, raster2, w1, h1, w2, h2, numComponents, contributors2, contributors);
        } else if (colormodel instanceof IndexColorModel) {
            if (colormodel.getPixelSize() <= 8) {
                if (numComponents < 4) {
                    IndexColorModel icm = (IndexColorModel) colormodel;
                    int mapSize = icm.getMapSize();
                    float rs[] = new float[mapSize];
                    float gs[] = new float[mapSize];
                    float bs[] = new float[mapSize];
                    byte temp[] = new byte[mapSize];
                    icm.getReds(temp);
                    for (int i = 0; i < mapSize; i++) {
                        rs[i] = temp[i] & 0xff;
                    }
                    icm.getGreens(temp);
                    for (int l5 = 0; l5 < mapSize; l5++) {
                        gs[l5] = temp[l5] & 0xff;
                    }
                    icm.getBlues(temp);
                    for (int i6 = 0; i6 < mapSize; i6++) {
                        bs[i6] = temp[i6] & 0xff;
                    }
                    byte data1[] = ((DataBufferByte) raster1.getDataBuffer()).getData();
                    float rs2[] = new float[w1];
                    float gs2[] = new float[w1];
                    float bs2[] = new float[w1];
                    for (int y7 = 0; y7 < h2; y7++) {
                        for (int i8 = 0; i8 < w1; i8++) {
                            float rgb[] = com.imageresize4j.Class_k.getRgb(contributors2[y7], w1, i8, data1, rs, gs, bs, -1);
                            rs2[i8] = rgb[0];
                            gs2[i8] = rgb[1];
                            bs2[i8] = rgb[2];
                        }

                        for (int j8 = 0; j8 < w2; j8++) {
                            float af16[][] = {
                                rs2, gs2, bs2
                            };
                            float af17[] = com.imageresize4j.Class_k.sub_552(var_1214, contributors[j8], j8, y7, af16);
                            float f3 = af17[0];
                            float f4 = af17[1];
                            float f5 = af17[2];
                            int j9 = (int) (f3 + 0.5D);
                            j9 = Class_w.sub_db(components[0], j9);
                            raster2.setSample(j8, y7, 0, j9);
                            j9 = (int) (f4 + 0.5D);
                            j9 = Class_w.sub_db(components[1], j9);
                            raster2.setSample(j8, y7, 1, j9);
                            j9 = (int) (f5 + 0.5D);
                            j9 = Class_w.sub_db(components[2], j9);
                            raster2.setSample(j8, y7, 2, j9);
                        }

                    }

                } else {
                    IndexColorModel icm = (IndexColorModel) colormodel;
                    int mapSize = icm.getMapSize();
                    float af2[] = new float[mapSize];
                    float af4[] = new float[mapSize];
                    float af6[] = new float[mapSize];
                    byte abyte1[] = new byte[mapSize];
                    icm.getReds(abyte1);
                    for (int j6 = 0; j6 < mapSize; j6++) {
                        af2[j6] = abyte1[j6] & 0xff;
                    }
                    icm.getGreens(abyte1);
                    for (int k6 = 0; k6 < mapSize; k6++) {
                        af4[k6] = abyte1[k6] & 0xff;
                    }
                    icm.getBlues(abyte1);
                    for (int l6 = 0; l6 < mapSize; l6++) {
                        af6[l6] = abyte1[l6] & 0xff;
                    }
                    byte abyte3[] = ((DataBufferByte) raster1.getDataBuffer()).getData();
                    int k7 = icm.getTransparentPixel();
                    float af9[] = new float[w1];
                    float af11[] = new float[w1];
                    float af12[] = new float[w1];
                    float af13[] = new float[w1];
                    for (int k8 = 0; k8 < h2; k8++) {
                        for (int l8 = 0; l8 < w1; l8++) {
                            float af15[] = com.imageresize4j.Class_k.getRgb(contributors2[k8], w1, l8, abyte3, af2, af4, af6, k7);
                            af9[l8] = af15[0];
                            af11[l8] = af15[1];
                            af12[l8] = af15[2];
                            af13[l8] = af15[3];
                        }

                        for (int i9 = 0; i9 < w2; i9++) {
                            float af18[][] = {
                                af9, af11, af12, af13
                            };
                            float af19[] = com.imageresize4j.Class_k.sub_552(var_1214, contributors[i9], i9, k8, af18);
                            float f6 = af19[0];
                            float f7 = af19[1];
                            float f8 = af19[2];
                            float f9 = af19[3];
                            int k9 = (int) (f6 + 0.5D);
                            k9 = Class_w.sub_db(components[0], k9);
                            raster2.setSample(i9, k8, 0, k9);
                            k9 = (int) (f7 + 0.5D);
                            k9 = Class_w.sub_db(components[1], k9);
                            raster2.setSample(i9, k8, 1, k9);
                            k9 = (int) (f8 + 0.5D);
                            k9 = Class_w.sub_db(components[2], k9);
                            raster2.setSample(i9, k8, 2, k9);
                            k9 = (int) (f9 + 0.5D);
                            k9 = Class_w.sub_db(components[3], k9);
                            raster2.setSample(i9, k8, 3, k9);
                        }

                    }

                }
            } else {
                double ad[] = new double[w1];
                for (int j2 = 0; j2 < numComponents; j2++) {
                    for (int i3 = 0; i3 < h2; i3++) {
                        for (int l3 = 0; l3 < w1; l3++)
                            ad[l3] = com.imageresize4j.Class_k.sub_61c(contributors2[i3], colormodel, raster1, l3, j2);

                        for (int i4 = 0; i4 < w2; i4++) {
                            double d;
                            int i7 = (int) ((d = com.imageresize4j.Class_k.sub_75d(var_1214, contributors[i4], i4, i3, ad)) + 0.5D);
                            i7 = Class_w.sub_db(components[j2], i7);
                            raster2.setSample(i4, i3, j2, i7);
                        }

                    }

                }

            }
        } else if (colormodel.getPixelSize() <= 8) {
            float af[] = new float[w1];
            for (int k2 = 0; k2 < numComponents; k2++) {
                for (int j3 = 0; j3 < h2; j3++) {
                    for (int j4 = 0; j4 < w1; j4++)
                        af[j4] = com.imageresize4j.Class_k.sub_676(contributors2[j3], raster1, j4, k2);

                    for (int k4 = 0; k4 < w2; k4++) {
                        float f2 = com.imageresize4j.Class_k.sub_70f(var_1214, contributors[k4], k4, j3, af);
                        int j5 = (int) (f2 + 0.5D);
                        j5 = Class_w.sub_db(components[k2], j5);
                        raster2.setSample(k4, j3, k2, j5);
                    }
                }
            }
        } else {
            double ad1[] = new double[w1];
            for (int l2 = 0; l2 < numComponents; l2++) {
                for (int k3 = 0; k3 < h2; k3++) {
                    for (int l4 = 0; l4 < w1; l4++)
                        ad1[l4] = com.imageresize4j.Class_k.sub_6c2(contributors2[k3], raster1, l4, l2);

                    for (int i5 = 0; i5 < w2; i5++) {
                        double d1 = com.imageresize4j.Class_k.sub_75d(var_1214, contributors[i5], i5, k3, ad1);
                        int j7 = (int) (d1 + 0.5D);
                        j7 = Class_w.sub_db(components[l2], j7);
                        raster2.setSample(i5, k3, l2, j7);
                    }
                }
            }
        }
        return newImage;
    }

    protected static final float MIN = 1E-05F;

    public static final int TYPE_NEAREST_NEIGHBOR = 0;
    public static final int TYPE_BILINEAR = 1;
    public static final int TYPE_BICUBIC = 2;
    public static final int TYPE_LANCZOS3 = 3;
    public static final int TYPE_BLACKMAN = 4;
    public static final int TYPE_HAMMING_4 = 5;
    public static final int TYPE_HAMMING_5 = 6;
    public static final int TYPE_HANN = 7;
    public static final int TYPE_COMBINED_2 = 8;
    public static final int TYPE_COMBINED_3 = 9;
    public static final int TYPE_COMBINED_4 = 10;
    public static final int TYPE_COMBINED_5 = 11;
    public static final int TYPE_HIPASS_2 = 12;
    public static final int TYPE_HIPASS_3 = 13;
    public static final int TYPE_HIPASS_5 = 14;
    public static final int TYPE_SHARP_LIGHT_3 = 15;
    public static final int TYPE_SHARP_LIGHT_5 = 16;
    public static final int TYPE_SHARP_3 = 17;
    public static final int TYPE_SHARP_5 = 18;
    public static final int TYPE_SHARP_MORE_3 = 19;
    public static final int TYPE_SHARP_MORE_5 = 20;
    public static final int TYPE_IDEAL_2 = 21;
    public static final int TYPE_IDEAL_3 = 22;
    public static final int TYPE_IDEAL_5 = 23;

    protected Filter filter;

    static Filter var_1214 = new Filter.Class_p();

    static Class_i var_121c = new Class_i();

    static {
        try {
            Class<?> class1 = Class.forName("com.imageresize4j.Class_I1");
            var_121c = (Class_i) (class1).newInstance();
        } catch (Exception e) {
        }
    }
}
