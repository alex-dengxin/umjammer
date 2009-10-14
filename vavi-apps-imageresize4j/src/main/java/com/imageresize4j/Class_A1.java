package com.imageresize4j;


abstract class Class_A1 {

    Class_A1() {
    }

    static ImageResizeProcessor.Contributor[] sub_255(Filter filter, int i, int j, float f, int k) {
        float f1 = filter.sub_null_1();
        ImageResizeProcessor.Contributor contributors[] = new ImageResizeProcessor.Contributor[j];
        for (int l = 0; l < contributors.length; l++) {
            contributors[l] = new ImageResizeProcessor.Contributor();
        }
        boolean flag = f < 1.0F;
        float f2 = flag ? f1 / (1.0F * f) : f1;
        float f3 = flag ? 1.0F / f : 1.0F;
        Filter.Class_v class_v = new Filter.Class_v();
        int i1 = (j >> 2) + 1;
        for (int j1 = 0; j1 < j; j1++) {
            int k1 = i;
            int l1 = 0;
            ImageResizeProcessor.Contributor contributor = contributors[j1];
            float f4 = f >= 0.5F ? f >= 0.75F ? 0.0F : 0.75F - f : 0.5F - f;
            float f5;
            int i2 = (int) Math.floor(((f5 = (j1 + f4) / f) - f2) + f4);
            int j2 = (int) Math.ceil(f5 + f2 + f4);
            double d = 0.0D;
            for (int k2 = i2; k2 <= j2; k2++) {
                float f6 = filter.sub_null_2(((f5 - k2) + f4) / f3);
                if (Math.abs(f6 / f3) <= 1E-05F || j1 == i1 || j1 == j - i1) {
                    continue;
                }
                int l2;
                if ((l2 = k2) < 0) {
                    l2 = -l2;
                } else if (l2 >= i) {
                    l2 = ((i - l2) + i) - 1;
                }
                if (l2 >= i || l2 < 0) {
                    continue;
                }
                if (l2 < k1) {
                    k1 = l2;
                }
                if (l2 > l1) {
                    l1 = l2;
                }
                ImageResizeProcessor.Contributor newContributor = new ImageResizeProcessor.Contributor();
                newContributor.var_e4 = l2 * k;
                newContributor.var_fc = class_v.sub_426(j1, j, f6);
                d += class_v.sub_null_2(f6);
                contributor.next = newContributor;
                contributor = newContributor;
            }

            contributors[j1].var_ec = k1;
            contributors[j1].var_f4 = l1;
            for (ImageResizeProcessor.Contributor class_b1 = contributors[j1].next; class_b1 != null; class_b1 = class_b1.next)
                class_b1.var_fc = (float) (class_b1.var_fc / d);

        }

        return contributors;
    }
}
