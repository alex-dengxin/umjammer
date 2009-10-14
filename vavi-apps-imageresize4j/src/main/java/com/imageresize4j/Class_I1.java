package com.imageresize4j;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.security.AccessControlException;

import sun.awt.image.ByteInterleavedRaster;


final class Class_I1 extends Class_i {

    Class_I1() {
    }

    final boolean isSupported(BufferedImage image) {
        try {
            return (image.getRaster() instanceof ByteInterleavedRaster) && !(image.getRaster().getSampleModel() instanceof SinglePixelPackedSampleModel) && !(image.getColorModel() instanceof IndexColorModel) && (image.getColorModel().getNumComponents() == 3 || image.getColorModel().getNumComponents() == 4);
        } catch (AccessControlException _ex) {
            return false;
        }
    }

    final int getScanlineStride(BufferedImage image) {
        return ((ByteInterleavedRaster) image.getRaster()).getScanlineStride();
    }

    final void sub_1eb(WritableRaster writableraster, WritableRaster writableraster1, int i, int j, int k, int l, int bytesPerPixel, ImageResizeProcessor.Contributor aclass_b[], ImageResizeProcessor.Contributor aclass_b1[]) {
        ByteInterleavedRaster byteinterleavedraster;
        int pixelStride = (byteinterleavedraster = (ByteInterleavedRaster) writableraster).getPixelStride();
        int scanlineStride = byteinterleavedraster.getScanlineStride();
        int l1 = byteinterleavedraster.getDataBuffer().getOffset() - byteinterleavedraster.getSampleModelTranslateY() * scanlineStride - byteinterleavedraster.getSampleModelTranslateX() * pixelStride;
        int i2 = writableraster.getMinX() - writableraster.getSampleModelTranslateX();
        int j2 = writableraster.getMinY() - writableraster.getSampleModelTranslateY();
        int k2 = l1 - (i2 * pixelStride + j2 * scanlineStride);
        byte abyte0[] = byteinterleavedraster.getDataStorage();
        int ai[] = byteinterleavedraster.getDataOffsets();
        ByteInterleavedRaster byteinterleavedraster1;
        int l2 = (byteinterleavedraster1 = (ByteInterleavedRaster) writableraster1).getPixelStride();
        int i3 = byteinterleavedraster1.getScanlineStride();
        int j3 = byteinterleavedraster1.getDataBuffer().getOffset() - byteinterleavedraster1.getSampleModelTranslateY() * i3 - byteinterleavedraster1.getSampleModelTranslateX() * l2;
        int k3 = byteinterleavedraster1.getMinX() - byteinterleavedraster1.getSampleModelTranslateX();
        int l3 = byteinterleavedraster1.getMinY() - byteinterleavedraster1.getSampleModelTranslateY();
        int i4 = j3 - (k3 * l2 + l3 * i3);
        byte abyte1[] = byteinterleavedraster1.getDataStorage();
        int ai1[] = byteinterleavedraster1.getDataOffsets();
        if (bytesPerPixel == 3) {
            float reds[] = new float[i];
            float greens[] = new float[i];
            float blues[] = new float[i];
            int j4 = k2 + ai[0];
            int k4 = k2 + ai[1];
            int i5 = k2 + ai[2];
            int k5 = i4 + ai1[0];
            int i6 = i4 + ai1[1];
            int k6 = i4 + ai1[2];
            for (int i7 = 0; i7 < l; i7++) {
                for (int k7 = 0; k7 < i; k7++) {
                    int ai2[] = {
                        j4, k4, i5
                    };
                    float af7[] = com.imageresize4j.Class_k.sub_40e(aclass_b[i7], k7 * pixelStride, abyte0, ai2);
                    reds[k7] = af7[0];
                    greens[k7] = af7[1];
                    blues[k7] = af7[2];
                }

                for (int l7 = 0; l7 < k; l7++) {
                    float af8[][] = {
                        reds, greens, blues
                    };
                    float af9[] = com.imageresize4j.Class_k.sub_552(ImageResizeProcessor.var_1214, aclass_b1[l7], l7, i7, af8);
                    float f = af9[0];
                    float f1 = af9[1];
                    float f2 = af9[2];
                    int j9 = (int) (f + 0.5D);
                    j9 = Class_w.sub_b9(l7, i7, j9);
                    int k9 = i7 * i3 + l7 * l2;
                    abyte1[k9 + k5] = (byte) j9;
                    j9 = (int) (f1 + 0.5D);
                    j9 = Class_w.sub_b9(l7, i7, j9);
                    abyte1[k9 + i6] = (byte) j9;
                    j9 = (int) (f2 + 0.5D);
                    j9 = Class_w.sub_b9(l7, i7, j9);
                    abyte1[k9 + k6] = (byte) j9;
                }
            }

            return;
        }
        if (bytesPerPixel == 4) {
            float af1[] = new float[i];
            float af3[] = new float[i];
            float af5[] = new float[i];
            float af6[] = new float[i];
            int l4 = k2 + ai[0];
            int j5 = k2 + ai[1];
            int l5 = k2 + ai[2];
            int j6 = k2 + ai[3];
            int l6 = i4 + ai1[0];
            int j7 = i4 + ai1[1];
            int i8 = i4 + ai1[2];
            int j8 = i4 + ai1[3];
            for (int k8 = 0; k8 < l; k8++) {
                for (int l8 = 0; l8 < i; l8++) {
                    int l9 = l8 * pixelStride;
                    int ai3[] = {
                        l4, j5, l5, j6
                    };
                    float af12[] = com.imageresize4j.Class_k.sub_40e(aclass_b[k8], l9, abyte0, ai3);
                    af1[l8] = af12[0];
                    af3[l8] = af12[1];
                    af5[l8] = af12[2];
                    af6[l8] = af12[3];
                }

                for (int i9 = 0; i9 < k; i9++) {
                    float af10[][] = {
                        af1, af3, af5, af6
                    };
                    float af11[] = com.imageresize4j.Class_k.sub_552(ImageResizeProcessor.var_1214, aclass_b1[i9], i9, k8, af10);
                    float f3 = af11[0];
                    float f4 = af11[1];
                    float f5 = af11[2];
                    float f6 = af11[3];
                    int i10 = (int) (f3 + 0.5D);
                    i10 = Class_w.sub_b9(i9, k8, i10);
                    int j10 = k8 * i3 + i9 * l2;
                    abyte1[j10 + l6] = (byte) i10;
                    i10 = (int) (f4 + 0.5D);
                    i10 = Class_w.sub_b9(i9, k8, i10);
                    abyte1[j10 + j7] = (byte) i10;
                    i10 = (int) (f5 + 0.5D);
                    i10 = Class_w.sub_b9(i9, k8, i10);
                    abyte1[j10 + i8] = (byte) i10;
                    i10 = (int) (f6 + 0.5D);
                    i10 = Class_w.sub_b9(i9, k8, i10);
                    abyte1[j10 + j8] = (byte) i10;
                }
            }
        }
    }
}
