/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.clip;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.sound.sampled.AudioFormat;

import ch.laoe.audio.Audio;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GEditableSegments;
import ch.laoe.ui.GMain;
import ch.laoe.ui.GPersistance;
import ch.laoe.ui.GProgressViewer;
import ch.oli4.io.XmlInputStream;
import ch.oli4.io.XmlOutputStream;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * clip fileformat saver/loader. file format definition: XML format is used, the filename-extension is ".laoe". it is gzipped.
 * "complete" clip-types describe a complete clip, "partial" clip describes only relative changes to the previous clip. complete
 * clips need all attributes of the tags, partial clips only attributes to identify the tag and the changed attributes. the
 * "length"-attribute in the samples-tag is mandatory. following fields are optional on partial clips: layer, channel, samples,
 * selection. all data outside the "laoe" field are ignored. the fileformat is identified through the laoe-tag and its attribute
 * "fileformat".
 * 
 * the file consists of two parts, the xml-header described below, which contains general informations, and the samples-body,
 * which is outside the xml-part and contains all sample values, as binary streamed float-values.
 * 
 * <pre>
 *        
 *  &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 *  &lt;laoe version=&quot;v0.3.17 alpha&quot; fileformat=&quot;one&quot; samples=&quot;below/extern&quot;&gt;
 *  &lt;clip name=&quot;about.laoe&quot; samplerate=&quot;44100&quot; samplewidth=&quot;16&quot; comment=&quot;created by LAoE! friday 13.07.2001 oli4&quot;&gt;
 *  &lt;audio loopEndPointer=&quot;4423&quot; loopStartPointer=&quot;4079&quot;/&gt;
 *  &lt;layer index=&quot;0&quot; name=&quot;layer 1&quot; type=&quot;audioLayer&quot;&gt;
 *  &lt;channel index=&quot;0&quot; name=&quot;channel 1&quot; id=&quot;ch0123&quot; audible=&quot;true&quot;&gt;
 *  &lt;samples length=&quot;1200&quot;/&gt;
 *  &lt;selection name=&quot;selection 1&quot; offset=&quot;0&quot; length=&quot;10030&quot;/&gt;
 *  &lt;plotter xOffset=&quot;0&quot; xLength=&quot;10030&quot; yOffset=&quot;-128&quot; yLength=&quot;256&quot;/&gt;
 *  &lt;/channel&gt;
 *   ...
 *  &lt;/layer&gt;
 *   ...
 *  &lt;/clip&gt;
 *  &lt;/laoe&gt;
 *   optional binary serialized samples in the same order than described in the XML-header...
 *       
 *   Since LAoE version 0.4.04 beta the channel-selection may optionally contain intensity-information.
 *   So the selection may be an element with separate begin- and end-tag in addition to the old data-less
 *   version. Backward-compatibility must be guaranteed!!! Here an example:
 *   ...
 *  &lt;channel index=&quot;0&quot; name=&quot;channel 1&quot; id=&quot;ch0123&quot; audible=&quot;true&quot;&gt;
 *  &lt;samples length=&quot;1200&quot;/&gt;
 *  &lt;selection name=&quot;selection 1&quot; offset=&quot;0&quot; length=&quot;10030&quot;&gt;
 *  &lt;intensity x=&quot;0&quot; y=&quot;.49&quot;/&gt;
 *  &lt;intensity x=&quot;.4&quot; y=&quot;1&quot;/&gt;
 *  &lt;intensity x=&quot;.735&quot; y=&quot;.22&quot;/&gt;
 *  &lt;intensity x=&quot;1&quot; y=&quot;0&quot;/&gt;
 *  &lt;/selection&gt;
 *  &lt;plotter xOffset=&quot;0&quot; xLength=&quot;10030&quot; yOffset=&quot;-128&quot; yLength=&quot;256&quot;/&gt;
 *  &lt;/channel&gt;
 *   ...
 *       
 *   Since LAoE version 0.4.05 beta the channel may optionally contain a mask.Backward-compatibility
 *   must be guaranteed!!! Here an example:
 *   ...
 *  &lt;channel index=&quot;0&quot; name=&quot;channel 1&quot; id=&quot;ch0123&quot; audible=&quot;true&quot;&gt;
 *  &lt;mask name=&quot;mask 1&quot;&gt;
 *  &lt;volumePoint x=&quot;12345&quot; y=&quot;.34&quot;/&gt;
 *  &lt;volumePoint x=&quot;48347&quot; y=&quot;.99&quot;/&gt;
 *  &lt;/mask&gt;
 *   ...
 *  &lt;/channel&gt;
 *   ...
 *       
 *   The layer contains a new optional attribute &quot;plotType&quot;: at the time, two different plottypes exist: sample-curve
 *   and spectrogram.
 *   ...
 *  &lt;layer index=&quot;0&quot; name=&quot;layer 1&quot; type=&quot;audioLayer&quot; plotType=&quot;spectrogram&quot;&gt;
 *   ...
 *       
 *   Since LAoE version 0.4.08 beta the channel may optionally contain markers.Backward-compatibility
 *   must be guaranteed!!! Here an example:
 *   ...
 *  &lt;channel index=&quot;0&quot; name=&quot;channel 1&quot; id=&quot;ch0123&quot; audible=&quot;true&quot;&gt;
 *   ...
 *  &lt;markers name=&quot;marker 1&quot;&gt;
 *  &lt;markerPoint x=&quot;2557&quot;/&gt;
 *  &lt;markerPoint x=&quot;12345&quot;/&gt;
 *  &lt;/markers&gt;
 *   ...
 *  &lt;/channel&gt;
 *   ...
 *       
 *   Since LAoE version 0.6.02 beta the channel-plotter may optionally contain the color, in
 *   hexadecimal format. Backward-compatibility must be guaranteed!!! Here an example:
 *   ...
 *  &lt;channel index=&quot;0&quot; name=&quot;channel 1&quot; id=&quot;ch0123&quot; audible=&quot;true&quot;&gt;
 *   ...
 *  &lt;plotter xOffset=&quot;0&quot; xLength=&quot;10030&quot; yOffset=&quot;-128&quot; yLength=&quot;256&quot; color=&quot;00FF66&quot;/&gt;
 *  &lt;/channel&gt;
 *   ...
 *    
 * </pre>
 * 
 * @version 13.07.01 first draft oli4 <br>
 *          15.07.01 gzipped and serialized oli4 <br>
 *          18.07.01 put serialized samples at the end oli4 <br>
 *          22.01.02 add intensity to the selection oli4 <br>
 *          03.03.02 add channel-mask oli4 <br>
 *          18.03.02 add layer plottype oli4 <br>
 *          04.07.02 add markers oli4 27.04.2003 <br>
 *          add channel-plotter color oli4
 */
