/*
 * Copyright (c) 2001, David N. Main, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or
 * promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.anotherbigidea.flash.writers;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.interfaces.SWFShape;
import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.interfaces.SWFText;
import com.anotherbigidea.flash.interfaces.SWFVectors;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.AlphaTransform;
import com.anotherbigidea.flash.structs.ButtonRecord;
import com.anotherbigidea.flash.structs.ButtonRecord2;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.structs.SoundInfo;
import com.anotherbigidea.util.Base64;
import com.anotherbigidea.util.xml.SaxHandlerBase;

import vavi.util.Debug;


/**
 * A SAX2 Handler that drives any implementation of the SWFTagTypes interface
 * and understands the XML Vocabulary produced by SWFSaxParser.
 *
 * This class is not thread-safe but it can be reused by setting a different
 * SWFTagTypes value.  Reuse is recommended since the initialization overhead
 * is expensive.
 */
public class SWFSaxWriter extends SaxHandlerBase {
    protected SWFTagTypes tags;
    protected SWFTagTypes movieTags;
    protected SWFShape shape;
    protected SWFVectors vectors;
    protected SWFActions actions;
    protected SWFText text;
    /** 1=button actions, 2=clip actions */
    protected int actionMode = 0;
    protected int flashVersion = 5;
    /** whether to auto allocate ids */
    protected boolean idAllocate = false;
    protected int newId = 1;
    protected Matrix matrix;
    protected Color color;
    protected AlphaTransform cxform;
    protected boolean hasAlpha;
    protected Map<String,Integer> ids = new HashMap<String,Integer>();

    /** for buttons */
    protected List<ButtonRecord2> buttonRecords = new ArrayList<ButtonRecord2>();

    /** for bitmaps/jpeg: */
    protected List<Color> bitmapColors;
    protected byte[] pixelData;
    protected byte[] jpegAlpha;

    /** for gradients: */
    protected List<Color> colors;
    protected List<Integer> ratios;

    /** for import and export */
    protected List<Object[]> symbols;

    /** for text chars */
    protected List<int[]> chars;

    /** for font-info */
    protected List<Integer> codes;

    /** for lookup tables */
    protected List<String> lookupValues = new ArrayList<String>();

    /** for sound: */
    protected List<Object[]> soundInfos;

    public SWFSaxWriter(SWFTagTypes tags) {
        setTagTypes(tags);
        initElements();
    }

    public void setTagTypes(SWFTagTypes tags) {
        this.tags = tags;
        this.movieTags = tags;
    }

