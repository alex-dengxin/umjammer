package com.imageresize4j.jai;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;

import javax.media.jai.BorderExtender;
import javax.media.jai.CRIFImpl;
import javax.media.jai.JAI;

import com.sun.media.jai.opimage.RIFUtil;


public class ImprovedScaleCRIF extends CRIFImpl {

    public ImprovedScaleCRIF() {
        super("ImprovedScale");
    }

    public RenderedImage create(ParameterBlock parameterblock, RenderingHints renderinghints) {
        javax.media.jai.ImageLayout imagelayout = RIFUtil.getImageLayoutHint(renderinghints);
        RenderedImage renderedimage = parameterblock.getRenderedSource(0);
        float f = parameterblock.getFloatParameter(0);
        float f1 = parameterblock.getFloatParameter(1);
        ImprovedScaleInterpolation improvedscaleinterpolation = (ImprovedScaleInterpolation) parameterblock.getObjectParameter(2);
        if (improvedscaleinterpolation == null) {
            improvedscaleinterpolation = ImprovedScaleInterpolation.IDEAL_2;
        }
        if (f == 1.0F && f1 == 1.0F) {
            return renderedimage;
        } else {
            BorderExtender borderextender = renderinghints != null ? (BorderExtender) renderinghints.get(JAI.KEY_BORDER_EXTENDER) : null;
            return new ImprovedScaleWarpOpImage(renderedimage, imagelayout, renderinghints, borderextender, f, f1, improvedscaleinterpolation);
        }
    }

    public RenderedImage create(RenderContext rendercontext, ParameterBlock parameterblock) {
        return parameterblock.getRenderedSource(0);
    }

    public RenderContext mapRenderContext(int i, RenderContext rendercontext, ParameterBlock parameterblock, RenderableImage renderableimage) {
        float f = parameterblock.getFloatParameter(0);
        float f1 = parameterblock.getFloatParameter(1);
        AffineTransform affinetransform = new AffineTransform(f, 0.0D, 0.0D, f1, 0.0D, 0.0D);
        RenderContext rendercontext1 = (RenderContext) rendercontext.clone();
        AffineTransform affinetransform1 = rendercontext1.getTransform();
        affinetransform1.concatenate(affinetransform);
        rendercontext1.setTransform(affinetransform1);
        return rendercontext1;
    }

    public Rectangle2D getBounds2D(ParameterBlock parameterblock) {
        RenderableImage renderableimage = parameterblock.getRenderableSource(0);
        float scaleX = parameterblock.getFloatParameter(0);
        float scaleY = parameterblock.getFloatParameter(1);
        float x = renderableimage.getMinX();
        float y = renderableimage.getMinY();
        float width = renderableimage.getWidth();
        float height = renderableimage.getHeight();
        float scaledX = x * scaleX;
        float scaledY = y * scaleY;
        float scaledWidth = width * scaleX;
        float scaledHeight = height * scaleY;
        return new Rectangle2D.Float(scaledX, scaledY, scaledWidth, scaledHeight);
    }
}
