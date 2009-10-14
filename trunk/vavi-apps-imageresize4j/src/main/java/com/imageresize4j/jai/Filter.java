package com.imageresize4j.jai;


abstract class Filter {

    static class Class_p extends Class_z {

        final float sub_21f(int i, int j, float f) {
            int k = var_1cd[(i & 0x3f) >> 5] * var_1d5[(j & 0x3f) >> 3];
            float f1;
            if ((f1 = var_185.sub_11a(i & 0x1f, j & 7, f * k)) <= 0.0F)
                return var_185.sub_null_1() * f;
            else
                return f1 * f;
        }

        final double sub_27b(int i, int j, double d) {
            int k = var_1cd[(i & 0x3f) >> 5] * var_1d5[(j & 0x3f) >> 3];
            double d1;
            if ((d1 = var_185.sub_156(i & 0x1f, j & 7, d * k)) <= 0.0D)
                return var_185.sub_null_1() * d;
            else
                return d1 * d;
        }

        public final float sub_2d8() {
            return 32F;
        }

        private Class_x var_185;

        static final int var_18d = 63;

        static final int var_19d = 31;

        static final int var_1ad = 15;

        static final int var_1bd = 7;

        static final int var_1cd[] = {
            1, 0
        };

        static final int var_1d5[] = {
            1, 0, 0, 0, 0, 0, 0, 0
        };

        Class_p() {
            var_185 = new Class_x();
        }
    }

    static class Class_G1 extends Class_z {

        final float sub_17e(int i, int j, float f) {
            float f1;
            if ((f1 = var_134.sub_11a((int) (sub_24c() - i % (2.0F * sub_24c()) - 1.0F), j % 8, f)) <= 0.0F)
                return (var_134.sub_null_1() * f) / 2.0F + (sub_24c() * sub_24c()) / (4F * var_134.sub_null_1());
            else
                return f1 * f;
        }

        final double sub_1e3(int i, int j, double d) {
            double d1;
            if ((d1 = var_134.sub_156((int) (sub_24c() - i % (2.0F * sub_24c()) - 1.0F), j % 8, d)) <= 0.0D)
                return (var_134.sub_null_1() * d) / 2D + ((sub_24c() * sub_24c()) / (4F * var_134.sub_null_1()));
            else
                return d1 * d;
        }

        public final float sub_24c() {
            return 32F;
        }

        private Class_x var_134;

        Class_G1() {
            var_134 = new Class_x();
        }
    }

    static class Class_x extends Class_z {

        final float sub_11a(int i, int j, float f) {
            if (f <= 0.0F)
                return 1.7689F;
            else
                return (1.0F - ((sub_277(j) & (1 << 31 - i)) >> 31 - i)) * 1.7689F;
        }

        final double sub_156(int i, int j, double d) {
            if (d <= 0.0D)
                return 1.7689000368118286D;
            else
                return (1.0D - ((sub_277(j) & (1 << 31 - i)) >> 31 - i)) * 1.7688999999999999D;
        }

        public final float sub_null_1() {
            return 1.5F;
        }

        Class_x() {
        }
    }

    static class Class_b extends Filter {

        public final float sub_null_2(float f) {
            return f / sub_null_1() + 1.0F;
        }

        public final float sub_null_1() {
            return 4F;
        }

        Class_b() {
        }
    }

    static class Class_C1 extends Filter {

        public final float sub_null_2(float f) {
            return f * sub_null_1();
        }

        final float sub_16d(int i, int j, float f) {
            if (i == (int) var_102.sub_null_2(j) || i == j - (int) var_102.sub_null_2(j))
                return f * (4F - var_102.sub_null_1());
            else
                return f;
        }

        public final float sub_null_1() {
            return 1.33F;
        }

        private Filter var_102;

        Class_C1() {
            var_102 = new Class_b();
        }
    }

    static class Class_f extends Filter {

        public final float sub_null_2(float f) {
            return f % (var_e5.sub_null_1() * sub_null_1());
        }

        public final float sub_null_1() {
            return 2.0F;
        }

        private Filter var_e5;

        Class_f() {
            var_e5 = new Class_z();
        }
    }

    static class Class_y extends Filter {

        public final float sub_null_2(float f) {
            if (f < sub_null_1())
                return sub_null_1();
            else
                return -f;
        }

