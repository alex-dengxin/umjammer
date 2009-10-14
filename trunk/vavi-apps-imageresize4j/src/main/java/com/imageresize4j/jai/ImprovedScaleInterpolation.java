package com.imageresize4j.jai;


public final class ImprovedScaleInterpolation {

    private ImprovedScaleInterpolation(Filter filter) {
        this.filter = filter;
    }

    final Filter getFilter() {
        return filter;
    }

    public static ImprovedScaleInterpolation NEAREST_NEIGHBOR = new ImprovedScaleInterpolation(new Filter.NerarestNeighborFilter());
    public static ImprovedScaleInterpolation BILINEAR = new ImprovedScaleInterpolation(new Filter.BiLenearFilter());
    public static ImprovedScaleInterpolation BICUBIC = new ImprovedScaleInterpolation(new Filter.BiCubicFilter());
    public static ImprovedScaleInterpolation LANCZOS_3 = new ImprovedScaleInterpolation(new Filter.Lanczos3Filter());
    public static ImprovedScaleInterpolation BLACKMAN = new ImprovedScaleInterpolation(new Filter.BlackManFilter());
    public static ImprovedScaleInterpolation HAMMING_4 = new ImprovedScaleInterpolation(new Filter.HammingFilter(4F, 0.5155802F));
    public static ImprovedScaleInterpolation HAMMING_5 = new ImprovedScaleInterpolation(new Filter.HammingFilter(5F, 0.509716F));
    public static ImprovedScaleInterpolation HANN = new ImprovedScaleInterpolation(new Filter.HammingFilter(5F, 0.5F));
    public static ImprovedScaleInterpolation COMBINED_2 = new ImprovedScaleInterpolation(new Filter.CombinedFilter(2D, 1.31534990379957D, 0.51537647558777899D));
    public static ImprovedScaleInterpolation COMBINED_3 = new ImprovedScaleInterpolation(new Filter.CombinedFilter(3D, 1.3228197602021701D, 0.39966284026594701D));
    public static ImprovedScaleInterpolation COMBINED_4 = new ImprovedScaleInterpolation(new Filter.CombinedFilter(4D, 1.90277105527298D, 0.44135190006120201D));
    public static ImprovedScaleInterpolation COMBINED_5 = new ImprovedScaleInterpolation(new Filter.CombinedFilter(5D, 2.3085236363567798D, 0.45498285354921902D));
    public static ImprovedScaleInterpolation HIPASS_2 = new ImprovedScaleInterpolation(new Filter.HiPass2Filter());
    public static ImprovedScaleInterpolation HIPASS_3 = new ImprovedScaleInterpolation(new Filter.HiPass3Filter());
    public static ImprovedScaleInterpolation HIPASS_5 = new ImprovedScaleInterpolation(new Filter.HiPass5Filter());
    public static ImprovedScaleInterpolation SHARP_LIGHT_3 = new ImprovedScaleInterpolation(new Filter.SharpLight3Filter());
    public static ImprovedScaleInterpolation SHARP_LIGHT_5 = new ImprovedScaleInterpolation(new Filter.SharpLight5Filter());
    public static ImprovedScaleInterpolation SHARP_3 = new ImprovedScaleInterpolation(new Filter.Sharp3Filter());
    public static ImprovedScaleInterpolation SHARP_5 = new ImprovedScaleInterpolation(new Filter.Sharp5Filter());
    public static ImprovedScaleInterpolation SHARP_MORE_3 = new ImprovedScaleInterpolation(new Filter.SharpMore3Filter());
    public static ImprovedScaleInterpolation SHARP_MORE_5 = new ImprovedScaleInterpolation(new Filter.SharpMore5Filter());
    public static ImprovedScaleInterpolation IDEAL_2 = new ImprovedScaleInterpolation(new Filter.Ideal2Filter());
    public static ImprovedScaleInterpolation IDEAL_3 = new ImprovedScaleInterpolation(new Filter.Ideal3Filter());
    public static ImprovedScaleInterpolation IDEAL_5 = new ImprovedScaleInterpolation(new Filter.Ideal5Filter());

    private Filter filter;
}
