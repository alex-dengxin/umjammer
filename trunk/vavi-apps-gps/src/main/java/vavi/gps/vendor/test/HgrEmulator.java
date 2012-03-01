/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import vavi.gps.GpsDevice;
import vavi.io.IODevice;
import vavi.io.IODeviceInputStream;
import vavi.io.IODeviceOutputStream;
import vavi.util.event.GenericListener;
import vavi.util.Debug;


/**
 * HGR のエミュレーションを行うクラスです。
 * <pre>
 * <!PUON		電源 ON
 * > メッセージ
 * < !PUOFF		電源 OFF
 * > 無し
 * < !PC		GPS OFF
 * > OK
 * < !GP		GPS ON
 * > OK
 * < !ID		ID 取得
 * > ID 情報
 * < !MRD6		メモリレジスタ読み込み D6 レジスタ
 * > MRDT3e900	... 0x3e900
 * < !MWW1c,21c	メモリレジスタ書き込み W1c レジスタ、値 0x21c
 * > OK		or NG
 * < !MD100,4c		[W0],[Da]
 * > MDC#......
 * > MDC#...
 * >  :
 * > MDF#...
 * </pre>
 * レジスタの説明
 * <pre>
 * W0	メモリスタートオフセット？(読み込み専用)
 *
 *		いつも 0x100
 *
 * D2	メモリスタートオフセット？(読み込み専用)
 *
 *		いつも 0x100
 *
 * D6	搭載メモリ量(読み込み専用)
 *
 *		HGR3 では 0x3e900
 *
 * Da	メモリ使用量
 *
 *		測位記録1地点あたり 19 byte メモリを使用するので、
 *		Da で読み取った値を 19 で割ると 記録されている測位データの数
 *		がわかる。
 *		メモリクリアしたい時はこのレジスタを 0 に set する。
 * 例:
 * 	!MRDa
 *	 MRDTa3e      → 0xa3e = 2622 = 138*19
 *	 !MWDa,0      ←メモリクリア
 *	 OK
 *
 * De	不明 (読み込み専用)
 *
 *		HGR3 ではいつも 0x384e6
 *		(256 Kbyte の搭載メモリからアルマナックデータなどの保存に使う
 *		 メモリの分を引いた、実際に利用できるメモリ量？
 *
 * D16	不明 (読み込み専用)
 *
 *		HGR3 ではいつも 0
 *
 * W12	単体動作時 測位データ記録間隔
 *
 *		0 の時、測位データの自動記録をしない。
 *		(マークボタンを押した時だけ記録される)
 * 例:
 *	 !MRW12
 *	 MRDT1	→ 1秒
 *	 !MWW12,3c    → 0x3c = 60(秒)に設定
 *	 OK
 *
 * W14	不明 (読み込み専用)
 *
 *		HGR3 ではいつも 0
 *
 * W1a	PC接続時 測位データ記録間隔
 *
 *		0 の時、測位データの自動記録をしない。
 *		(マークボタンを押した時だけ記録される)
 * 例:
 *	 !MRW1a
 *	 MRDT1	→ 0 (自動記録なし)
 *	 !MWW1a,1     → 1秒に設定
 *	 OK
 *
 * W1c	LCD の時刻表示 Timezone offset (HGR3 only?)
 *
 *		HGR 内部では GMT(UTC) で扱われていて、このレジスタの
 *		値分オフセット されて液晶に時刻表示される。単位は「分」
 *		通常日本ならば 60*9 = 540 = 0x21c
 *
 *	 !MRW1c
 *	 MRDT21c      → 0x21c = 540 = 60*9 (JST)
 *	 !MWW1c,0     → 0 (GMT)
 *	 OK
 *	 !MWW1c,fed4  → 0xfed4 = -300 (shortで) = -5*60
 *
 * B1e	不明
 *
 *		HGR3 ではいつも 1 が読み書きされる
 *
 * B1f	不明
 *
 *		HGR3 ではいつも 5 が読み書きされる
 *
 * B20	バックライト点灯時間 (HGR3 only?)
 *
 *		0 の時、バックライト自動 OFF が無効となる。
 *
 *	例:
 *		!MRB20
 *		MRDT0	→  0 (バックライト自動OFFが無効)
 *		!MWB20,78    →  0x78 = 120 (秒)に設定
 *		OK
 *
 * たぶん先頭のアルファベット
 *  B Byte, W Word, D DWord
 * </pre>
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030331 nsano initial version <br>
 */
public abstract class HgrEmulator extends GpsDevice {

    /** */
//  private int register_W1a = 0;

    //----

    /** */
    private boolean powerOn = false;

    /** */
    private Timer outputTimer;

    /** */
    private long interval = 1000;

    /** */
    private void doCommand(String command) throws IOException {
        if ("!PUON".equals(command)) {
            doPUON();
        } else if ("!PUOFF".equals(command)) {
            doPUOFF();
        } else if ("!GP".equals(command)) {
            doGP();
        } else if ("!PC".equals(command)) {
            doPC();
        } else if ("!ID".equals(command)) {
            doID();
        } else {
Debug.println("unrecognized command: " + command);
        }
    }

    /** */
    private void doPUON() throws IOException {
        os.writeLine("ROM    OK");
        os.writeLine("RS232C OK");
        os.writeLine("CLOCK  NG");
        os.writeLine();
        os.writeLine("        ----< SONY GLOBAL POSITIONING SYSTEM >-----");
        os.writeLine("                               (C)Copyright 1991,1997   Sony Corporation.");
        os.writeLine();

        powerOn = true;
    }

    /** */
    private void doPUOFF() throws IOException {
        if (!powerOn) {
            return;
        }

        if (outputTimer != null) {
            outputTimer.cancel();
        }
        powerOn = false;
Debug.println("power off");
    }

    /** */
    private void doPC() throws IOException {
        if (!powerOn) {
            return;
        }

        if (outputTimer != null) {
            outputTimer.cancel();
        }

        os.writeLine("OK");
    }

    /** */
    private void doGP() throws IOException {
        if (!powerOn) {
            return;
        }

        outputTimer = new Timer();
        outputTimer.schedule(getOutputTimerTask(), 100, interval);

        os.writeLine("OK");
    }

    /** */
    private void doID() throws IOException {
        os.writeLine("ID" + "DTPCQ-HGR3,1.0.00.07281");
    }

    //----

    /** */
    protected IODeviceInputStream is;

    /** */
    protected IODeviceOutputStream os;

    /** シリアル回線からの入力 */
    protected Runnable getInputThread() {
        return new Runnable() {
            public void run() {
Debug.println("input thread started");
                while (loop) {
                    try {
                        String command = is.readLine();
//Debug.println("command: " + command);
                        doCommand(command);
                    }
                    catch (IOException e) {
Debug.println(e);
                    }
                }
Debug.println("input thread stopped");
            }
        };
    }

    /** */
    protected GenericListener getOutputGenericListener() {
        throw new IllegalStateException("This class cannot be output device.");
    }

    /** シリアル回線への GPS データの出力 */
    protected abstract TimerTask getOutputTimerTask();

    /** make sure not to duplicate input thread */
    public void start() {
Debug.println("here");
    }

    /** */
    public HgrEmulator() {

        IODevice ioDevice = new SharedMemoryDevice("output", "input");

        this.is = new IODeviceInputStream(ioDevice);
        this.os = new IODeviceOutputStream(ioDevice);

        super.start();
    }
}

/* */
