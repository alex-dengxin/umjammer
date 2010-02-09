/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


/**
 * AbstractDevicePlug.
 *
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public abstract class AbstractDevicePlug {

    /**
     * 入力デバイスのポーリングを行うスレッドを取得します。
     * 以下を <code>java.lang.Runnable#run()</code> メソッドに記述すること。
     * <ol>
     * <li>入力デバイスから１ブロックを読み取る</li>
     * <li>1. のデータを <code>vavi.gps.GpsData</code> またはそのサブクラスに
     * 変換する</li>
     * <li><code>vavi.util.event.GenericEvent</code> を
     * argument は 2. のデータ、name は "data" として作成する</li>
     * <li>3. のイベントを引数にして <code>fireEventHappened()</code>
     * メソッドを発行する</li>
     * </ol>
     * @return	java.lang.Runnable の実装クラス
     */
    protected abstract Runnable getInputThread();

    /** TODO 複数接続未対応 */
    protected volatile boolean loop;

    /** デバイスを機能させます。 */
    public void start() {
        loop = true;
        Thread thread = new Thread(getInputThread());
        thread.start();
    }

    /**
     * 出力デバイスへデータを出力する、イベントリスナを取得します。
     * 以下を <code>vavi.util.event.GenericEvent#eventHappened()</code>
     * メソッドに記述すること。
     * <ol>
     * <li><code>vavi.util.event.GenericEvent</code> の getName() が "name"
     * のイベントの getArgument() から <code>vavi.gps.GpsData</code> 型の
     * データを取得する</li>
     * <li>1. のデータを出力デバイスに適切なフォーマットに変換する</li>
     * <li>出力デバイスに 2. のデータを出力する
     * </ol>
     * @return	vavi.util.event.GenericListener の実装クラス
     */
    protected abstract GenericListener getOutputGenericListener();

    /** 入力デバイスと出力デバイスを接続します。 */
    public void connect(AbstractDevicePlug outputDevice) {
        // out@outputDevice -> in@inputDevice
        GenericListener listener = outputDevice.getOutputGenericListener();
        addGenericListener(listener);
        // out@inputDevice -> in@outputDevice
        listener = getOutputGenericListener();
        outputDevice.addGenericListener(listener);
    }

    //-------------------------------------------------------------------------

    /** 汎用イベント機構の実装 */
    private GenericSupport gs = new GenericSupport();

    /** 汎用イベントリスナを追加します。 */
    public void addGenericListener(GenericListener listener) {
        gs.addGenericListener(listener);
    }

    /** 汎用イベントリスナを削除します。 */
    public void removeGenericListener(GenericListener listener) {
        gs.removeGenericListener(listener);
    }

    /** 汎用イベントを発行します。 */
    protected void fireEventHappened(GenericEvent ev) {
        gs.fireEventHappened(ev);
    }
}

/* */
