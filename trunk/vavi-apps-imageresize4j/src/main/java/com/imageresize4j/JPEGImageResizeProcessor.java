package com.imageresize4j;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import sun.awt.image.ByteInterleavedRaster;


public class JPEGImageResizeProcessor extends ImageResizeProcessor {

    public JPEGImageResizeProcessor(int i) {
        super(i);
    }

    final class JPEGImageResizeException extends RuntimeException {
    }

    class JPEGComponentSampleModel extends ComponentSampleModel {

        public JPEGComponentSampleModel(ComponentSampleModel csm, int i, int j) {
            super(csm.getDataType(), i, j, csm.getPixelStride(), csm.getScanlineStride(), csm.getBandOffsets());
        }

        public final DataBuffer createDataBuffer() {
            return new DataBufferByte(0);
        }
    }

    class JPEGWritableRaster extends WritableRaster {

        public JPEGWritableRaster(ComponentSampleModel csm, int i, int j) {
            super(new JPEGComponentSampleModel(csm, i, j), new Point());
            var_6fd = 0;
            var_705 = 0;
            var_72d = 0;
            var_735 = 1;
            var_73d = 2;
            var_745 = 0;
            var_74d = 1;
            var_755 = 2;
        }

        public final void sub_7e4(BufferedImage bufferedimage) {
            var_6ad = getWidth();
            int i = getHeight();
            var_6b5 = bufferedimage.getWidth();
            var_6bd = bufferedimage.getHeight();
            float f = (float) var_6b5 / (float) var_6ad;
            float f1 = (float) var_6bd / (float) i;
            var_715 = var_6ad * 3;
            var_6c5 = computeContributors(var_6ad, var_6b5, f, 1);
            var_6cd = computeContributors(i, var_6bd, f1, var_715);
            float f2 = f1 >= 1.0F ? filter.sub_null_1() : filter.sub_null_1() / f1;
            var_75d = (int) ((f2 + 1.0F) * 2.0F) + 1;
            var_6d5 = new float[var_6ad];
            var_6dd = new float[var_6ad];
            var_6e5 = new float[var_6ad];
            var_6ed = new byte[var_75d * var_6ad * 3];
            var_6f5 = ((ByteInterleavedRaster) bufferedimage.getRaster()).getDataStorage();
            var_725 = ((ByteInterleavedRaster) bufferedimage.getRaster()).getScanlineStride();
            var_70d = var_71d = ((ByteInterleavedRaster) bufferedimage.getRaster()).getPixelStride();
            int ai[] = ((ByteInterleavedRaster) bufferedimage.getRaster()).getDataOffsets();
            var_745 = ai[0];
            var_74d = ai[1];
            var_755 = ai[2];
        }

        public final void setRect(int i, int j, Raster raster) {
            if (i != 0 && raster.getWidth() != var_6ad)
                throw new JPEGImageResizeException();
            ByteInterleavedRaster byteinterleavedraster;
            int ai[] = (byteinterleavedraster = (ByteInterleavedRaster) raster).getDataOffsets();
            var_72d = ai[0];
            var_735 = ai[1];
            var_73d = ai[2];
            byte abyte0[] = byteinterleavedraster.getDataStorage();
            int k = j + raster.getHeight();
            label0: for (int l = j; l < k; l++) {
                System.arraycopy(var_6ed, abyte0.length, var_6ed, 0, var_6ed.length - abyte0.length);
                System.arraycopy(abyte0, 0, var_6ed, var_6ed.length - abyte0.length, abyte0.length);
                var_705 = l;
                int i1 = var_6cd[var_6fd].var_f4;
                do {
                    if (i1 > var_705)
                        continue label0;
                    sub_a00();
                    if (var_6fd == var_6bd)
                        continue label0;
                    i1 = var_6cd[var_6fd].var_f4;
                } while (true);
            }

        }

        private void sub_a00() {
            int i = ((var_705 - var_75d) + 1) * var_715;
            int j = 0;
            for (int k = -i; j < var_6ad; k += var_70d) {
                float f = 0.0F;
                float f2 = 0.0F;
                float f4 = 0.0F;
                for (ImageResizeProcessor.Contributor class_b = var_6cd[var_6fd].next; class_b != null; class_b = class_b.next) {
                    float f6 = class_b.var_fc;
                    int k1 = class_b.var_e4 + k;
                    f += (var_6ed[k1 + var_72d] & 0xff) * f6;
                    f2 += (var_6ed[k1 + var_735] & 0xff) * f6;
                    f4 += (var_6ed[k1 + var_73d] & 0xff) * f6;
                }

                var_6d5[j] = f;
                var_6dd[j] = f2;
                var_6e5[j] = f4;
                j++;
            }

            j = 0;
            for (int l = var_6fd * var_725; j < var_6b5; l += var_71d) {
                float f1 = 0.0F;
                float f3 = 0.0F;
                float f5 = 0.0F;
                for (ImageResizeProcessor.Contributor class_b1 = var_6c5[j].next; class_b1 != null; class_b1 = class_b1.next) {
                    int j1 = class_b1.var_e4;
                    float f7 = class_b1.var_fc;
                    f1 += var_6d5[j1] * f7;
                    f3 += var_6dd[j1] * f7;
                    f5 += var_6e5[j1] * f7;
                }

                int i1;
                if ((i1 = (int) (f1 + 0.5D)) < 0)
                    i1 = 0;
                else if (i1 > 255)
                    i1 = 255;
                var_6f5[l + var_745] = (byte) i1;
                if ((i1 = (int) (f3 + 0.5D)) < 0)
                    i1 = 0;
                else if (i1 > 255)
                    i1 = 255;
                var_6f5[l + var_74d] = (byte) i1;
                if ((i1 = (int) (f5 + 0.5D)) < 0)
                    i1 = 0;
                else if (i1 > 255)
                    i1 = 255;
                var_6f5[l + var_755] = (byte) i1;
                j++;
            }

            var_6fd++;
        }

        private int var_6ad;
        private int var_6b5;
        private int var_6bd;
        private ImageResizeProcessor.Contributor var_6c5[];
        private ImageResizeProcessor.Contributor var_6cd[];
        float var_6d5[];
        float var_6dd[];
        float var_6e5[];
        byte var_6ed[];
        byte var_6f5[];
        private int var_6fd;
        private int var_705;
        private int var_70d;
        private int var_715;
        private int var_71d;
        private int var_725;
        private int var_72d;
        private int var_735;
        private int var_73d;
        private int var_745;
        private int var_74d;
        private int var_755;
        private int var_75d;
    }
}