public class AClipStorage {
    /**
     * loads a clip from a file. returns true, if the fileformat was accepted, and false if it was a wrong fileformat
     */
    public static boolean supports(File f) throws IOException {
        return f.getPath().endsWith(".laoe");
    }

    // clip

    /**
     * loads a clip from a file. returns true, if the fileformat was accepted, and false if it was a wrong fileformat
     */
    public static boolean load(AClip c, File f) throws IOException {
        ALayer l = null;
        AChannel ch = null;
        AChannelSelection chSel = null;
        AChannelMask chMask = null;
        AChannelMarker chMarker = null;
        int loopStartPointer = 0;
        int loopEndPointer = 0;

        try {
            // GZip
            GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(f)));
            XmlInputStream is = new XmlInputStream(zis);

            // parse-flags
            boolean xmlHeader = true;
            boolean samplesBelow = true;

            while (xmlHeader) {
                int t = is.read();

                switch (t) {
                case XmlInputStream.SYSTEM_TAG:
                    break;

                case XmlInputStream.BEGIN_TAG:
                    if (is.getTagName().equals("laoe")) {
                        if (is.getAttribute("samples").equals("below")) {
                            samplesBelow = true;
                        } else {
                            samplesBelow = false;
                        }
                    } else if (is.getTagName().equals("clip")) {
                        c.setName(is.getAttribute("name"));
                        c.setSampleRate(Float.parseFloat(is.getAttribute("samplerate")));
                        c.setSampleWidth(Integer.parseInt(is.getAttribute("samplewidth")));
                        c.setComments(is.getAttribute("comment"));
                    } else if (is.getTagName().equals("layer")) {
                        l = new ALayer();
                        l.setName(is.getAttribute("name"));
                        String ty = is.getAttribute("type");
                        if (ty.equals("audioLayer")) {
                            l.setType(ALayer.AUDIO_LAYER);
                        } else if (ty.equals("parameterLayer")) {
                            l.setType(ALayer.PARAMETER_LAYER);
                        }
                        String pt = is.getAttribute("plotType");
                        if (pt != null) {
                            if (pt.equals("sampleCurve")) {
                                l.setPlotType(ALayer.SAMPLE_CURVE_TYPE);
                            } else if (pt.equals("spectrogram")) {
                                l.setPlotType(ALayer.SPECTROGRAM_TYPE);
                            }
                        }
                        c.add(l);
                    } else if (is.getTagName().equals("channel")) {
                        ch = new AChannel();
                        if (!samplesBelow) {
                            ch.setId(is.getAttribute("id"));
                        }
                        ch.setName(is.getAttribute("name"));
                        ch.setAudible(is.getAttribute("audible").equals("true"));
                        l.add(ch);
                    } else if (is.getTagName().equals("selection")) // new version with intensity
                    {
                        chSel = new AChannelSelection();
                        ch.setChannelSelection(chSel);
                        chSel.setChannel(ch);
                        chSel.setName(is.getAttribute("name"));
                        chSel.setOffset(Integer.parseInt(is.getAttribute("offset")));
                        chSel.setLength(Integer.parseInt(is.getAttribute("length")));
                    } else if (is.getTagName().equals("mask")) {
                        chMask = ch.getMask();
                        chMask.setName(is.getAttribute("name"));
                    } else if (is.getTagName().equals("markers")) {
                        chMarker = ch.getMarker();
                        chMarker.setName(is.getAttribute("name"));
                    } else if (is.getTagName().equals("graphicObjects")) {
                        ch.getGraphicObjects().fromXmlElement(is);
                    }
                    break;

                case XmlInputStream.END_TAG:
                    if (is.getTagName().equals("laoe")) {
                        xmlHeader = false;
                    }
                    break;

                case XmlInputStream.BEGIN_END_TAG:
                    if (is.getTagName().equals("selection")) // compatible old intensity-less version
                    {
                        chSel = new AChannelSelection();
                        ch.setChannelSelection(chSel);
                        chSel.setChannel(ch);
                        chSel.setName(is.getAttribute("name"));
                        chSel.setOffset(Integer.parseInt(is.getAttribute("offset")));
                        chSel.setLength(Integer.parseInt(is.getAttribute("length")));
                    } else if (is.getTagName().equals("plotter")) {
                        ch.getChannelPlotter().setXRange(Float.parseFloat(is.getAttribute("xOffset")), Float.parseFloat(is.getAttribute("xLength")));
                        ch.getChannelPlotter().setYRange(Float.parseFloat(is.getAttribute("yOffset")), Float.parseFloat(is.getAttribute("yLength")));

                        if (is.containsAttribute("color")) {
                            l.getLayerPlotter().setColor(new Color(Integer.parseInt(is.getAttribute("color"), 16)));
                        }
                    } else if (is.getTagName().equals("audio")) {
                        loopEndPointer = Integer.parseInt(is.getAttribute("loopEndPointer"));
                        loopStartPointer = Integer.parseInt(is.getAttribute("loopStartPointer"));
                    } else if (is.getTagName().equals("samples")) {
                        ch.sample = new float[Integer.parseInt(is.getAttribute("length"))];
                    } else if (is.getTagName().equals("intensity")) {
                        float x = Float.parseFloat(is.getAttribute("x"));
                        float y = Float.parseFloat(is.getAttribute("y"));
                        chSel.addIntensityPoint(x, y);
                    } else if (is.getTagName().equals("volumePoint")) {
                        float x = Float.parseFloat(is.getAttribute("x"));
                        float y = Float.parseFloat(is.getAttribute("y"));
                        chMask.getSegments().addPoint(x, y);
                    } else if (is.getTagName().equals("markerPoint")) {
                        int x = Integer.parseInt(is.getAttribute("x"));
                        chMarker.addMarker(x);
                    }
                    break;

                case XmlInputStream.DATA_CHUNK:
                    break;

                case XmlInputStream.EOF:
                    xmlHeader = false;
                    break;
                }
            }

