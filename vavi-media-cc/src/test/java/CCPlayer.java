/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.io.IOException;

import vavi.media.ui.cc.ClosedCaption;
import vavi.media.ui.cc.ClosedCaptionReader;
import vavi.media.ui.cc.ClosedCaptionSpi;
import vavi.media.ui.cc.Scheduler;
import vavi.media.ui.cc.V2Scheduler;
import vavi.media.ui.cc.Viewer;
import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * 字幕システムのクラスです。
 * システムは簡単なのですが、肝は
 * <ol>
 * <li>スクリーン上に透明な背景で文字だけ浮かび上がらせること</li>
 * <li>いろんなファイル形式に対応できること</li>
 * <li>軽いこと</li>
 * </ol>
 * です。1. は Java だと難しいです。skinlf でトライしてみます。
 * 2. は Java の得意とするところです。ローダを SPI 形式にしてあらゆる
 * フォーマットに対応してみせます。手始めは時間タグ付き歌詞ファイルか？
 * 3. も Java だと難しいですね。どうしよう。最悪 C# か？ JDirect もアリか？
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00 030214 nsano initial version <br>
 *          0.10 030304 nsano 一応完成 <br>
 *          0.11 030306 nsano fix when lest timeFrom is -1 <br>
 */
public class CCPlayer {

    /** model */
    private Scheduler scheduler;

    /** view */
    private Viewer viewer;

    /** */
    public CCPlayer() {
    }

    /** */
    private void setModel(ClosedCaption[] ccs) {
        scheduler = new V2Scheduler(ccs);
        scheduler.addGenericListener(listener);
    }

    /** */
    public void setSpeed(int speed) {
        scheduler.setSpeed(speed);
    }

    /** */
    public void start() {
        scheduler.start();
    }

    /** */
    public void setView(Viewer viewer) {
        this.viewer = viewer;
    }

    /** */
    private GenericListener listener = new GenericListener() {
        public void eventHappened(GenericEvent ev) {
            String name = ev.getName();
            if ("show".equals(name)) {
                viewer.showClosedCaption((ClosedCaption) ev.getArguments()[0]);
            } else if ("exit".equals(name)) {
                ClosedCaption cc = (ClosedCaption) ev.getArguments()[0];
                long time = cc.getTimeTo() == -1 ?
                    2000L :
                    cc.getTimeTo() - cc.getTimeFrom();
                try { Thread.sleep(time); } catch (Exception e) {}
                System.exit(0);
            } else {
Debug.println("unknown command: " + name);
            }
        }
    };

    /** */
    private ClosedCaptionReader reader;

    /** */
    public void readClosedCaption(File file) throws IOException {
        reader = ClosedCaptionSpi.Factory.getReader(file);
        setModel(reader.readClosedCaptions());
    }

    //----

    /**
     *
     */
    public static void main(String[] args) throws Exception {

        CCPlayer player = new CCPlayer();

        player.readClosedCaption(new File(args[0]));
        player.setSpeed(Integer.parseInt(args[1]));

        player.setView(new SkinLFViewer());
new HandSynchronizer(player.scheduler);	// TODO

//      player.start();
    }
}

/* */