        final float sub_21b(int i, int j, float f) {
            float f1;
            if ((f1 = var_180.sub_null_2(var_188.sub_null_2(var_190.sub_null_2(j)) * var_188.sub_null_2((2.0F * var_190.sub_null_2(i)) / var_188.sub_null_1()) * (var_190.sub_null_2(j) * var_180.sub_null_1() + var_190.sub_null_2(i)))) <= 0.0F)
                return (f * sub_null_1()) / 2.0F + (var_188.sub_null_2(f1 * f) * var_180.sub_null_1() * var_188.sub_null_1()) / sub_null_1();
            else
                return f1 * f;
        }

        final double sub_2ba(int i, int j, double d) {
            float f;
            if ((f = var_180.sub_null_2(var_188.sub_null_2(var_190.sub_null_2(j)) * var_188.sub_null_2((2.0F * var_190.sub_null_2(i)) / var_188.sub_null_1()) * (var_190.sub_null_2(j) * var_180.sub_null_1() + var_190.sub_null_2(i)))) <= 0.0F)
                return (d * sub_null_1()) / 2D + (Filter.sub_482(f * d) * var_180.sub_null_1() * var_188.sub_null_1()) / sub_null_1();
            else
                return f * d;
        }

        public final float sub_null_1() {
            return 1.5F;
        }

        private Filter var_180;

        private Filter var_188;

        private Filter var_190;

        Class_y() {
            var_180 = new Class_z();
            var_188 = new Class_a();
            var_190 = new Class_f();
        }
    }

    static class Class_a extends Filter {

        public final float sub_null_2(float f) {
            return f >= sub_null_1() ? 0.0F : 1.0F;
        }

        public final float sub_null_1() {
            return 8F;
        }

        Class_a() {
        }
    }

    static class Class_z extends Filter {

        public final float sub_null_2(float f) {
            if (f > 0.0F)
                return ((sub_251(f) & (1 << (int) (((sub_null_1() - f) + (int) (f / sub_null_1()) * sub_null_1()) - 1.0F))) >> (int) (((sub_null_1() - f) + (int) (f / sub_null_1()) * sub_null_1()) - 1.0F)) * -1.7689F + 1.7689F;
            else
                return 1.7689F;
        }

        protected final long sub_251(float f) {
            return sub_277((int) (f / sub_null_1()));
        }

        protected final long sub_277(int i) {
            return Double.doubleToLongBits(var_175[i]);
        }

        float sub_29b(int i, int j, float f) {
            return sub_49e(i, j, f);
        }

        public float sub_null_1() {
            return 32F;
        }

        double var_175[] = {
            7.3166376846186566E-315D, 7.0264763448097053E-315D, 7.0679211007989758E-315D, 7.0679258438291758E-315D, 7.3994826269352862E-315D, 7.0679207845969624E-315D, 7.1145466686757892E-315D, 6.9863710057269669E-315D
        };

        Class_z() {
        }
    }

    static class NerarestNeighborFilter extends Filter {

        public final float sub_null_2(float f) {
            return f <= -0.5F || f > 0.5F ? 0.0F : 1.0F;
        }

        public final float sub_null_1() {
            return 0.5F;
        }

        NerarestNeighborFilter() {
        }
    }

