/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;


/**
 * BD-J Karaoke.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080824 nsano initial version <br>
 */
public class KaraokeApp implements Xlet {

    /** */
    private HScene scene;
    /** */
    private MyView gui;
    /** */
    XletContext context;

    /* */
    public void initXlet(XletContext context) {
        this.context = context;

        this.scene = HSceneFactory.getInstance().getDefaultHScene();

        try {
            this.gui = new MyView();

            gui.setBounds(scene.getBounds());
            scene.add(gui, BorderLayout.CENTER);
        } catch (Exception e) {
e.printStackTrace(System.err);
        }

        scene.validate();
    }

    /* */
    public void startXlet() {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();

        new Thread(gui).start();
    }

    /* */
    public void pauseXlet() {
        gui.setVisible(false);
    }

    /* */
    public void destroyXlet(boolean unconditional) {
        scene.remove(gui);
        scene = null;
    }

    /** */
    class MyView extends Container implements Runnable {

        static final int MODE_INIT = 0;
        static final int MODE_RUN = 1;

        private int mode = MODE_INIT;

        Console console;

        /** */
        MyView() throws Exception {
            addKeyListener(keyListener);
        }

        /** */
        boolean notified;

        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
System.err.println("NOTIFIED: 1");
        }

        final String[] xletPropNames = {
            "dvb.org.id",
            "dvb.app.id",
        };

        final String[] systemPropNames = {
            "dvb.persistent.root",
            "aacs.bluray.online.capability",
            "bluray.vfs.root",
            "bluray.bindingunit.root",
            "bluray.disc.root",
            "bluray.localstorage.level",
            "bluray.profile.1",
            "bluray.p1.version.major",
            "bluray.p1.version.minor",
            "bluray.p1.version.micro",
            "bluray.profile.2",
            "bluray.p2.version.major",
            "bluray.p2.version.minor",
            "bluray.p2.version.micro",
            "bluray.rccapability.release",
            "bluray.rccapability.holdandrelease",
            "bluray.rccapability.repeatonhold",
            "dvb.persistent.root",
            "file.separator",
            "mhp.profile.enhanced_broadcast",
            "mhp.eb.version.major",
            "mhp.eb.version.minor",
            "mhp.eb.version.micro",
            "mhp.profile.interactive_broadcast",
            "mhp.ib.version.major",
            "mhp.ib.version.minor",
            "mhp.ib.version.micro",
            "mhp.profile.internet_access",
            "mhp.ia.version.major",
            "mhp.ia.version.minor",
            "mhp.ia.version.micro",
            "mhp.option.ip.multicast",
            "mhp.option.dsmcc.uu",
            "mhp.option.dvb.html",
            "file.encoding",
            "os.arch",
            "os.name",
            "os.version",
            "user.language",
            "user.timezone",
        };

        /** properties listing */
        void demo1() {
            for (int i = 0; i < xletPropNames.length; i++) {
                try {
                    console.println(xletPropNames[i] + ": " + context.getXletProperty(xletPropNames[i]));
                } catch (Exception e) {
e.printStackTrace(System.err);
                    console.println(e.toString());
                }
            }
            for (int i = 0; i < systemPropNames.length; i++) {
                try {
                    console.println(systemPropNames[i] + ": " + System.getProperty(systemPropNames[i]));
                } catch (Exception e) {
e.printStackTrace(System.err);
                    console.println(e.toString());
                }
            }
        }

        private final String[] CONTENTS = {
            ContentDescriptor.CONTENT_UNKNOWN,
            FileTypeDescriptor.AIFF,
            FileTypeDescriptor.BASIC_AUDIO,
            FileTypeDescriptor.GSM,
            FileTypeDescriptor.MIDI,
            FileTypeDescriptor.MPEG,
            FileTypeDescriptor.MPEG_AUDIO,
            FileTypeDescriptor.MSVIDEO,
            FileTypeDescriptor.QUICKTIME,
            FileTypeDescriptor.RMF,
            FileTypeDescriptor.VIVO,
            FileTypeDescriptor.WAVE,
            VideoFormat.CINEPAK,
            VideoFormat.H261,
            VideoFormat.H263,
            VideoFormat.H261_RTP,
            VideoFormat.H263_RTP,
            VideoFormat.INDEO32,
            VideoFormat.INDEO41,
            VideoFormat.INDEO50,
            VideoFormat.IRGB,
            VideoFormat.JPEG,
            VideoFormat.JPEG_RTP,
            VideoFormat.MJPEGA,
            VideoFormat.MJPEGB,
            VideoFormat.MJPG,
            VideoFormat.MPEG_RTP,
            VideoFormat.RGB,
            VideoFormat.RLE,
            VideoFormat.SMC,
            VideoFormat.YUV,
            AudioFormat.ALAW,
            AudioFormat.DOLBYAC3,
            AudioFormat.DVI,
            AudioFormat.DVI_RTP,
            AudioFormat.G723,
            AudioFormat.G723_RTP,
            AudioFormat.G728,
            AudioFormat.G728_RTP,
            AudioFormat.G729,
            AudioFormat.G729_RTP,
            AudioFormat.G729A,
            AudioFormat.G729A_RTP,
            AudioFormat.GSM,
            AudioFormat.GSM_MS,
            AudioFormat.GSM_RTP,
            AudioFormat.IMA4,
            AudioFormat.IMA4_MS,
            AudioFormat.LINEAR,
            AudioFormat.MAC3,
            AudioFormat.MAC6,
            AudioFormat.MPEG,
            AudioFormat.MPEG_RTP,
            AudioFormat.MPEGLAYER3,
            AudioFormat.MSADPCM,
            AudioFormat.MSNAUDIO,
            AudioFormat.MSRT24,
            AudioFormat.TRUESPEECH,
            AudioFormat.ULAW,
            AudioFormat.ULAW_RTP,
            AudioFormat.VOXWAREAC10,
            AudioFormat.VOXWAREAC16,
            AudioFormat.VOXWAREAC20,
            AudioFormat.VOXWAREAC8,
            AudioFormat.VOXWAREMETASOUND,
            AudioFormat.VOXWAREMETAVOICE,
            AudioFormat.VOXWARERT29H,
            AudioFormat.VOXWARETQ40,
            AudioFormat.VOXWARETQ60,
            AudioFormat.VOXWAREVR12,
            AudioFormat.VOXWAREVR18
        };

