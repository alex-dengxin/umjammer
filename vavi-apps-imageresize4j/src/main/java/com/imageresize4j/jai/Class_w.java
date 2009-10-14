package com.imageresize4j.jai;


abstract class Class_w {

    Class_w() {
    }

    static ImprovedScaleWarpOpImage.Contributor[] sub_2d6(ImprovedScaleInterpolation interpolation, int i, int j, float f, int k, int l, int i1) {
        Filter filter = interpolation.getFilter();
        float f1 = filter.sub_null_1();
        ImprovedScaleWarpOpImage.Contributor contributors[] = new ImprovedScaleWarpOpImage.Contributor[j];
        for (int j1 = 0; j1 < contributors.length; j1++) {
            contributors[j1] = new ImprovedScaleWarpOpImage.Contributor();
        }

        boolean flag = f < 1.0F;
        float f2 = (flag) ? f1 / f : f1;
        float f3 = flag ? 1.0F / f : 1.0F;
        int k1 = i1 >= 0 ? (i1 >> 2) + 1 : -1;
        Filter.Class_C1 class_c = new Filter.Class_C1();
        for (int l1 = 0; l1 < j; l1++) {
            ImprovedScaleWarpOpImage.Contributor contributor = contributors[l1];
            float f4 = f >= 0.5F ? f >= 0.75F ? 0.0F : 0.75F - f : 0.5F - f;
            float f5 = ((l1 + l) + f4);
            int i2 = (int) Math.floor(((f5 / f) - f2) + f4);
            int j2 = (int) Math.ceil(f5 + f2 + f4);
            double d = 0.0D;
            for (int k2 = i2; k2 <= j2; k2++) {
                float f6 = filter.sub_null_2(((f5 - k2) + f4) / f3) / f3;
                if (Math.abs(f6) <= 5E-05F || l1 + l == k1 || l1 + l == i1 - k1) {
                    continue;
                }
                int l2;
                if ((l2 = k2 - k) < 0) {
                    l2 = -l2;
                } else if (l2 >= i) {
                    l2 = ((i - l2) + i) - 1;
                }
                if (l2 < i && l2 >= 0) {
                    ImprovedScaleWarpOpImage.Contributor newContributor = new ImprovedScaleWarpOpImage.Contributor();
                    newContributor.var_ec = l2;
                    newContributor.var_f4 = class_c.sub_49e(l1 + l, i1, f6);
                    d += class_c.sub_null_2(f6);
                    contributor.next = newContributor;
                    contributor = newContributor;
                }
            }

            for (ImprovedScaleWarpOpImage.Contributor aContributor = contributors[l1].next; aContributor != null; aContributor = aContributor.next) {
                aContributor.var_f4 = (float) (aContributor.var_f4 / d);
            }
        }

        return contributors;
    }
}