    static class Lanczos3Filter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0F)
                f = -f;
            if (f < 3F)
                return (float) (sub_4d6(f) * sub_4d6(f / 3D));
            else
                return 0.0F;
        }

        public final float sub_null_1() {
            return 3F;
        }

        Lanczos3Filter() {
        }
    }

    static class SharpMore5Filter extends Class_l {

        static double var_151[] = {
            1.0D, 0.64158213887595483D, 0.12279755459449922D, -0.37260139830720546D, -0.38206636937596578D, -0.15967001197374861D, 0.14053831094223743D, 0.19845686843122962D, 0.10849018900694445D, -0.051415204539439659D, -0.091647180195294914D, -0.058981632260955277D, 0.011808298413412478D, 0.024268846298998583D, 0.016724647647395771D, 0.0D, -0.017037094860105597D
        };

        public SharpMore5Filter() {
            super(5D, var_151);
        }
    }

    static class SharpMore3Filter extends Class_l {

        static double var_13f[] = {
            1.0D, 0.72654815611440815D, 0.32928098589204818D, -0.085692689884251202D, -0.37106982621995643D, -0.37007814069721384D, -0.21867169074770149D, -0.018065493111099853D, 0.086941918116560615D, 0.098661805006191525D, 0.059096770768313489D, 0.0080931440606260632D, -0.0091455264986868724D, -0.010601627234754158D
        };

        public SharpMore3Filter() {
            super(3D, var_13f);
        }
    }

    static class SharpLight5Filter extends Class_l {

        static double var_15a[] = {
            1.0D, 0.76131894461873939D, 0.34740537937853344D, -0.04477046161267368D, -0.21435935845918747D, -0.14121885743809853D, 0.0061425941515969029D, 0.10495657306187614D, 0.070560716586366867D, -0.004812676312782968D, -0.054721351308016232D, -0.034527290264678052D, 0.0030298797726524694D, 0.018782078042245694D, 0.0099774703321944529D, -0.00210462196107657D, -0.036623470765502208D
        };

        public SharpLight5Filter() {
            super(5D, var_15a);
        }
    }

    static class SharpLight3Filter extends Class_l {

        static double var_13f[] = {
            1.0D, 0.86584457777599522D, 0.58007574670544759D, 0.265689400452296D, -0.018712967688679815D, -0.15731903819774309D, -0.16990207981951608D, -0.099520214258794357D, 0.00061050610886054929D, 0.036310560073882149D, 0.044263292332038538D, 0.026088274697487951D, -4.2056903366160834E-06D, -0.0037121182923316072D
        };

        public SharpLight3Filter() {
            super(3D, var_13f);
        }
    }

    static class Sharp5Filter extends Class_l {

        static double var_15a[] = {
            1.0D, 0.77025182382356383D, 0.32375671742117257D, -0.13196683717331822D, -0.26845585565592106D, -0.16958362131221913D, 0.022892663766495548D, 0.13013536025148778D, 0.089449726523713996D, -0.0088680973081166053D, -0.065104546078472908D, -0.045958142849695377D, 0.0037797374360233442D, 0.021832457679349482D, 0.012298907824392824D, -0.0035109746517350115D, -0.12961534902297386D
        };

        public Sharp5Filter() {
            super(5D, var_15a);
        }
    }

    static class Sharp3Filter extends Class_l {

        static double var_13f[] = {
            1.0D, 0.84576402890862545D, 0.52312620434965806D, 0.16549460573510535D, -0.13740876952799844D, -0.25389001443473946D, -0.22475137978737622D, -0.10970534820445429D, 0.016966329483115949D, 0.056868046555631269D, 0.058043522293300863D, 0.030492066411408135D, -0.0017184280798370909D, -0.0010510724062658657D
        };

        public Sharp3Filter() {
            super(3D, var_13f);
        }
    }

    static class Ideal5Filter extends Class_l {

        static double var_187[] = {
            1.0D, 0.88814208754916002D, 0.61751277712241004D, 0.28500341954674668D, 0.00018329776501681668D, -0.15308921600765488D, -0.16873295236634014D, -0.093892945620310336D, -0.000120880216590127D, 0.05813944459590862D, 0.064378560534582227D, 0.035236471089619782D, 2.9154416011418785E-06D, -0.019817426889036812D, -0.020003428795154238D, -0.0097403805789003459D, -9.4945379537752616E-06D, 0.0039494778024229786D, 0.0030440173961950034D, 0.001227039369011178D, 7.9803394527584709E-05D,
            -0.0041726864114180898D
        };

        public Ideal5Filter() {
            super(5D, var_187);
        }
    }

    static class Ideal3Filter extends Class_l {

        static double var_13f[] = {
            1.0D, 0.87932219148970459D, 0.59314599885973696D, 0.25931580000178694D, -0.0025129861289902118D, -0.1216782591631709D, -0.12315995526932647D, -0.060999211067421281D, -0.00025643594415092634D, 0.028186267749460446D, 0.023447634041601217D, 0.0059407823328461642D, -0.0022714006581971038D, -0.0060516730010153393D
        };

        public Ideal3Filter() {
            super(3D, var_13f);
        }
    }

    static class Ideal2Filter extends Class_l {

        static double var_11b[] = {
            1.0D, 0.71410596942031102D, 0.38867141959650447D, 0.14065986586049925D, -0.044050885078032669D, -0.067486980951941625D, -0.03448277621987593D, -0.0088823962778690844D, 0.016176193722573166D, 0.10557156751204902D
        };

        public Ideal2Filter() {
            super(2D, var_11b);
        }
    }

    static class HiPass5Filter extends Class_l {

        static double var_187[] = {
            1.0D, 0.88672828269223147D, 0.61511823316461156D, 0.2789175339690465D, 0.00018263152562390789D, -0.15187294197450391D, -0.16745601009542002D, -0.092906241641560999D, -0.00012042573230908405D, 0.057913973035429785D, 0.06412903457482981D, 0.035099833989292568D, 2.9395405270933504E-06D, -0.019740572591641945D, -0.019925853160630752D, -0.0097026062447276261D, -9.4577170260326216E-06D, 0.0039341612659583112D, 0.0030322123410002877D, 0.0012222807669429469D, 7.95087232889365E-05D,
            -0.0041339296035369068D
        };

        public HiPass5Filter() {
            super(5D, var_187);
        }
    }

    static class HiPass3Filter extends Class_l {

        static double var_13f[] = {
            1.0D, 0.86313144295488919D, 0.5543262806360485D, 0.24797734198681415D, -0.012816673772480429D, -0.13673071704955117D, -0.12233761771144888D, -0.059484989341286459D, 0.005267856374742981D, 0.032786024084942203D, 0.023864340516658179D, 0.0079677698809519464D, 0.002518568696903829D, 0.080982021940508297D
        };

        public HiPass3Filter() {
            super(3D, var_13f);
        }
    }

    static class HiPass2Filter extends Class_l {

        static double var_11b[] = {
            1.0D, 0.84317642086573319D, 0.49029520099901214D, 0.19790227864350593D, -0.0090438075945842612D, -0.099181971291664756D, -0.063931952971442343D, -0.011188735609628469D, 0.0046494219313015167D, -0.011063692408117175D
        };

        public HiPass2Filter() {
            super(2D, var_11b);
        }
    }

    static class HammingFilter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0F)
                f = -f;
            if (f > var_124)
                return 0.0F;
            else
                return (float) (sub_4d6(f) * (var_12c + var_134 * StrictMath.cos(f * var_13c)));
        }

        public final float sub_null_1() {
            return var_124;
        }

        private float var_124;

        private double var_12c;

        private double var_134;

        private double var_13c;

        public HammingFilter(float f, float f1) {
            var_124 = 5F;
            var_12c = 0.5D;
            var_134 = 1.0D - var_12c;
            var_13c = 3.1415926535897931D / var_124;
            var_124 = f;
            var_12c = f1;
            var_134 = 1.0F - f1;
            var_13c = 3.1415926535897931D / f;
        }
    }

    static class Class_l extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0F)
                f = -f;
            if (f >= var_1cd) {
                return 0.0F;
            } else {
                double d;
                int i = (int) (d = var_1dd * f);
                double d1 = d - i;
                return (float) sub_313(i, d1);
            }
        }

        final int sub_2c1() {
            return var_1d5.length;
        }

        final double sub_2e1(int i) {
            if (i < 0)
                i = -i;
            if (i >= sub_2c1())
                return 0.0D;
            else
                return var_1d5[i];
        }

        final double sub_313(int i, double d) {
            return sub_34d(sub_2e1(i - 1), sub_2e1(i), sub_2e1(i + 1), sub_2e1(i + 2), d);
        }

        final double sub_34d(double d, double d1, double d2, double d3, double d4) {
            return sub_373(d, d1, d2, d3, d4);
        }

        static double sub_373(double d, double d1, double d2, double d3, double d4) {
            double d5 = 1.5D * (d1 - d2) + 0.5D * (d3 - d);
            double d6 = ((1.0D * d - 2.5D * d1) + 2D * d2) - 0.5D * d3;
            double d7 = 0.5D * (d2 - d);
            double d8 = d1;
            return ((d5 * d4 + d6) * d4 + d7) * d4 + d8;
        }

        static double sub_3db(double d, double d1, double d2, double d3, double d4) {
            double d5 = ((1.0D - 3D * d4) + 3D * d4 * d4) - d4 * d4 * d4;
            double d6 = (4D - 6D * d4 * d4) + 3D * d4 * d4 * d4;
            double d7 = (1.0D + 3D * d4 + 3D * d4 * d4) - 3D * d4 * d4 * d4;
            double d8 = d4 * d4 * d4;
            double d9 = (d * d5 + d1 * d6 + d7 * d2 + d8 * d3) / 6D;
            return d9;
        }

        final void sub_476() {
            double d = var_1d5[0];
            for (int i = 0; i < sub_2c1(); i++)
                if (var_1d5[i] > d)
                    d = var_1d5[i];

            var_1d5[0] = d;
        }

        final void sub_4c1() {
            sub_476();
            double d = var_1d5[0];
            for (int i = 0; i < sub_2c1(); i++)
                var_1d5[i] = var_1d5[i] / d;

        }

        final void sub_505(Filter class_k) {
            int i = sub_2c1() - 1;
            for (int j = 0; j < i; j++)
                var_1d5[j] = class_k.sub_null_2((float) (((double) sub_null_1() * (double) j) / (i - 1)));

            var_1d5[sub_2c1() - 1] = (var_1d5[sub_2c1() - 2] + var_1d5[sub_2c1() - 2]) - var_1d5[sub_2c1() - 3];
        }

        public final float sub_null_1() {
            return (float) var_1cd;
        }

        private double var_1cd;

        double var_1d5[];

        double var_1dd;

        public Class_l(double d, double ad[]) {
            var_1cd = d;
            var_1d5 = ad;
            var_1dd = (sub_2c1() - 2) / d;
        }

        public Class_l(double d, int i, Filter class_k) {
            var_1cd = d;
            var_1d5 = new double[i];
            sub_505(class_k);
            sub_4c1();
            var_1dd = (sub_2c1() - 2) / d;
        }
    }

    static class CombinedFilter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0F)
                f = -f;
            if (f > var_109)
                return 0.0F;
            else
                return (float) (sub_4d6(f) * (var_119 * sub_4d6(f / var_109) + (1.0D - var_119) * sub_509(f, var_111)));
        }

        public final float sub_null_1() {
            return (float) var_109;
        }

        double var_109;

        double var_111;

        double var_119;

        public CombinedFilter(double d, double d1, double d2) {
            var_109 = d;
            var_111 = d1;
            var_119 = d2;
        }

        public CombinedFilter() {
            this(4D, 0.41999999999999998D, 0.080000000000000002D);
        }
    }

    static class BlackManFilter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0F)
                f = -f;
            if (f > 4F)
                return 0.0F;
            else
                return (float) (sub_4d6(f) * (0.41999999999999998D + 0.5D * Math.cos((3.1415926535897931D * f) / 4D) + 0.080000000000000002D * Math.cos((6.2831853071795862D * f) / 4D)));
        }

        public final float sub_null_1() {
            return 4F;
        }

        BlackManFilter() {
        }
    }

    static class BiLenearFilter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0D)
                f = -f;
            if (f < 1.0D)
                return 1.0F - f;
            else
                return 0.0F;
        }

        public final float sub_null_1() {
            return 1.0F;
        }

        BiLenearFilter() {
        }
    }

    static class BiCubicFilter extends Filter {

        public final float sub_null_2(float f) {
            if (f < 0.0D)
                f = -f;
            if (f < 1.0D)
                return (2.0F * f - 3F) * f * f + 1.0F;
            else
                return 0.0F;
        }

        public final float sub_null_1() {
            return 1.0F;
        }

        BiCubicFilter() {
        }
    }

    Filter() {
    }

    abstract float sub_null_1();

    abstract float sub_null_2(float f);

    static double sub_482(double d) {
        return d;
    }

    float sub_49e(int i, int j, float f) {
        return f;
    }

    double sub_4ba(int i, int j, double d) {
        return d;
    }

    static double sub_4d6(double d) {
        d *= 3.1415926535897931D;
        if (1.0D + d * d == 1.0D)
            return 1.0D;
        else
            return Math.sin(d) / d;
    }

    static double sub_509(double d, double d1) {
        return Math.pow(2D, -((d * d) / (d1 * d1)));
    }
}