    /**
     * Resolve a symbolic id to a numeric id
     */
    protected int getId(String id) throws SAXException {
        if (id == null) {
            throw new SAXException("Missing id.");
        }

        if (idAllocate) {
            Integer idI = ids.get(id);

            if (idI != null) {
                return idI.intValue();
            }

            throw new SAXException("Id not found: " + id);
        }

        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            throw new SAXException("Invalid id: " + id);
        }
    }

    /**
     * Get an existing symbolic id - or allocate one if it does not exist
     */
    protected int getOrAllocateId(String id) throws SAXException {
        if (id == null) {
            throw new SAXException("Missing id.");
        }

        if (idAllocate) {
            Integer idI = ids.get(id);

            if (idI != null) {
                return idI.intValue();
            }

            ids.put(id, new Integer(newId));
            return newId++;
        }

        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            throw new SAXException("Invalid id: " + id);
        }
    }

    /**
     * Define a symbolic id and allocate the numeric id
     */
    protected int newId(String id) throws SAXException {
        if (id == null) {
            throw new SAXException("Missing id.");
        }

        if (idAllocate) {
            ids.put(id, new Integer(newId));
            return newId++;
        }

        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            throw new SAXException("Invalid id: " + id);
        }
    }

    /** */
    protected void initElements() {
        elementTypes.put("movie", new Movie());
        elementTypes.put("sprite", new Sprite());
        elementTypes.put("frame", new Frame());
        elementTypes.put("tag", new Tag());
        elementTypes.put("background-color", new BackgroundColor());
        elementTypes.put("protect", new Protect());
        elementTypes.put("enable-debug", new EnableDebug());
        elementTypes.put("actions", new Actions());
        elementTypes.put("shape", new Shape());
        elementTypes.put("release", new Release());
        elementTypes.put("remove", new Remove());
        elementTypes.put("place", new Place());
        elementTypes.put("jpeg", new Jpeg());
        elementTypes.put("bitmap", new Bitmap());
        elementTypes.put("import", new Import());
        elementTypes.put("export", new Export());
        elementTypes.put("quicktime-movie", new QTMovie());
        elementTypes.put("text-font", new TextFont());
        elementTypes.put("text", new Text());
        elementTypes.put("font-info", new FontInfo());
        elementTypes.put("edit-font", new EditFont());
        elementTypes.put("edit-field", new EditField());
        elementTypes.put("morph", new Morph());
        elementTypes.put("button", new Button());
        elementTypes.put("jpeg-header", new JPEGHeader());

        // Sound:
        elementTypes.put("sound", new Sound());
        elementTypes.put("button-sound", new ButtonSound());
        elementTypes.put("sound-info", new Sound_Info());
        elementTypes.put("start-sound", new StartSound());
        elementTypes.put("sound-stream-header", new SoundHeader());
        elementTypes.put("sound-stream-block", new SoundBlock());

        // For JPEG:
        elementTypes.put("image", new JPEGImage());
        elementTypes.put("alpha", new JPEGAlpha());

        // For Button
        elementTypes.put("layer", new Layer());

        // For Generator
        elementTypes.put("serial-number", new SerialNumber());
        elementTypes.put("generator", new Generator());
        elementTypes.put("generator-text", new GeneratorText());
        elementTypes.put("generator-command", new GeneratorCommand());
        elementTypes.put("generator-font", new GeneratorFont());
        elementTypes.put("generator-name-character",
                         new GeneratorNameCharacter());

        // For Text
        elementTypes.put("set-font", new SetFont());
        elementTypes.put("set-x", new SetX());
        elementTypes.put("set-y", new SetY());
        elementTypes.put("char", new Char());

        // For fonts
        elementTypes.put("glyph", new Glyph());
        elementTypes.put("code", new Code());
        elementTypes.put("anticlockwise", new Anticlockwise());

        // For import and export
        elementTypes.put("symbol", new Symbol());

        // Bitmap elements
        elementTypes.put("colors", new Colors());
        elementTypes.put("pixels", new Pixels());

        // Structures
        elementTypes.put("color", new ColorElem());
        elementTypes.put("matrix", new MatrixElem());
        elementTypes.put("color-transform", new CXFormElem());

        // Shape elements:
        elementTypes.put("line", new ShapeLine());
        elementTypes.put("curve", new ShapeCurve());
        elementTypes.put("move", new ShapeMove());
        elementTypes.put("set-primary-fill", new ShapeFill0());
        elementTypes.put("set-secondary-fill", new ShapeFill1());
        elementTypes.put("set-line-style", new ShapeSetLine());
        elementTypes.put("color-fill", new ShapeColorFill());
        elementTypes.put("gradient-fill", new ShapeGradient());
        elementTypes.put("image-fill", new ShapeImage());
        elementTypes.put("line-style", new ShapeLineStyle());
        elementTypes.put("step", new Step());

        // Actions
        elementTypes.put("unknown", new ActionUnknown());
        elementTypes.put("jump-label", new ActionJumpLabel());
        elementTypes.put("comment", new ActionComment());
        elementTypes.put("goto-frame", new ActionGotoFrame());
        elementTypes.put("get-url", new ActionGetUrl());
        elementTypes.put("next-frame", new ActionNextFrame());
        elementTypes.put("prev-frame", new ActionPrevFrame());
        elementTypes.put("play", new ActionPlay());
        elementTypes.put("stop", new ActionStop());
        elementTypes.put("toggle-quality", new ActionToggleQuality());
        elementTypes.put("stop-sounds", new ActionStopSounds());
        elementTypes.put("wait-for-frame", new ActionWaitForFrame());
        elementTypes.put("set-target", new ActionSetTarget());
        elementTypes.put("push", new ActionPush());
        elementTypes.put("pop", new ActionPop());
        elementTypes.put("add", new ActionAdd());
        elementTypes.put("subtract", new ActionSubtract());
        elementTypes.put("multiply", new ActionMultiply());
        elementTypes.put("divide", new ActionDivide());
        elementTypes.put("equals", new ActionEquals());
        elementTypes.put("less-than", new ActionLessThan());
        elementTypes.put("and", new ActionAnd());
        elementTypes.put("or", new ActionOr());
        elementTypes.put("not", new ActionNot());
        elementTypes.put("string-equals", new ActionStringEquals());
        elementTypes.put("string-length", new ActionStringLength());
        elementTypes.put("concat", new ActionConcat());
        elementTypes.put("substring", new ActionSubstring());
        elementTypes.put("string-less-than", new ActionStringLessThan());
        elementTypes.put("mutlibyte-string-length",
                         new ActionMutlibyteStringLength());
        elementTypes.put("multibyte-substring", new ActionMultibyteSubstring());
        elementTypes.put("to-integer", new ActionToInteger());
        elementTypes.put("char-to-ascii", new ActionCharToAscii());
        elementTypes.put("ascii-to-char", new ActionAsciiToChar());
        elementTypes.put("mutlibyte-char-to-ascii",
                         new ActionMutlibyteCharToAscii());
        elementTypes.put("ascii-to-multibyte-char",
                         new ActionAsciiToMultibyteChar());
        elementTypes.put("jump", new ActionJump());
        elementTypes.put("if", new ActionIf());
        elementTypes.put("call", new ActionCall());
        elementTypes.put("get-variable", new ActionGetVariable());
        elementTypes.put("set-variable", new ActionSetVariable());
        elementTypes.put("set-target", new ActionSetTarget());
        elementTypes.put("get-property", new ActionGetProperty());
        elementTypes.put("set-property", new ActionSetProperty());
        elementTypes.put("clone-sprite", new ActionCloneSprite());
        elementTypes.put("remove-sprite", new ActionRemoveSprite());
        elementTypes.put("start-drag", new ActionStartDrag());
        elementTypes.put("end-drag", new ActionEndDrag());
        elementTypes.put("wait-for-frame", new ActionWaitForFrame());
        elementTypes.put("trace", new ActionTrace());
        elementTypes.put("get-time", new ActionGetTime());
        elementTypes.put("random-number", new ActionRandomNumber());
        elementTypes.put("call-function", new ActionCallFunction());
        elementTypes.put("call-method", new ActionCallMethod());
        elementTypes.put("lookup-table", new ActionLookupTable());
        elementTypes.put("function", new ActionFunction());
        elementTypes.put("define-local-value", new ActionDefineLocalValue());
        elementTypes.put("define-local", new ActionDefineLocal());
        elementTypes.put("delete-property", new ActionDeleteProperty());
        elementTypes.put("delete-thread-vars", new ActionDeleteThreadVars());
        elementTypes.put("enumerate", new ActionEnumerate());
        elementTypes.put("typed-equals", new ActionTypedEquals());
        elementTypes.put("get-member", new ActionGetMember());
        elementTypes.put("init-array", new ActionInitArray());
        elementTypes.put("init-object", new ActionInitObject());
        elementTypes.put("new-method", new ActionNewMethod());
        elementTypes.put("new-object", new ActionNewObject());
        elementTypes.put("set-member", new ActionSetMember());
        elementTypes.put("get-target-path", new ActionGetTargetPath());
        elementTypes.put("with", new ActionWith());
        elementTypes.put("to-number", new ActionToNumber());
        elementTypes.put("to-string", new ActionToString());
        elementTypes.put("type-of", new ActionTypeOf());
        elementTypes.put("typed-add", new ActionTypedAdd());
        elementTypes.put("typed-less-than", new ActionTypedLessThan());
        elementTypes.put("modulo", new ActionModulo());
        elementTypes.put("bit-and", new ActionBitAnd());
        elementTypes.put("bit-or", new ActionBitOr());
        elementTypes.put("bit-xor", new ActionBitXor());
        elementTypes.put("shift-left", new ActionShiftLeft());
        elementTypes.put("shift-right", new ActionShiftRight());
        elementTypes.put("shift-right-unsigned", new ActionShiftRightUnsigned());
        elementTypes.put("decrement", new ActionDecrement());
        elementTypes.put("increment", new ActionIncrement());
        elementTypes.put("duplicate", new ActionDuplicate());
        elementTypes.put("return", new ActionReturn());
        elementTypes.put("swap", new ActionSwap());
        elementTypes.put("store", new ActionStore());

        // For lookup tables
        elementTypes.put("value", new LookupValue());
    }

    protected class Sound extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            String form = getAttr(attrs, "format", "mp3");
            int format = SWFConstants.SOUND_FORMAT_MP3;
            if (form.equals("raw")) {
                format = SWFConstants.SOUND_FORMAT_RAW;
            }
            if (form.equals("adpcm")) {
                format = SWFConstants.SOUND_FORMAT_ADPCM;
            }

            String rate = getAttr(attrs, "rate", "11");
            int freq = SWFConstants.SOUND_FREQ_11KHZ;
            if (rate.equals("5.5")) {
                freq = SWFConstants.SOUND_FREQ_5_5KHZ;
            }
            if (rate.equals("22")) {
                freq = SWFConstants.SOUND_FREQ_22KHZ;
            }
            if (rate.equals("44")) {
                freq = SWFConstants.SOUND_FREQ_44KHZ;
            }

            String bits = getAttr(attrs, "bits", "16");
            boolean bits16 = bits.equals("16");

            boolean stereo = getAttrBool(attrs, "stereo", false);
            int sampleCount = getAttrInt(attrs, "sample-count", 0);

            tags.tagDefineSound(id, format, freq, bits16, stereo, sampleCount,
                                Base64.decode(buff.toString()));
        }
    }

    protected class Sound_Info extends SaxHandlerBase.GatheringElementType {
        protected List<SoundInfo.EnvelopePoint> envPoints = new ArrayList<SoundInfo.EnvelopePoint>();

        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            envPoints.clear();
            startGatherMode(this);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            if (localName.equals("envelope-point")) {
                int position = getAttrInt(atts, "position", 0);
                int level0 = getAttrInt(atts, "level-0", 0);
                int level1 = getAttrInt(atts, "level-1", 0);

                envPoints.add(new SoundInfo.EnvelopePoint(position, level0,
                                                          level1));
            }

            return false; //do not gather
        }

        public void endElement() throws Exception {
            boolean noMultiplePlay = getAttrBool(attrs, "single-instance", false);
            boolean stopSound = getAttrBool(attrs, "stop-playing", false);
            int inPoint = getAttrInt(attrs, "fade-in", -1);
            int outPoint = getAttrInt(attrs, "fade-out", -1);
            int loopCount = getAttrInt(attrs, "loop-count", 1);

            int id = getId(getAttr(attrs, "sound-id", ""));
            String event = attrs.getValue("", "event");

            SoundInfo.EnvelopePoint[] envelope = new SoundInfo.EnvelopePoint[envPoints.size()];

            envPoints.toArray(envelope);
            envPoints.clear();

            soundInfos.add(new Object[] {
                               new Integer(id), event,
                               new SoundInfo(noMultiplePlay, stopSound,
                                             envelope, inPoint, outPoint,
                                             loopCount)
                           });
        }
    }

    protected class ButtonSound extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            soundInfos = new ArrayList<Object[]>();
        }

        public void endElement() throws Exception {
            int id = getId(attrs.getValue("", "id"));

            int rolloverId = 0;
            SoundInfo rolloverInfo = null;

            int rolloutId = 0;
            SoundInfo rolloutInfo = null;

            int pressId = 0;
            SoundInfo pressInfo = null;

            int releaseId = 0;
            SoundInfo releaseInfo = null;

            for (Iterator<Object[]> it = soundInfos.iterator(); it.hasNext();) {
                Object[] oo = it.next();

                int soundId = ((Integer) oo[0]).intValue();
                String event = (String) oo[1];
                SoundInfo si = (SoundInfo) oo[2];

                if ("roll-over".equals(event)) {
                    rolloverId = soundId;
                    rolloverInfo = si;
                } else if ("roll-out".equals(event)) {
                    rolloutId = soundId;
                    rolloutInfo = si;
                } else if ("press".equals(event)) {
                    pressId = soundId;
                    pressInfo = si;
                } else if ("release".equals(event)) {
                    releaseId = soundId;
                    releaseInfo = si;
                }
            }

            tags.tagDefineButtonSound(id, rolloverId, rolloverInfo, rolloutId,
                                      rolloutInfo, pressId, pressInfo,
                                      releaseId, releaseInfo);

            soundInfos = null;
        }
    }

    protected class StartSound extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            soundInfos = new ArrayList<Object[]>();
        }

        public void endElement() throws Exception {
            if (soundInfos.size() < 1) {
                return;
            }

            Object[] oo = soundInfos.get(0);

            int id = ((Integer) oo[0]).intValue();
            SoundInfo si = (SoundInfo) oo[2];

            tags.tagStartSound(id, si);
            soundInfos = null;
        }
    }

    protected class SoundHeader extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String form = getAttr(atts, "stream-format", "mp3");
            int format = SWFConstants.SOUND_FORMAT_MP3;
            if (form.equals("raw")) {
                format = SWFConstants.SOUND_FORMAT_RAW;
            }
            if (form.equals("adpcm")) {
                format = SWFConstants.SOUND_FORMAT_ADPCM;
            }

            String rate = getAttr(atts, "play-rate", "11");
            int freq = SWFConstants.SOUND_FREQ_11KHZ;
            if (rate.equals("5.5")) {
                freq = SWFConstants.SOUND_FREQ_5_5KHZ;
            }
            if (rate.equals("22")) {
                freq = SWFConstants.SOUND_FREQ_22KHZ;
            }
            if (rate.equals("44")) {
                freq = SWFConstants.SOUND_FREQ_44KHZ;
            }

            int playFreq = freq;

            rate = getAttr(atts, "stream-rate", "11");
            freq = SWFConstants.SOUND_FREQ_11KHZ;
            if (rate.equals("5.5")) {
                freq = SWFConstants.SOUND_FREQ_5_5KHZ;
            }
            if (rate.equals("22")) {
                freq = SWFConstants.SOUND_FREQ_22KHZ;
            }
            if (rate.equals("44")) {
                freq = SWFConstants.SOUND_FREQ_44KHZ;
            }

            int streamFreq = freq;

            boolean play16bit = getAttr(atts, "play-bits", "16").equals("16");
            boolean stream16bit = getAttr(atts, "stream-bits", "16").equals("16");

            boolean playStereo = getAttrBool(atts, "play-stereo", false);
            boolean streamStereo = getAttrBool(atts, "stream-stereo", false);

            int averageSampleCount = getAttrInt(atts, "average-sample-count", 0);

            tags.tagSoundStreamHead2(playFreq, play16bit, playStereo, format,
                                     streamFreq, stream16bit, streamStereo,
                                     averageSampleCount);
        }
    }

    protected class SoundBlock extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagSoundStreamBlock(Base64.decode(buff.toString()));
        }
    }

    protected class Movie extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int width = getAttrInt(atts, "width", 550);
            int height = getAttrInt(atts, "height", 400);
            int rate = getAttrInt(atts, "rate", 12);
            int version = getAttrInt(atts, "version", 5);
            idAllocate = getAttrBool(atts, "allocate-ids", false);

            flashVersion = version;

            tags.header(version, -1, width * SWFConstants.TWIPS,
                        height * SWFConstants.TWIPS, rate, -1);
        }

        public void endElement() throws Exception {
            tags.tagEnd();
        }
    }

    protected class Sprite extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int id = newId(atts.getValue("", "id"));

            tags = tags.tagDefineSprite(id);
        }

        public void endElement() throws Exception {
            tags.tagEnd(); //end the sprite timeline
            tags = movieTags; //restore main timeline           
        }
    }

    protected class Frame extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String label = atts.getValue("", "label");
            if ((label != null) && (label.length() > 0)) {
                tags.tagFrameLabel(label);
            }

            int count = getAttrInt(atts, "count", 1);

            while (count > 0) {
                tags.tagShowFrame();
                count--;
            }
        }
    }

    protected class SerialNumber extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagSerialNumber(buff.toString());
        }
    }

    protected class Generator extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagGenerator(Base64.decode(buff.toString()));
        }
    }

    protected class GeneratorText extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagGeneratorText(Base64.decode(buff.toString()));
        }
    }

    protected class GeneratorCommand extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagGeneratorCommand(Base64.decode(buff.toString()));
        }
    }

    protected class GeneratorFont extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagGeneratorFont(Base64.decode(buff.toString()));
        }
    }

    protected class GeneratorNameCharacter
        extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            tags.tagNameCharacter(Base64.decode(buff.toString()));
        }
    }

    protected class Tag extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            int type = getAttrInt(attrs, "type", 0);

            String base64 = buff.toString().trim();

            byte[] bytes = (base64.length() > 0) ? Base64.decode(base64) : null;

            tags.tag(type, false, bytes);
        }
    }

    protected class Protect extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String password = atts.getValue("", "password");

            byte[] bytes = null;
            if ((password != null) && (password.length() > 0)) {
                bytes = Base64.decode(password);
            }

            tags.tagProtect(bytes);
        }
    }

    protected class EnableDebug extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String password = atts.getValue("", "password");

            byte[] bytes = null;
            if ((password != null) && (password.length() > 0)) {
                bytes = Base64.decode(password);
            }

            tags.tagEnableDebug(bytes);
        }
    }

    protected class BackgroundColor extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int red = getAttrInt(atts, "red", 255);
            int green = getAttrInt(atts, "green", 255);
            int blue = getAttrInt(atts, "blue", 255);

            tags.tagSetBackgroundColor(new Color(red, green, blue));
        }
    }

    protected class Shape extends SaxHandlerBase.ElementType {
        protected boolean insideMorph;

        public void startElement(Attributes atts) throws Exception {
            if (shape == null) {
                insideMorph = false;

                hasAlpha = getAttrBool(atts, "has-alpha", false);

                int id = newId(atts.getValue("", "id"));

                double minx = getAttrDouble(atts, "min-x", 0.0);
                double miny = getAttrDouble(atts, "min-y", 0.0);
                double maxx = getAttrDouble(atts, "max-x", 0.0);
                double maxy = getAttrDouble(atts, "max-y", 0.0);

                Rect rect = new Rect((int) (minx * SWFConstants.TWIPS),
                                     (int) (miny * SWFConstants.TWIPS),
                                     (int) (maxx * SWFConstants.TWIPS),
                                     (int) (maxy * SWFConstants.TWIPS));

                shape = hasAlpha ? tags.tagDefineShape3(id, rect)
                                 : tags.tagDefineShape2(id, rect);

                vectors = shape;
            } else {
                insideMorph = true;
            }
        }

        public void endElement() throws Exception {
            vectors.done();

            if (!insideMorph) {
                vectors = null;
                shape = null;
            }
        }
    }

    protected class ShapeLine extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            double dx = getAttrDouble(atts, "dx", 0.0);
            double dy = getAttrDouble(atts, "dy", 0.0);

            vectors.line((int) (dx * SWFConstants.TWIPS),
                         (int) (dy * SWFConstants.TWIPS));
        }
    }

    protected class ShapeCurve extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            double cx = getAttrDouble(atts, "cx", 0.0);
            double cy = getAttrDouble(atts, "cy", 0.0);
            double dx = getAttrDouble(atts, "dx", 0.0);
            double dy = getAttrDouble(atts, "dy", 0.0);

            vectors.curve((int) (cx * SWFConstants.TWIPS),
                          (int) (cy * SWFConstants.TWIPS),
                          (int) (dx * SWFConstants.TWIPS),
                          (int) (dy * SWFConstants.TWIPS));
        }
    }

    protected class ShapeMove extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            double x = getAttrDouble(atts, "x", 0.0);
            double y = getAttrDouble(atts, "y", 0.0);

            vectors.move((int) (x * SWFConstants.TWIPS),
                         (int) (y * SWFConstants.TWIPS));
        }
    }

    protected class ShapeFill0 extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int index = getAttrInt(atts, "index", 1);
            shape.setFillStyle0(index);
        }
    }

    protected class ShapeFill1 extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int index = getAttrInt(atts, "index", 1);
            shape.setFillStyle1(index);
        }
    }

    protected class ShapeSetLine extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int index = getAttrInt(atts, "index", 1);
            shape.setLineStyle(index);
        }
    }

    protected class ShapeColorFill extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            color = null;
        }

        public void endElement() throws Exception {
            if (color == null) {
                color = new Color(255, 255, 255);
            }
            shape.defineFillStyle(color);
            color = null;
        }
    }

    protected class ShapeGradient extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);

            colors = new ArrayList<Color>();
            ratios = new ArrayList<Integer>();

            matrix = null;
        }

        public void endElement() throws Exception {
            boolean radial = getAttrBool(attrs, "radial", false);

            if (matrix == null) {
                matrix = new Matrix();
            }

            Color[] colorsA = new Color[colors.size()];
            int[] ratiosA = new int[colors.size()];

            for (int i = 0; i < colorsA.length; i++) {
                colorsA[i] = colors.get(i);
                ratiosA[i] = ratios.get(i).intValue();
            }

            shape.defineFillStyle(matrix, ratiosA, colorsA, radial);
            matrix = null;
        }
    }

    protected class ShapeImage extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            matrix = null;
        }

        public void endElement() throws Exception {
            if (matrix == null) {
                matrix = new Matrix();
            }

            int bitmapId = getId(attrs.getValue("", "image-id"));
            boolean clipped = getAttrBool(attrs, "clipped", false);

            shape.defineFillStyle(bitmapId, matrix, clipped);
            matrix = null;
        }
    }

    protected class ShapeLineStyle extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            color = null;
        }

        public void endElement() throws Exception {
            double width = getAttrDouble(attrs, "width", 1.0);

            if (color == null) {
                color = new Color(0, 0, 0);
            }
            shape.defineLineStyle((int) (width * SWFConstants.TWIPS), color);
            color = null;
        }
    }

    protected class Step extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            color = null;
        }

        public void endElement() throws Exception {
            if (color == null) {
                color = new Color(0, 0, 0);
            }

            int ratio = getAttrInt(attrs, "ratio", 0);

            ratios.add(new Integer(ratio));
            colors.add(color);
        }
    }

    protected class Release extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int id = getId(atts.getValue("", "id"));

            tags.tagFreeCharacter(id);
        }
    }

    protected class Remove extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int depth = getAttrInt(atts, "depth", 0);

            tags.tagRemoveObject2(depth);
        }
    }

    protected class Place extends SaxHandlerBase.GatheringElementType {
        protected int actionFlags; //cummulative action conditions

        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            matrix = null;
            cxform = null;
            actionFlags = 0;

            startGatherMode(this);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            if (localName.equals("actions")) {
                String flags = getAttr(atts, "conditions", "");

                StringTokenizer tok = new StringTokenizer(flags);
                while (tok.hasMoreTokens()) {
                    String f = tok.nextToken();

                    if (f.equals("load")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_ON_LOAD;
                    } else if (f.equals("enter-frame")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_ENTER_FRAME;
                    } else if (f.equals("unload")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_UNLOAD;
                    } else if (f.equals("mouse-move")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_MOVE;
                    } else if (f.equals("mouse-down")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_DOWN;
                    } else if (f.equals("mouse-up")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_UP;
                    } else if (f.equals("key-down")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_KEY_DOWN;
                    } else if (f.equals("key-up")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_KEY_UP;
                    } else if (f.equals("data")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_DATA;
                    }
                }
            }

            return true;
        }

        public void endElement() throws Exception {
            int id = getId(getAttr(attrs, "id", "-1"));
            boolean alter = getAttrBool(attrs, "alter", false);
            int clip = getAttrInt(attrs, "clip-depth", -1);
            int ratio = getAttrInt(attrs, "ratio", -1);
            int depth = getAttrInt(attrs, "depth", -1);
            String name = attrs.getValue("", "name");

            dispatchGatheredElement("matrix");
            dispatchGatheredElement("color-transform");

            actions = tags.tagPlaceObject2(alter, clip, depth, id, matrix,
                                           cxform, ratio, name, actionFlags);

            if ((actionFlags != 0) && (actions != null)) {
                actionMode = 2;
                endGatherMode(); //replay all the actions
                actions.done();
                actionMode = 0;
            }

            matrix = null;
            cxform = null;
            actions = null;
        }
    }

    protected class Jpeg extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            pixelData = null;
            jpegAlpha = null;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));
            String filename = attrs.getValue("", "file");
            boolean commonHeader = getAttrBool(attrs, "common-header", false);

            if (filename != null) {
                tags.tagDefineBitsJPEG2(id, new FileInputStream(filename));

                return;
            }

            if (jpegAlpha != null) {
                tags.tagDefineBitsJPEG3(id, pixelData, jpegAlpha);
            } else if (commonHeader) {
                tags.tagDefineBits(id, pixelData);
            } else {
                tags.tagDefineBitsJPEG2(id, pixelData);
            }

            pixelData = null;
            jpegAlpha = null;
        }
    }

    protected class JPEGImage extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            pixelData = Base64.decode(buff.toString());
        }
    }

    protected class JPEGAlpha extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            jpegAlpha = Base64.decode(buff.toString());
        }
    }

    protected class JPEGHeader extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            byte[] data = Base64.decode(buff.toString());
            tags.tagJPEGTables(data);
        }
    }

    protected class Bitmap extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            bitmapColors = null;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            boolean hasAlpha = getAttrBool(attrs, "has-alpha", false);
            int width = getAttrInt(attrs, "width", 0);
            int height = getAttrInt(attrs, "height", 0);
            int bits = getAttrInt(attrs, "bits", 32);

            int format = SWFConstants.BITMAP_FORMAT_32_BIT;
            if (bits == 8) {
                format = SWFConstants.BITMAP_FORMAT_8_BIT;
            }
            if (bits == 16) {
                format = SWFConstants.BITMAP_FORMAT_16_BIT;
            }

            Color[] colors = null;
            if ((bitmapColors != null) && !bitmapColors.isEmpty()) {
                colors = new Color[bitmapColors.size()];

                for (int i = 0; i < colors.length; i++) {
                    colors[i] = bitmapColors.get(i);
                }
            } else if (format != SWFConstants.BITMAP_FORMAT_32_BIT) {
                colors = new Color[0];
            }

            if (hasAlpha) {
                tags.tagDefineBitsLossless2(id, format, width, height, colors,
                                            pixelData);
            } else {
                tags.tagDefineBitsLossless(id, format, width, height, colors,
                                           pixelData);
            }

            bitmapColors = null;
            pixelData = null;
        }
    }

    protected class Colors extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            bitmapColors = new ArrayList<Color>();
        }

        public void endElement() throws Exception {
        }
    }

    protected class Pixels extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
        }

        public void endElement() throws Exception {
            pixelData = Base64.decode(buff.toString().trim());
        }
    }

    protected class Import extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            symbols = new ArrayList<Object[]>();
        }

        public void endElement() throws Exception {
            String movie = attrs.getValue("", "movie");
            if (movie == null) {
                throw new SAXException("Missing movie name in import.");
            }

            String[] names = new String[symbols.size()];
            int[] ids = new int[names.length];

            for (int i = 0; i < names.length; i++) {
                Object[] sym = symbols.get(i);
                names[i] = (String) sym[0];
                ids[i] = ((Integer) sym[1]).intValue();
            }

            tags.tagImport(movie, names, ids);
            symbols = null;
        }
    }

    protected class Export extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            symbols = new ArrayList<Object[]>();
        }

        public void endElement() throws Exception {
            String[] names = new String[symbols.size()];
            int[] ids = new int[names.length];

            for (int i = 0; i < names.length; i++) {
                Object[] sym = symbols.get(i);
                names[i] = (String) sym[0];
                ids[i] = ((Integer) sym[1]).intValue();
            }

            tags.tagExport(names, ids);
            symbols = null;
        }
    }

    protected class Symbol extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int id = getOrAllocateId(atts.getValue("", "id"));
            String name = atts.getValue("", "name");

            if (name == null) {
                throw new SAXException("Missing name in import/export symbol");
            }
            if (id < 1) {
                throw new SAXException("Invalid id in import/export symbol");
            }

            if (symbols != null) {
                symbols.add(new Object[] { name, new Integer(id) });
            }
        }
    }

    protected class TextFont extends SaxHandlerBase.GatheringElementType {
        protected int glyphCount;

        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            glyphCount = 0;
            startGatherMode(this);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            if (localName.equals("glyph")) {
                glyphCount++;
            }
            return true;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            vectors = tags.tagDefineFont(id, glyphCount);
            endGatherMode(); //dispatch glyphs
            vectors = null;
        }
    }

    protected class EditField extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            color = null;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            String name = getAttr(attrs, "name", "") + 0x00;
            String text = attrs.getValue("", "text");

            double minx = getAttrDouble(attrs, "min-x", 0.0);
            double miny = getAttrDouble(attrs, "min-y", 0.0);
            double maxx = getAttrDouble(attrs, "max-x", 0.0);
            double maxy = getAttrDouble(attrs, "max-y", 0.0);

            Rect rect = new Rect((int) (minx * SWFConstants.TWIPS),
                                 (int) (miny * SWFConstants.TWIPS),
                                 (int) (maxx * SWFConstants.TWIPS),
                                 (int) (maxy * SWFConstants.TWIPS));

            String align = getAttr(attrs, "align", "left").trim();
            int alignment = SWFConstants.TEXTFIELD_ALIGN_LEFT;
            if (align.equals("right")) {
                alignment = SWFConstants.TEXTFIELD_ALIGN_RIGHT;
            }
            if (align.equals("center")) {
                alignment = SWFConstants.TEXTFIELD_ALIGN_CENTER;
            }
            if (align.equals("justify")) {
                alignment = SWFConstants.TEXTFIELD_ALIGN_JUSTIFY;
            }

            int fontId = getId(attrs.getValue("", "font"));
            int fontSize = (int) (getAttrDouble(attrs, "size", 12.0) * SWFConstants.TWIPS);
            int limit = getAttrInt(attrs, "limit", 0);
            int leftMargin = (int) (getAttrDouble(attrs, "left-margin", 12.0) * SWFConstants.TWIPS);
            int rightMargin = (int) (getAttrDouble(attrs, "right-margin", 12.0) * SWFConstants.TWIPS);
            int indentation = (int) (getAttrDouble(attrs, "indentation", 12.0) * SWFConstants.TWIPS);
            int lineSpacing = (int) (getAttrDouble(attrs, "linespacing", 12.0) * SWFConstants.TWIPS);

            int flags = (text != null) ? SWFConstants.TEXTFIELD_HAS_TEXT : 0;
            if (!getAttrBool(attrs, "selectable", true)) {
                flags |= SWFConstants.TEXTFIELD_NO_SELECTION;
            }
            if (getAttrBool(attrs, "border", true)) {
                flags |= SWFConstants.TEXTFIELD_DRAW_BORDER;
            }
            if (getAttrBool(attrs, "html", false)) {
                flags |= SWFConstants.TEXTFIELD_HTML;
            }
            if (!getAttrBool(attrs, "system-font", false)) {
                flags |= SWFConstants.TEXTFIELD_FONT_GLYPHS;
            }
            if (getAttrBool(attrs, "wordwrap", false)) {
                flags |= SWFConstants.TEXTFIELD_WORD_WRAP;
            }
            if (getAttrBool(attrs, "multiline", false)) {
                flags |= SWFConstants.TEXTFIELD_IS_MULTILINE;
            }
            if (getAttrBool(attrs, "password", false)) {
                flags |= SWFConstants.TEXTFIELD_IS_PASSWORD;
            }
            if (getAttrBool(attrs, "read-only", false)) {
                flags |= SWFConstants.TEXTFIELD_DISABLE_EDIT;
            }

            if (color == null) {
                color = new AlphaColor(0, 0, 0, 0);
            } else if (!(color instanceof AlphaColor)) {
                color = new AlphaColor(color, 255);
            }

            tags.tagDefineTextField(id, name, text, rect, flags,
                                    (AlphaColor) color, alignment, fontId,
                                    fontSize, limit, leftMargin, rightMargin,
                                    indentation, lineSpacing);
        }
    }

    protected class Button extends SaxHandlerBase.GatheringElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);

            buttonRecords.clear();

            startGatherMode(this);
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));
            boolean menu = getAttrBool(attrs, "menu", false);

            dispatchAllMatchingGatheredElements("layer");

            actions = tags.tagDefineButton2(id, menu, buttonRecords);

            if (actions != null) {
                actionMode = 1;
                endGatherMode(); //dispatch any actions
                actions.done();
                actionMode = 0;
            }

            buttonRecords.clear();
        }
    }

    protected class Layer extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            cxform = null;
            matrix = null;
        }

        public void endElement() throws Exception {
            int id = getId(attrs.getValue("", "id"));
            int depth = getAttrInt(attrs, "depth", 1);
            String fors = getAttr(attrs, "for", "hit over up down");

            int flags = 0;
            StringTokenizer tok = new StringTokenizer(fors);
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();

                if (token.equals("hit")) {
                    flags |= ButtonRecord.BUTTON_HITTEST;
                } else if (token.equals("over")) {
                    flags |= ButtonRecord.BUTTON_OVER;
                } else if (token.equals("up")) {
                    flags |= ButtonRecord.BUTTON_UP;
                } else if (token.equals("down")) {
                    flags |= ButtonRecord.BUTTON_DOWN;
                }
            }

            ButtonRecord2 rec = new ButtonRecord2(id, depth, matrix, cxform,
                                                  flags);
            buttonRecords.add(rec);
        }
    }

    protected class Morph extends SaxHandlerBase.GatheringElementType {
        protected Rect startBounds;
        protected Rect endBounds;

        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);

            startBounds = null;
            endBounds = null;

            startGatherMode(this);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            if (localName.equals("shape")) {
                double minx = getAttrDouble(atts, "min-x", 0.0);
                double miny = getAttrDouble(atts, "min-y", 0.0);
                double maxx = getAttrDouble(atts, "max-x", 0.0);
                double maxy = getAttrDouble(atts, "max-y", 0.0);

                Rect rect = new Rect((int) (minx * SWFConstants.TWIPS),
                                     (int) (miny * SWFConstants.TWIPS),
                                     (int) (maxx * SWFConstants.TWIPS),
                                     (int) (maxy * SWFConstants.TWIPS));

                if (startBounds == null) {
                    startBounds = rect;
                } else {
                    endBounds = rect;
                }
            }

            return true;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            shape = tags.tagDefineMorphShape(id, startBounds, endBounds);
            vectors = shape;

            endGatherMode(); //dispatch shapes

            shape = null;
            vectors = null;
        }
    }

    protected class EditFont extends SaxHandlerBase.GatheringElementType {
        protected int glyphCount;
        protected List<Integer> codes = new ArrayList<Integer>();
        protected List<Integer> advances = new ArrayList<Integer>();
        protected List<Rect> bounds = new ArrayList<Rect>();
        protected List<int[]> kerns = new ArrayList<int[]>();
        protected boolean hasLayout;

        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);

            codes.clear();
            advances.clear();
            bounds.clear();
            kerns.clear();
            hasLayout = false;

            glyphCount = 0;
            startGatherMode(this);
        }

        public boolean gatherElement(String localName, Attributes atts) {
            if (localName.equals("glyph")) {
                glyphCount++;

                double minx = getAttrDouble(atts, "min-x", 0.0);
                double miny = getAttrDouble(atts, "min-y", 0.0);
                double maxx = getAttrDouble(atts, "max-x", 0.0);
                double maxy = getAttrDouble(atts, "max-y", 0.0);

                Rect rect = new Rect((int) (minx * SWFConstants.TWIPS),
                                     (int) (miny * SWFConstants.TWIPS),
                                     (int) (maxx * SWFConstants.TWIPS),
                                     (int) (maxy * SWFConstants.TWIPS));

                bounds.add(rect);

                int code = getAttrInt(atts, "code", 0);
                int advance = (int) (getAttrDouble(atts, "advance", 0.0) * SWFConstants.TWIPS);

                if ((code > 0) && (advance != 0)) {
                    hasLayout = true;
                }

                codes.add(new Integer(code));
                advances.add(new Integer(advance));
            }
            else if (localName.equals("kerning")) {
                hasLayout = true;

                int[] info = new int[] {
                                 getAttrInt(atts, "code-1", 0),
                                 getAttrInt(atts, "code-2", 0),
                                 (int) (getAttrDouble(atts, "offset", 0.0) * SWFConstants.TWIPS)
                             };

                kerns.add(info);

                return false;
            }

            return true;
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            int flags = 0;
            String f = getAttr(attrs, "flags", "");

            StringTokenizer tok = new StringTokenizer(f);
            while (tok.hasMoreTokens()) {
                String t = tok.nextToken();

                if (t.equals("unicode")) {
                    flags |= SWFConstants.FONT2_UNICODE;
                }
                if (t.equals("shiftjis")) {
                    flags |= SWFConstants.FONT2_SHIFTJIS;
                }
                if (t.equals("ansi")) {
                    flags |= SWFConstants.FONT2_ANSI;
                }
                if (t.equals("italic")) {
                    flags |= SWFConstants.FONT2_ITALIC;
                }
                if (t.equals("bold")) {
                    flags |= SWFConstants.FONT2_BOLD;
                }
            }

            String name = getAttr(attrs, "name", "unknown");
            int ascent = (int) (getAttrDouble(attrs, "ascent", 0.0) * SWFConstants.TWIPS);
            int descent = (int) (getAttrDouble(attrs, "descent", 0.0) * SWFConstants.TWIPS);
            int leading = (int) (getAttrDouble(attrs, "leading", 0.0) * SWFConstants.TWIPS);

            int[] advancesA = new int[glyphCount];
            int[] codesA = new int[glyphCount];
            Rect[] boundsA = new Rect[glyphCount];

            for (int i = 0; i < glyphCount; i++) {
                advancesA[i] = advances.get(i).intValue();
                codesA[i] = codes.get(i).intValue();
                boundsA[i] = bounds.get(i);
            }

            int[] kern1 = new int[kerns.size()];
            int[] kern2 = new int[kerns.size()];
            int[] kernA = new int[kerns.size()];

            for (int i = 0; i < kern1.length; i++) {
                int[] info = kerns.get(i);
                kern1[i] = info[0];
                kern2[i] = info[1];
                kernA[i] = info[2];
            }

            if ((ascent > 0) || (descent > 0) || (leading > 0) || hasLayout) {
                flags |= SWFConstants.FONT2_HAS_LAYOUT;
            }

            vectors = tags.tagDefineFont2(id, flags, name, glyphCount, ascent,
                                          descent, leading, codesA, advancesA,
                                          boundsA, kern1, kern2, kernA);

            endGatherMode(); //dispatch glyphs

            if (glyphCount == 0) {
                vectors.done();
            }
            vectors = null;
        }
    }

    protected class Glyph extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
        }

        public void endElement() throws Exception {
if (vectors == null) { // TODO vavi null
 Debug.println("Glyph:: vectors: " + vectors);
 return;
}
            vectors.done();
        }
    }

    protected class Anticlockwise extends SaxHandlerBase.ElementType {
        public void endElement() throws Exception {
            SWFShape shape = (vectors instanceof SWFShape) ? (SWFShape) vectors
                                                           : null;

            if (shape != null) {
                shape.setFillStyle0(1);
            }
        }
    }

    protected class FontInfo extends SaxHandlerBase.ContentElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            codes = new ArrayList<Integer>();
        }

        public void endElement() throws Exception {
            int id = getId(attrs.getValue("", "id"));

            String fs = attrs.getValue("", "flags");
            String name = attrs.getValue("", "name");

            if (name == null) {
                throw new SAXException("Missing name in font-info");
            }

            if (fs == null) {
                fs = "";
            }

            int flags = 0;

            StringTokenizer tok = new StringTokenizer(fs);
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();

                if (s.equalsIgnoreCase("unicode")) {
                    flags += SWFConstants.FONT_UNICODE;
                } else if (s.equalsIgnoreCase("shiftjis")) {
                    flags += SWFConstants.FONT_SHIFTJIS;
                } else if (s.equalsIgnoreCase("ansi")) {
                    flags += SWFConstants.FONT_ANSI;
                } else if (s.equalsIgnoreCase("italic")) {
                    flags += SWFConstants.FONT_ITALIC;
                } else if (s.equalsIgnoreCase("bold")) {
                    flags += SWFConstants.FONT_BOLD;
                }
            }

            int[] codesA = new int[codes.size()];
            for (int i = 0; i < codesA.length; i++) {
                codesA[i] = codes.get(i).intValue();
            }
            codes = null;

            tags.tagDefineFontInfo(id, name, flags, codesA);
        }
    }

    protected class Code extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            codes.add(new Integer(getAttrInt(atts, "code", 0)));
        }
    }

    protected class Text extends SaxHandlerBase.GatheringElementType {
        public void startElement(Attributes atts) throws Exception {
            super.startElement(atts);
            startGatherMode(this);
        }

        public void endElement() throws Exception {
            int id = newId(attrs.getValue("", "id"));

            boolean hasAlpha = getAttrBool(attrs, "has-alpha", false);
            double minx = getAttrDouble(attrs, "min-x", 0.0);
            double miny = getAttrDouble(attrs, "min-y", 0.0);
            double maxx = getAttrDouble(attrs, "max-x", 0.0);
            double maxy = getAttrDouble(attrs, "max-y", 0.0);

            Rect rect = new Rect((int) (minx * SWFConstants.TWIPS),
                                 (int) (miny * SWFConstants.TWIPS),
                                 (int) (maxx * SWFConstants.TWIPS),
                                 (int) (maxy * SWFConstants.TWIPS));

            // need to get the matrix from the front of the gathered elements
            dispatchGatheredElement("matrix");

            text = hasAlpha ? tags.tagDefineText2(id, rect, matrix)
                            : tags.tagDefineText(id, rect, matrix);

            chars = new ArrayList<int[]>();

            endGatherMode(); //dispatch the gathered elements

            flushChars();
            chars = null;

            text.done();
            text = null;
        }
    }

    protected void flushChars() throws Exception {
        if (chars == null || chars.isEmpty()) { // TODO vavi null
Debug.println("flushChars: chars: " + chars);
new Exception("*** DUMMY ***").printStackTrace(System.err);
            return;
        }

        int[] indices = new int[chars.size()];
        int[] advances = new int[indices.length];

        for (int i = 0; i < indices.length; i++) {
            int[] ii = chars.get(i);
            indices[i] = ii[0];
            advances[i] = ii[1];
        }

        text.text(indices, advances);
        chars.clear();
    }

    protected class SetFont extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            flushChars();

            int id = getId(atts.getValue("", "id"));
            double size = getAttrDouble(atts, "size", 0.0);

            if (id < 1) {
                throw new SAXException("Invalid id for font in text");
            }