        /** jmf handler listing */
        void demo2() {
            console.println("Handler:");
            for (int i = 0; i < CONTENTS.length; i++) {
                List classes = Manager.getHandlerClassList(CONTENTS[i]);
                for (int j = 0; j < classes.size() / 2; j += 2) {
                    String className = replace((String) classes.get(j + 1), "/", "_");
                    try {
                        Class.forName(className);
console.println(classes.get(j) + "    " + className); 
                    } catch (Exception e) {
System.err.println("No such a class: " + className); 
                    }
                }
            }
            console.println("DataSource:");
            for (int i = 0; i < CONTENTS.length; i++) {
                List classes = Manager.getDataSourceList(CONTENTS[i]);
                for (int j = 0; j < classes.size() / 2; j += 2) {
                    String className = replace((String) classes.get(j + 1), "/", "_");
                    try {
                        Class.forName(className);
console.println(classes.get(j) + "    " + className); 
                    } catch (Exception e) {
System.err.println("No such a class: " + className); 
                    }
                }
            }
        }

        /** */
        String itoa(int v, int c) {
            String t = "0";
            
            for (int i = 1; i < c; i++) {
                t += "0";
            }
            
            String r = t + v;
            
            return r.substring(r.length() - c);
        }

        /** */
        String replace(String string, String target, String replacement) {
            int p;
            int q = 0;
            while ((p = string.indexOf(target, q)) != -1) {
                string = string.substring(0, p) + replacement + string.substring(p + target.length());
                q = p + replacement.length();
            }
            return string;
        }

