package com.imageresize4j.jai;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertyGenerator;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpOpImage;


final class v_PropertyGenerator implements PropertyGenerator {

    public v_PropertyGenerator() {
    }

    public final String[] getPropertyNames() {
        String as[] = new String[1];
        as[0] = "ROI";
        return as;
    }

    public final Class<?> getClass(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName may not be null");
        }
        if (propertyName.equalsIgnoreCase("roi")) {
            if (roiClass == null) {
                return roiClass = javax.media.jai.ROI.class;
            } else {
                return roiClass;
            }
        } else {
            return null;
        }
    }

    public final boolean canGenerateProperties(Object opName) {
        if (opName == null) {
            throw new IllegalArgumentException("opNode may not be null");
        } else {
            return opName instanceof RenderedOp;
        }
    }

    public final Object getProperty(String propertyName, Object opName) {
        if (propertyName == null || opName == null) {
            throw new IllegalArgumentException("Neither parameter may be null");
        }
        if (!canGenerateProperties(opName)) {
            throw new IllegalArgumentException(opName.getClass().getName() + " is not a supported class");
        }
        if (opName instanceof RenderedOp) {
            return getProperty(propertyName, (RenderedOp) opName);
        } else {
            return null;
        }
    }

    public final Object getProperty(String propertyName, RenderedOp renderedop) {
        if (propertyName == null || renderedop == null) {
            throw new IllegalArgumentException("Neither parameter may be null");
        }
        if (propertyName.equals("roi")) {
            ParameterBlock parameterBlock = renderedop.getParameterBlock();
            PlanarImage planarImage = (PlanarImage) parameterBlock.getRenderedSource(0);
            Object obj = planarImage.getProperty("ROI");
            if (obj == null || obj.equals(Image.UndefinedProperty) || !(obj instanceof ROI)) {
                return null;
            }
            ROI roi = (ROI) obj;
            Rectangle rectangle;
            PlanarImage planarimage1 = renderedop.getRendering();
            if ((planarimage1 instanceof WarpOpImage) && ((WarpOpImage) planarimage1).getBorderExtender() == null) {
                WarpOpImage warpopimage = (WarpOpImage) planarimage1;
                Interpolation interpolation = warpopimage.getInterpolation();
                int w1 = interpolation.getWidth() <= 0 ? 0 : 1;
                int h1 = interpolation.getHeight() <= 0 ? 0 : 1;
                rectangle = new Rectangle(planarImage.getMinX() + interpolation.getLeftPadding(), planarImage.getMinY() + interpolation.getTopPadding(), (planarImage.getWidth() - interpolation.getWidth()) + w1, (planarImage.getHeight() - interpolation.getHeight()) + h1);
            } else {
                rectangle = planarImage.getBounds();
            }
            if (!rectangle.contains(roi.getBounds())) {
                roi = roi.intersect(new ROIShape(rectangle));
            }
            float p1 = parameterBlock.getFloatParameter(0);
            float p2 = parameterBlock.getFloatParameter(1);
            AffineTransform affinetransform = new AffineTransform(p1, 0.0D, 0.0D, p2, 0.0D, 0.0D);
            ROI newRoi = roi.transform(affinetransform);
            Rectangle rectangle1 = renderedop.getBounds();
            if (!rectangle1.contains(newRoi.getBounds())) {
                newRoi = newRoi.intersect(new ROIShape(rectangle1));
            }
            return newRoi;
        } else {
            return null;
        }
    }

    public final Object getProperty(String s, RenderableOp renderableop) {
        if (s == null || renderableop == null) {
            throw new IllegalArgumentException("Neither parameter may be null");
        } else {
            return null;
        }
    }

    static Class<?> roiClass;
}