if (text == null) { // TODO vavi null
 Debug.println("text: " + text);
 return;
}
            text.font(id, (int) (size * SWFConstants.TWIPS));
        }
    }

    protected class SetX extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            flushChars();

            double x = getAttrDouble(atts, "x", 0.0);

            text.setX((int) (x * SWFConstants.TWIPS));
        }
    }

    protected class SetY extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            flushChars();

            double y = getAttrDouble(atts, "y", 0.0);
if (text == null) { // TODO vavi null
 Debug.println("text: " + text);
 return;
}
            text.setY((int) (y * SWFConstants.TWIPS));
        }
    }

    protected class Char extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            double adv = getAttrDouble(atts, "advance", 0.0);
            int idx = getAttrInt(atts, "glyph-index", 0);

if (chars == null) { // TODO vavi null
 Debug.println("Char: chars: null");
 return;
}
            chars.add(new int[] { idx, (int) (adv * SWFConstants.TWIPS) });
        }
    }

    protected class QTMovie extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int id = newId(atts.getValue("", "id"));
            String name = atts.getValue("", "name");

            if (name == null) {
                throw new SAXException("Missing name for QuickTime movie");
            }
            if (id < 1) {
                throw new SAXException("Invalid id for QuickTime movie");
            }

            tags.tagDefineQuickTimeMovie(id, name);
        }
    }

    protected class ColorElem extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int red = getAttrInt(atts, "red", 0);
            int green = getAttrInt(atts, "green", 0);
            int blue = getAttrInt(atts, "blue", 0);
            int alpha = getAttrInt(atts, "alpha", 255);

            if (alpha != 255) {
                color = new AlphaColor(red, green, blue, alpha);
            } else {
                color = new Color(red, green, blue);
            }

            if (bitmapColors != null) {
                bitmapColors.add(color);
                color = null;
            }

            if (text != null) {
                flushChars();
                text.color(color);
                color = null;
            }
        }
    }

    protected class MatrixElem extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            double skew0 = getAttrDouble(atts, "skew0", 0.0);
            double skew1 = getAttrDouble(atts, "skew1", 0.0);
            double scalex = getAttrDouble(atts, "scale-x", 1.0);
            double scaley = getAttrDouble(atts, "scale-y", 1.0);
            double x = getAttrDouble(atts, "x", 0.0);
            double y = getAttrDouble(atts, "y", 0.0);

            matrix = new Matrix(scalex, scaley, skew0, skew1,
                                x * SWFConstants.TWIPS, y * SWFConstants.TWIPS);
        }
    }

    protected class CXFormElem extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int addRed = getAttrInt(atts, "add-red", 0);
            int addGreen = getAttrInt(atts, "add-green", 0);
            int addBlue = getAttrInt(atts, "add-blue", 0);
            int addAlpha = getAttrInt(atts, "add-alpha", 0);

            double multRed = getAttrDouble(atts, "mult-red", 1.0);
            double multGreen = getAttrDouble(atts, "mult-green", 1.0);
            double multBlue = getAttrDouble(atts, "mult-blue", 1.0);
            double multAlpha = getAttrDouble(atts, "mult-alpha", 1.0);

            cxform = new AlphaTransform(multRed, multGreen, multBlue,
                                        multAlpha, addRed, addGreen, addBlue,
                                        addAlpha);
        }
    }

    protected class Actions extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            if (actionMode == 0) {
                actions = tags.tagDoAction();
            }

            int actionFlags = 0;

            if (actionMode == 1) //button
             {
                String flags = getAttr(atts, "conditions", "release");

                StringTokenizer tok = new StringTokenizer(flags);
                while (tok.hasMoreTokens()) {
                    String f = tok.nextToken();

                    if (f.equals("menu-drag-out")) {
                        actionFlags |= SWFConstants.BUTTON2_OVERDOWN2IDLE;
                    } else if (f.equals("menu-drag-over")) {
                        actionFlags |= SWFConstants.BUTTON2_IDLE2OVERDOWN;
                    } else if (f.equals("release-outside")) {
                        actionFlags |= SWFConstants.BUTTON2_OUTDOWN2IDLE;
                    } else if (f.equals("drag-over")) {
                        actionFlags |= SWFConstants.BUTTON2_OUTDOWN2OVERDOWN;
                    } else if (f.equals("drag-out")) {
                        actionFlags |= SWFConstants.BUTTON2_OVERDOWN2OUTDOWN;
                    } else if (f.equals("release")) {
                        actionFlags |= SWFConstants.BUTTON2_OVERDOWN2OVERUP;
                    } else if (f.equals("press")) {
                        actionFlags |= SWFConstants.BUTTON2_OVERUP2OVERDOWN;
                    } else if (f.equals("roll-out")) {
                        actionFlags |= SWFConstants.BUTTON2_OVERUP2IDLE;
                    } else if (f.equals("roll-over")) {
                        actionFlags |= SWFConstants.BUTTON2_IDLE2OVERUP;
                    }
                }

                int charCode = getAttrInt(atts, "char-code", 0);

                if (charCode > 0) {
                    actionFlags |= ((charCode << 9) & 0xfe00);
                }
            } else if (actionMode == 2) //clip actions
             {
                String flags = getAttr(atts, "conditions", "");

                StringTokenizer tok = new StringTokenizer(flags);
                while (tok.hasMoreTokens()) {
                    String f = tok.nextToken();

                    if (f.equals("load")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_ON_LOAD;
                    } else if (f.equals("enter-frame")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_ENTER_FRAME;
                    } else if (f.equals("unload")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_UNLOAD;
                    } else if (f.equals("mouse-move")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_MOVE;
                    } else if (f.equals("mouse-down")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_DOWN;
                    } else if (f.equals("mouse-up")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_MOUSE_UP;
                    } else if (f.equals("key-down")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_KEY_DOWN;
                    } else if (f.equals("key-up")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_KEY_UP;
                    } else if (f.equals("data")) {
                        actionFlags |= SWFConstants.CLIP_ACTION_DATA;
                    }
                }
            }

            actions.start(actionFlags);
        }

        public void endElement() throws Exception {
            actions.end();
            if (actionMode == 0) {
                actions.done();
            }
        }
    }

    protected class ActionUnknown extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            int code = getAttrInt(attrs, "code", 0);
            byte[] data = Base64.decode(buff.toString());
            buff = null;

            actions.unknown(code, data);
        }
    }

    protected class ActionJumpLabel extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.jumpLabel(getAttr(atts, "label", ""));
        }
    }

    protected class ActionComment extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            actions.comment(buff.toString());
            buff = null;
        }
    }

    protected class ActionGotoFrame extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            String label = attrs.getValue("", "label");
            if (label != null) {
                actions.gotoFrame(label);
                return;
            }

            int number = getAttrInt(attrs, "number", -1);
            if (number >= 0) {
                actions.gotoFrame(number);
                return;
            }

            boolean play = getAttrBool(attrs, "play", true);
            actions.gotoFrame(play);
        }
    }

    protected class ActionGetUrl extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String url = atts.getValue("", "url");
            String target = atts.getValue("", "target");

            if ((url != null) || (target != null)) {
                if (url == null) {
                    url = "";
                }
                if (target == null) {
                    target = "";
                }

                actions.getURL(url, target);
                return;
            }

            String sendVars = getAttr(atts, "send-vars", "none");
            boolean targetSprite = getAttrBool(atts, "target-sprite", false);
            String loadVars = atts.getValue("", "load-vars-into");

            int send = SWFActions.GET_URL_SEND_VARS_POST;
            if (sendVars.equalsIgnoreCase("post")) {
                send = SWFActions.GET_URL_SEND_VARS_POST;
            } else if (sendVars.equalsIgnoreCase("get")) {
                send = SWFActions.GET_URL_SEND_VARS_GET;
            }

            int load = 0;

            if (loadVars != null) {
                load = SWFActions.GET_URL_MODE_LOAD_VARS_INTO_LEVEL;

                if (loadVars.equalsIgnoreCase("sprite")) {
                    load = SWFActions.GET_URL_MODE_LOAD_VARS_INTO_SPRITE;
                }
            } else {
                load = targetSprite
                       ? SWFActions.GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE
                       : SWFActions.GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL;
            }

            actions.getURL(send, load);
        }
    }

    protected class ActionNextFrame extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.nextFrame();
        }
    }

    protected class ActionPrevFrame extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.prevFrame();
        }
    }

    protected class ActionPlay extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.play();
        }
    }

    protected class ActionStop extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stop();
        }
    }

    protected class ActionToggleQuality extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.toggleQuality();
        }
    }

    protected class ActionStopSounds extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stopSounds();
        }
    }

    protected class ActionWaitForFrame extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            int number = getAttrInt(atts, "number", -1);
            String jumpLabel = getAttr(atts, "jump-label", "");

            if (number >= 0) {
                actions.waitForFrame(number, jumpLabel);
            } else {
                actions.waitForFrame(jumpLabel);
            }
        }
    }

    protected class ActionSetTarget extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String target = atts.getValue("", "target");

            if (target != null) {
                actions.setTarget(target);
            } else {
                actions.setTarget();
            }
        }
    }

    protected class ActionPush extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String value = atts.getValue("", "string");
            if (value != null) {
                actions.push(value);
                return;
            }

            value = atts.getValue("", "float");
            if (value != null) {
                actions.push(Float.parseFloat(value));
                return;
            }

            value = atts.getValue("", "double");
            if (value != null) {
                actions.push(Double.parseDouble(value));
                return;
            }

            value = atts.getValue("", "register");
            if (value != null) {
                actions.pushRegister(Integer.parseInt(value));
                return;
            }

            value = atts.getValue("", "boolean");
            if (value != null) {
                actions.push((value.equalsIgnoreCase("yes") ||
                             value.equalsIgnoreCase("true")) ? true : false);
                return;
            }

            value = atts.getValue("", "int");
            if (value != null) {
                actions.push(Integer.parseInt(value));
                return;
            }

            value = atts.getValue("", "lookup");
            if (value != null) {
                actions.lookup(Integer.parseInt(value));
                return;
            }

            actions.pushNull();
        }
    }

    protected class ActionPop extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.pop();
        }
    }

    protected class ActionAdd extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.add();
        }
    }

    protected class ActionSubtract extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.substract();
        }
    }

    protected class ActionMultiply extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.multiply();
        }
    }

    protected class ActionDivide extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.divide();
        }
    }

    protected class ActionEquals extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.equals();
        }
    }

    protected class ActionLessThan extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.lessThan();
        }
    }

    protected class ActionAnd extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.and();
        }
    }

    protected class ActionOr extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.or();
        }
    }

    protected class ActionNot extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.not();
        }
    }

    protected class ActionStringEquals extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stringEquals();
        }
    }

    protected class ActionStringLength extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stringLength();
        }
    }

    protected class ActionConcat extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.concat();
        }
    }

    protected class ActionSubstring extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.substring();
        }
    }

    protected class ActionStringLessThan extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stringLessThan();
        }
    }

    protected class ActionMutlibyteStringLength
        extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.stringLengthMB();
        }
    }

    protected class ActionMultibyteSubstring extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.substringMB();
        }
    }

    protected class ActionToInteger extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.toInteger();
        }
    }

    protected class ActionCharToAscii extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.charToAscii();
        }
    }

    protected class ActionAsciiToChar extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.asciiToChar();
        }
    }

    protected class ActionMutlibyteCharToAscii
        extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.charMBToAscii();
        }
    }

    protected class ActionAsciiToMultibyteChar
        extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.asciiToCharMB();
        }
    }

    protected class ActionCall extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.call();
        }
    }

    protected class ActionGetVariable extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.getVariable();
        }
    }

    protected class ActionSetVariable extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.setVariable();
        }
    }

    protected class ActionGetProperty extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.getProperty();
        }
    }

    protected class ActionSetProperty extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.setProperty();
        }
    }

    protected class ActionCloneSprite extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.cloneSprite();
        }
    }

    protected class ActionRemoveSprite extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.removeSprite();
        }
    }

    protected class ActionStartDrag extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.startDrag();
        }
    }

    protected class ActionEndDrag extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.endDrag();
        }
    }

    protected class ActionTrace extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.trace();
        }
    }

    protected class ActionGetTime extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.getTime();
        }
    }

    protected class ActionRandomNumber extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.randomNumber();
        }
    }

    protected class ActionCallFunction extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.callFunction();
        }
    }

    protected class ActionCallMethod extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.callMethod();
        }
    }

    protected class ActionDefineLocalValue extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.defineLocalValue();
        }
    }

    protected class ActionDefineLocal extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.defineLocal();
        }
    }

    protected class ActionDeleteProperty extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.deleteProperty();
        }
    }

    protected class ActionDeleteThreadVars extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.deleteThreadVars();
        }
    }

    protected class ActionEnumerate extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.enumerate();
        }
    }

    protected class ActionTypedEquals extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.typedEquals();
        }
    }

    protected class ActionGetMember extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.getMember();
        }
    }

    protected class ActionInitArray extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.initArray();
        }
    }

    protected class ActionInitObject extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.initObject();
        }
    }

    protected class ActionNewMethod extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.newMethod();
        }
    }

    protected class ActionNewObject extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.newObject();
        }
    }

    protected class ActionSetMember extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.setMember();
        }
    }

    protected class ActionGetTargetPath extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.getTargetPath();
        }
    }

    protected class ActionToNumber extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.convertToNumber();
        }
    }

    protected class ActionToString extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.convertToString();
        }
    }

    protected class ActionTypeOf extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.typeOf();
        }
    }

    protected class ActionTypedAdd extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.typedAdd();
        }
    }

    protected class ActionTypedLessThan extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.typedLessThan();
        }
    }

    protected class ActionModulo extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.modulo();
        }
    }

    protected class ActionBitAnd extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.bitAnd();
        }
    }

    protected class ActionBitOr extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.bitOr();
        }
    }

    protected class ActionBitXor extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.bitXor();
        }
    }

    protected class ActionShiftLeft extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.shiftLeft();
        }
    }

    protected class ActionShiftRight extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.shiftRight();
        }
    }

    protected class ActionShiftRightUnsigned extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.shiftRightUnsigned();
        }
    }

    protected class ActionDecrement extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.decrement();
        }
    }

    protected class ActionIncrement extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.increment();
        }
    }

    protected class ActionDuplicate extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.duplicate();
        }
    }

    protected class ActionReturn extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.returnValue();
        }
    }

    protected class ActionSwap extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.swap();
        }
    }

    protected class ActionLookupTable extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            lookupValues.clear();
        }

        public void endElement() throws Exception {
            String[] values = new String[lookupValues.size()];
            lookupValues.toArray(values);
            lookupValues.clear();
            actions.lookupTable(values);
        }
    }

    protected class LookupValue extends SaxHandlerBase.ContentElementType {
        public void endElement() throws Exception {
            lookupValues.add(buff.toString());
        }
    }

    protected class ActionJump extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.jump(getAttr(atts, "jump-label", ""));
        }
    }

    protected class ActionIf extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.ifJump(getAttr(atts, "jump-label", ""));
        }
    }

    protected class ActionStore extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.storeInRegister(getAttrInt(atts, "register", 0));
        }
    }

    protected class ActionWith extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            actions.startWith();
        }

        public void endElement() throws Exception {
            actions.endBlock();
        }
    }

    protected class ActionFunction extends SaxHandlerBase.ElementType {
        public void startElement(Attributes atts) throws Exception {
            String name = getAttr(atts, "name", "");
            String parms = getAttr(atts, "params", "");

            StringTokenizer tok = new StringTokenizer(parms);
            List<String> p = new ArrayList<String>();
            while (tok.hasMoreTokens()) {
                p.add(tok.nextToken());
            }

            String[] params = new String[p.size()];
            p.toArray(params);

            actions.startFunction(name, params);
        }

        public void endElement() throws Exception {
            actions.endBlock();
        }
    }
}