        /** class reflection */
        void demo3(Class clazz) throws Exception {
            Class sc = clazz.getSuperclass();
System.err.println("class " + clazz.getName() + (sc != null ? " extends " + sc : ""));
            Class[] is = clazz.getInterfaces();
System.err.print(is.length > 0 ? " implements " : "");
for (int i = 0; i < is.length; i++) {
 System.err.print(is[i].getName());
 System.err.print(", ");
}
System.err.println("{");
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                System.err.print(" ");
                System.err.print(methods[i]);
                System.err.println(";");
            }
System.err.println("}");
        }

        /** package listing */
        void demo4() throws Exception {
            Package[] ps = Package.getPackages();
            for (int i = 0; i < ps.length; i++) {
//                if (ps[i].getName().indexOf("media") != -1) {
                 System.err.println(i + ": " + ps[i]);
                 console.println(i + ": " + ps[i]);
//                }
            }
        }

        String orgPath;
        String appPath;

        /**
         * directory listing and deleting
         * @after orgPath
         * @after appPath
         */
        void demo5() throws Exception {
            String adaRoot = System.getProperty("dvb.persistent.root");
            String orgId = (String) context.getXletProperty("dvb.org.id");
            String appId = (String) context.getXletProperty("dvb.app.id");
            orgPath = adaRoot + File.separator + orgId;
            appPath = orgPath + File.separator + appId;

            File file = new File(appPath);
            File[] files = file.listFiles();
console.println("1: " + file);
            for (int i = 0; i < files.length; i++) {
console.println("1: " + files[i] + ", " + files[i].length());
            }

            file = new File(orgPath);
            files = file.listFiles();
console.println("2: " + file);
            for (int i = 0; i < files.length; i++) {
console.println("2: " + files[i] + ", " + files[i].length());
            }

            file = new File(appPath + File.separator + "sound1.bdmv");
console.println("3: " + file);
            if (file.exists()) {
                boolean r = file.delete();
console.println("3: remove: " + file + ", " + r);
            }

            file = new File(appPath + File.separator + "sound2.bdmv");
console.println("4: " + file);
            if (file.exists()) {
                boolean r = file.delete();
console.println("4: remove: " + file + ", " + r);
            }
        }

        String sound1;

        /**
         * ada creation
         * @before appPath
         * @after sound1
         */
        void demo6() throws Exception {
            sound1 = appPath + File.separator + "sound1.bdmv";
            File file = new File(sound1);
console.println("5: " + file);
            if (file.exists()) {
                boolean r = file.delete();
console.println("5: remove: " + file + ", " + r);
            }
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            InputStream is = new BufferedInputStream(getClass().getResourceAsStream("/sound.bdmv"));
            byte[] buf = new byte[1024];
            int l = 0;
            while (is.available() > 0) {
                int r = is.read(buf, 0, buf.length);
                try {
                    os.write(buf, 0, r);
                } catch (IOException e) {
console.println("bytes: " + l);
throw e;
                }
                l += r;
            }
            os.flush();
            os.close();
            is.close();
        }

        /** jmf test */
        void demo7() throws Exception {
            int c = 0;
            boolean err = false;
            while (c < 10) {

                if (!err) {
                    try {
//                      String url = "sound.bdmv";
//                      String url = "peyanguS.wav";
//                      String url = "bd://SOUND:00";
//                      String url = "bd://SOUND:" + itoa(c, 2);
                        URL url = new File(sound1).toURL();
System.err.println("url: " + url);
//                      MediaLocator mediaLocator = new MediaLocator(this.getClass().getResource(url)); // NG sound.bdmv
//                      MediaLocator mediaLocator = new org.davic.media.MediaLocator(new org.bluray.net.BDLocator(url)); // OK bd://SOUND:00
                        MediaLocator mediaLocator = new MediaLocator(url);
System.err.println("ml: " + mediaLocator + ", " + mediaLocator.getClass().getName());
console.println("ml: " + mediaLocator + ", " + mediaLocator.getClass().getName());
                        DataSource dataSource = Manager.createDataSource(mediaLocator);
//                      DataSource dataSource = new BdjDataSource(mediaLocator);
System.err.println("ds: " + dataSource.getContentType() + ", " + dataSource.getClass().getName());
System.err.println(" ds interface: " + dataSource.getClass().getSuperclass().getName());
console.println(" ds interface: " + dataSource.getClass().getSuperclass().getName());
for (int i = 0; i < dataSource.getClass().getInterfaces().length; i++) {
 System.err.println(" ds super: " + dataSource.getClass().getInterfaces()[i].getName());
 console.println(" ds super: " + dataSource.getClass().getInterfaces()[i].getName());
}
console.println("ds: " + dataSource.getContentType() + ", " + dataSource.getClass().getName());
                        Player player = null;
try {
// demo3(dataSource.getClass());
                        player = Manager.createPlayer(dataSource);
// player = new com.sony.bdjstack.javax.media.content.file.Player();
// player.setSource(dataSource);
} catch (Throwable e) {
 console.println(e);
}
                        if (player != null) {
console.println("player: " + player.getClass().getName());
                            player.start();
                        } else {
System.err.println("no player: " + dataSource);
console.println("no player: " + dataSource);
                        }
//err = true;
                    } catch (Throwable e) {
e.printStackTrace(System.err);
console.println(e);
err = true;
                    }
                }
c++;
            }

            try { Thread.sleep(3000); } catch (Exception e) {}
        }

        /** ada sound test */
        void demo8() throws Exception {

//            URL inUrl = getClass().getResource("/test.wav");
//            InputStream is = new BufferedInputStream(inUrl.openStream());
//            AdaSound adaSound = new AdaSound(context, new SimpleWaveInputStream(is));
            URL inUrl = getClass().getResource("/test.mp3");
            InputStream is = inUrl.openStream();
            AdaSound adaSound = new AdaSound(context, new Mp3InputStream(is));

            int c = 0;

            while (true) {

                long lastTime = System.currentTimeMillis();

                int length = adaSound.decode();
                URL url = adaSound.getURL();
                MediaLocator mediaLocator = new MediaLocator(url);
                DataSource dataSource = Manager.createDataSource(mediaLocator);
                Player player = Manager.createPlayer(dataSource);
                player.start();
console.println("count: " + c + ", " + length);
                
                if (adaSound.available() == 0) {
                    break;
                }

                long waitTime = length - (System.currentTimeMillis() - lastTime);
                Thread.sleep(waitTime > 0 ? waitTime : 0);
console.println("sleep: " + waitTime);

                c++;
            }

console.println("done: " + c);
            adaSound.close();
        }

        /* */
        public void run() {

            while (!notified) {
                Thread.yield();
            }
System.err.println("NOTIFIED: 2");

            console = new Console(this, getWidth(), getHeight());
            repaint();

            mode = MODE_RUN;

console.println(" \nBD-J Console (c) by umjammer");

try {
            demo5();
            demo8();

} catch (Throwable e) {
 console.println(e);
}
        }

        /** */
        private KeyListener keyListener = new KeyAdapter() { 
            public void keyPressed(KeyEvent keyEvent) {
                console.keyPressed(keyEvent);
            }
        };

        /* */
        public void paint(Graphics g) {
            switch (mode) {
            case MODE_RUN:
                console.paint(g);
                break;
            }
        }
    }
}

/* */
