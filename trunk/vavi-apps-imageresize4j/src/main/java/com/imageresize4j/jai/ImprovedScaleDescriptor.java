package com.imageresize4j.jai;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PropertyGenerator;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;


public class ImprovedScaleDescriptor extends OperationDescriptorImpl {

    public ImprovedScaleDescriptor() {
        super(var_f69, new String[] {
            "rendered", "renderable"
        }, 1, var_f79, argClasses, null, null);
    }

    public PropertyGenerator[] getPropertyGenerators(String s) {
        if (s == null)
            throw new IllegalArgumentException("modeName may not be null");
        if (!"rendered".equalsIgnoreCase(s)) {
            PropertyGenerator apropertygenerator[] = new PropertyGenerator[1];
            apropertygenerator[0] = new v_PropertyGenerator();
            return apropertygenerator;
        } else {
            return null;
        }
    }

    protected boolean validateParameters(String s, ParameterBlock parameterblock, StringBuffer stringbuffer) {
        if (parameterblock.getNumParameters() < 2 || parameterblock.getObjectParameter(1) == null || parameterblock.getObjectParameter(0) == null) {
            if (parameterblock.getObjectParameter(1) == null)
                parameterblock.set(parameterblock.getObjectParameter(0), 1);
            if (parameterblock.getObjectParameter(0) == null)
                parameterblock.set(parameterblock.getObjectParameter(1), 0);
        }
        if (!super.validateParameters(s, parameterblock, stringbuffer))
            return false;
        float f = parameterblock.getFloatParameter(0);
        float f1 = parameterblock.getFloatParameter(1);
        if (f <= 0.0F || f1 <= 0.0F) {
            stringbuffer.append(getName()).append(" operation requires positive scale factors.");
            return false;
        } else {
            return true;
        }
    }

    public static void registerImprovedScaleDescriptor() {
        JAI.getDefaultInstance().getOperationRegistry();
        new ImprovedScaleDescriptor();
        new ImprovedScaleCRIF();
    }

    public static RenderedOp create(RenderedImage renderedimage, Float scaleX, Float scaleY, ImprovedScaleInterpolation interpolation, RenderingHints renderinghints) {
        ParameterBlockJAI parameterblockjai;
        (parameterblockjai = new ParameterBlockJAI("ImprovedScale", "rendered")).setSource("source0", renderedimage);
        parameterblockjai.setParameter("scaleX", scaleX);
        parameterblockjai.setParameter("scaleY", scaleY);
        parameterblockjai.setParameter("interpolation", interpolation);
        return JAI.create("ImprovedScale", parameterblockjai, renderinghints);
    }

    public static RenderableOp createRenderable(RenderableImage source, Float scaleX, Float scaleY, ImprovedScaleInterpolation interpolation, RenderingHints hints) {
        ParameterBlockJAI parameterblockjai;
        (parameterblockjai = new ParameterBlockJAI("Scale", "renderable")).setSource("source0", source);
        parameterblockjai.setParameter("scaleX", scaleX);
        parameterblockjai.setParameter("scaleY", scaleY);
        parameterblockjai.setParameter("interpolation", interpolation);
        return JAI.createRenderable("ImprovedScale", parameterblockjai, hints);
    }

    public static RenderedOp createResize(RenderedImage renderedimage, Integer integer, Integer integer1, ImprovedScaleInterpolation improvedscaleinterpolation, RenderingHints hints) {
        Float float1 = null;
        Float float2 = null;
        if (integer != null)
            float1 = new Float(integer.floatValue() / renderedimage.getWidth());
        if (integer1 != null)
            float2 = new Float(integer1.floatValue() / renderedimage.getHeight());
        return create(renderedimage, float1, float2, improvedscaleinterpolation, hints);
    }

    public static RenderableOp createResizeRenderable(RenderableImage renderableimage, Integer integer, Integer integer1, ImprovedScaleInterpolation improvedscaleinterpolation, RenderingHints renderinghints) {
        Float float1 = null;
        Float float2 = null;
        if (integer != null)
            float1 = new Float(integer.floatValue() / renderableimage.getWidth());
        if (integer1 != null)
            float2 = new Float(integer1.floatValue() / renderableimage.getHeight());
        return createRenderable(renderableimage, float1, float2, improvedscaleinterpolation, renderinghints);
    }

    static Class<?> _mthclass$(String s) {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            throw (Error) (new NoClassDefFoundError()).initCause(e);
        }
    }

    static final String name = "ImprovedScale";

    private static final String var_f69[][] = {
        {
            "GlobalName", "ImprovedScale"
        }, {
            "LocalName", "ImprovedScale"
        }, {
            "Vendor", "com.imageresize4j.jai"
        }, {
            "Description", "Scales an image."
        }, {
            "DocURL", ""
        }, {
            "Version", "1.0"
        }, {
            "arg0Desc", "The X scale factor."
        }, {
            "arg1Desc", "The Y scale factor."
        }, {
            "arg2Desc", "The interpolation method."
        }
    };

    private static final Class<?> argClasses[] = {
        java.lang.Float.class,
        java.lang.Float.class,
        com.imageresize4j.jai.ImprovedScaleInterpolation.class
    };

    private static final String var_f79[] = {
        "scaleX", "scaleY", "interpolation"
    };
}