            // audio...
            c.getAudio().setLoopEndPointer(loopEndPointer);
            c.getAudio().setLoopStartPointer(loopStartPointer);
            c.getAudio().setEncoding(AudioFormat.Encoding.PCM_SIGNED);
            c.getAudio().setFileType(Audio.fileTypeLaoe);

            if (samplesBelow) {
                // serialized samples below...
                // ObjectInputStream ois = new ObjectInputStream(new Base64InputStream(zis));
                ObjectInputStream ois = new ObjectInputStream(zis);
                GProgressViewer.entrySubProgress("clip", " " + f.getName());
                for (int i = 0; i < c.getNumberOfLayers(); i++) {
                    GProgressViewer.setProgress((i + 1) * 100 / c.getNumberOfLayers());
                    l = c.getLayer(i);
                    GProgressViewer.entrySubProgress("layer", " " + i);
                    for (int j = 0; j < l.getNumberOfChannels(); j++) {
                        GProgressViewer.setProgress((j + 1) * 100 / l.getNumberOfChannels());
                        ch = l.getChannel(j);
                        GProgressViewer.entrySubProgress("channel", " " + j);
                        ch.sample = loadSamples(ois);
                        GProgressViewer.exitSubProgress();
                    }
                    GProgressViewer.exitSubProgress();
                }
                GProgressViewer.exitSubProgress();
                ois.close();
            } else {
                is.close();
            }
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
        }
        return true;
    }

    /**
     * saves a clip to a file.
     */
    public static void save(AClip c, File f) throws IOException {
        save(c, f, true);
    }

    /**
     * saves a clip with link to external samples
     */
    public static void saveWithoutSamples(AClip c, File f) throws IOException {
        save(c, f, false);
    }

    private static void save(AClip c, File f, boolean completeClip) throws IOException {
        try {
            // GZip
            GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            XmlOutputStream os = new XmlOutputStream(zos);

            // XML-header, system tag...
            os.appendSystemTag();
            os.appendCR();

            // laoe...
            Map<String, String> attr = new HashMap<String, String>();
            attr.put("version", GMain.version);
            attr.put("fileformat", "one");
            if (completeClip) {
                attr.put("samples", "below");
            } else {
                attr.put("samples", "extern");
            }
            os.appendBeginTag("laoe", (HashMap) attr);
            os.appendCR();

            // clip...
            attr.clear();
            attr.put("name", c.getName());
            attr.put("samplerate", Float.toString(c.getSampleRate()));
            attr.put("samplewidth", Integer.toString(c.getSampleWidth()));
            attr.put("comment", c.getComments());
            os.appendTab(1);
            os.appendBeginTag("clip", (HashMap) attr);
            os.appendCR();

            // audio
            Audio aud = c.getAudio();
            if (aud != null) {
                attr.clear();
                attr.put("loopStartPointer", Integer.toString(aud.getLoopStartPointer()));
                attr.put("loopEndPointer", Integer.toString(aud.getLoopEndPointer()));
                os.appendTab(2);
                os.appendBeginEndTag("audio", (HashMap) attr);
                os.appendCR();
            }

            for (int i = 0; i < c.getNumberOfLayers(); i++) {
                // layer...
                ALayer l = c.getLayer(i);
                attr.clear();
                attr.put("index", Integer.toString(i));
                attr.put("name", l.getName());
                switch (l.getType()) {
                case ALayer.AUDIO_LAYER:
                    attr.put("type", "audioLayer");
                    break;

                case ALayer.PARAMETER_LAYER:
                    attr.put("type", "parameterLayer");
                    break;
                }
                switch (l.getPlotType()) {
                case ALayer.SAMPLE_CURVE_TYPE:
                    attr.put("plotType", "sampleCurve");
                    break;

                case ALayer.SPECTROGRAM_TYPE:
                    attr.put("plotType", "spectrogram");
                    break;
                }
                os.appendTab(2);
                os.appendBeginTag("layer", (HashMap) attr);
                os.appendCR();

                for (int j = 0; j < l.getNumberOfChannels(); j++) {
                    // channel...
                    AChannel ch = l.getChannel(j);
                    attr.clear();
                    attr.put("index", Integer.toString(j));
                    attr.put("name", ch.getName());
                    attr.put("id", ch.getId());
                    attr.put("audible", String.valueOf(ch.isAudible()));
                    os.appendTab(3);
                    os.appendBeginTag("channel", (HashMap) attr);
                    os.appendCR();

                    // samples
                    float s[] = ch.sample;
                    attr.clear();
                    attr.put("length", Integer.toString(s.length));
                    if (!completeClip) {
                        attr.put("location", ch.getId());
                    }
                    os.appendTab(4);
                    os.appendBeginEndTag("samples", (HashMap) attr);
                    os.appendCR();

                    // selection
                    AChannelSelection sel = ch.getChannelSelection();
                    if (sel != null) {
                        attr.clear();
                        attr.put("name", sel.getName());
                        attr.put("offset", Integer.toString(sel.getOffset()));
                        attr.put("length", Integer.toString(sel.getLength()));
                        os.appendTab(4);
                        os.appendBeginTag("selection", (HashMap) attr);
                        os.appendCR();

                        List<AChannelSelection.Point> intensity = sel.getIntensityPoints();

                        for (int k = 0; k < intensity.size(); k++) {
                            attr.clear();
                            attr.put("x", Float.toString(intensity.get(k).x));
                            attr.put("y", Float.toString(intensity.get(k).y));
                            os.appendTab(5);
                            os.appendBeginEndTag("intensity", (HashMap) attr);
                            os.appendCR();
                        }
                        os.appendTab(4);
                        os.appendEndTag("intensity");
                        os.appendCR();
                    }

                    // mask
                    AChannelMask chMask = ch.getMask();
                    if (chMask != null) {
                        attr.clear();
                        attr.put("name", chMask.getName());
                        os.appendTab(4);
                        os.appendBeginTag("mask", (HashMap) attr);
                        os.appendCR();

                        GEditableSegments seg = chMask.getSegments();

                        for (int k = 0; k < seg.getNumberOfPoints(); k++) {
                            attr.clear();
                            attr.put("x", Float.toString(seg.getPointX(k)));
                            attr.put("y", Float.toString(seg.getPointY(k)));
                            os.appendTab(5);
                            os.appendBeginEndTag("volumePoint", (HashMap) attr);
                            os.appendCR();
                        }
                        os.appendTab(4);
                        os.appendEndTag("mask");
                        os.appendCR();
                    }

                    // marker
                    AChannelMarker chMarker = ch.getMarker();
                    if (chMask != null) {
                        attr.clear();
                        attr.put("name", chMarker.getName());
                        os.appendTab(4);
                        os.appendBeginTag("markers", (HashMap) attr);
                        os.appendCR();

                        for (int k = 0; k < chMarker.getNumberOfMarkers(); k++) {
                            attr.clear();
                            attr.put("x", Integer.toString(chMarker.getMarkerX(k)));
                            os.appendTab(5);
                            os.appendBeginEndTag("markerPoint", (HashMap) attr);
                            os.appendCR();
                        }
                        os.appendTab(4);
                        os.appendEndTag("markers");
                        os.appendCR();
                    }

                    // plotter
                    AChannelPlotter plt = ch.getChannelPlotter();
                    if (plt != null) {
                        attr.clear();
                        attr.put("xOffset", Float.toString(plt.getXOffset()));
                        attr.put("xLength", Float.toString(plt.getXLength()));
                        attr.put("yOffset", Float.toString(plt.getYOffset()));
                        attr.put("yLength", Float.toString(plt.getYLength()));
                        attr.put("color", Integer.toString(l.getLayerPlotter().getColor().getRGB(), 16));
                        os.appendTab(4);
                        os.appendBeginEndTag("plotter", (HashMap) attr);
                        os.appendCR();
                    }

                    // graphic objects
                    if (ch.getGraphicObjects() != null) {
                        ch.getGraphicObjects().toXmlElement(os);
                    }

                    os.appendTab(3);
                    os.appendEndTag("channel");
                    os.appendCR();
                }

                os.appendTab(2);
                os.appendEndTag("layer");
                os.appendCR();
            }

            os.appendTab(1);
            os.appendEndTag("clip");
            os.appendCR();

            os.appendEndTag("laoe");

            if (completeClip) {
                // append serialized samples below...
                // ObjectOutputStream oos = new ObjectOutputStream(new Base64OutputStream(zos));
                ObjectOutputStream oos = new ObjectOutputStream(zos);

                GProgressViewer.entrySubProgress("clip", " " + f.getName());
                for (int i = 0; i < c.getNumberOfLayers(); i++) {
                    GProgressViewer.setProgress((i + 1) * 100 / c.getNumberOfLayers());
                    ALayer l = c.getLayer(i);
                    GProgressViewer.entrySubProgress("layer", " " + i);
                    for (int j = 0; j < l.getNumberOfChannels(); j++) {
                        GProgressViewer.setProgress((j + 1) * 100 / l.getNumberOfChannels());
                        AChannel ch = l.getChannel(j);
                        GProgressViewer.entrySubProgress("channel", " " + j);
                        saveSamples(ch.sample, oos);
                        GProgressViewer.exitSubProgress();
                    }
                    GProgressViewer.exitSubProgress();
                }
                oos.close();
                GProgressViewer.exitSubProgress();
            } else {
                os.appendCR();
                os.close();
            }
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
        }
    }

    // samples

    /**
     * loads a sample-array in serialized form
     */
    private static float[] loadSamples(ObjectInputStream ois) throws IOException {
        try {
            GProgressViewer.entrySubProgress();
            int l = ois.readInt();
            float s[] = new float[l];
            for (int i = 0; i < s.length; i++) {
                s[i] = ois.readFloat();

                // faster progress...
                if ((i & 0x3FF) == 0) {
                    GProgressViewer.setProgress((i + 1) * 100 / s.length);
                }
            }
            GProgressViewer.exitSubProgress();
            return s;
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
            return null;
        }
    }

    /**
     * saves a sample-array in serialized form
     */
    private static void saveSamples(float[] samples, ObjectOutputStream oos) throws IOException {
        try {
            GProgressViewer.entrySubProgress();
            oos.writeInt(samples.length);
            for (int i = 0; i < samples.length; i++) {
                oos.writeFloat(samples[i]);

                // faster progress...
                if ((i & 0x3FF) == 0) {
                    GProgressViewer.setProgress((i + 1) * 100 / samples.length);
                }
            }
            oos.flush();
            GProgressViewer.exitSubProgress();
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
        }
    }

    // separate channel storing (e.g. used in undo-history)

    private static final boolean channelCompressionEnable = GPersistance.createPersistance().getBoolean("history.compression");

    /**
     * saves a sample-array in serialized form
     */
    public static void saveSamples(float[] samples, File f) throws IOException {
        try {
            ObjectOutputStream oos;
            if (channelCompressionEnable) {
                oos = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(f))));
            } else {
                oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            }
            saveSamples(samples, oos);
            oos.close();
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
        }
    }

    /**
     * loads a sample-array in serialized form
     */
    public static float[] loadSamples(File f) throws IOException {
        try {
            ObjectInputStream ois;
            if (channelCompressionEnable) {
                ois = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(f))));
            } else {
                ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
            }
            float s[] = loadSamples(ois);
            ois.close();
            return s;
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
            return null;
        }
    }

}
