/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.uusbd;

import java.io.IOException;


/**
 * Usb.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 030314 nsano initial version <br>
 */
public class Usb {

    /** */
    public Usb() throws IOException {
        open();
    }

    /** */
    public Usb(int flag,
               int clazz,
               int subClass,
               int vendor,
               int product,
               byte bcdDevice) throws IOException {
        open(flag, clazz, subClass, vendor, product, bcdDevice);
    }

    //-------------------------------------------------------------------------

    /** USB のハンドル */
    long instance;

    /**
     * USBデバイスをオープンする。
     * システムに接続されていて、uusbd.sys がドライバーとして使用されており、
     * 他のアプリケーションがオープンしていない USB デバイスが存在する必要が
     * ある。使用後 close() を呼び出すこと。
     * @throws UsbException
     */
    private native void open() throws UsbException;

    /** */
    public static final int MASK_NO = 0;
    /** */
    public static final int MASK_CLASS = 1;
    /** */
    public static final int MASK_SUBCLASS = 2;
    /** */
    public static final int MASK_VENDOR = 4;
    /** */
    public static final int MASK_PRODUCT = 8;
    /** */
    public static final int MASK_BCDDEVICE = 16;

    /**
     * open() に検索条件を追加したもの
     * 使用後 close() を呼び出すこと。
     *
     * <li> TODO 未テスト
     *
     * @param flag デバイス検索条件に使用する引数を指定する。
     *             複数の項目で条件をきめる場合はORをとったものを使う
     * <pre>
     *		MASK_CLASS クラスコードを検索条件にする時
     *		MASK_SUBCLASS サブクラスコードを検索条件にする時
     *		MASK_VENDOR ベンダー ID を検索条件にする時
     *		MASK_PRODUCT	プロダクト ID を検索条件にする時
     *		MASK_BCDDEVICE デバイスリリース番号を検索条件にする時
     *		MASK_NO 上記のいずれのマスクも使用しない場合
     * </pre>
     * @param clazz	デバイスのクラスコード
     * @param subClass	サブクラスコード
     * @param vendor	ベンダーID
     * @param product	プロダクトID
     * @param bcdDevice	デバイスリリース番号
     */
    private native void open(int flag,
                             int clazz,
                             int subClass,
                             int vendor,
                             int product,
                             byte bcdDevice) throws UsbException;

    /**
     * クローズします．
     */
    public native void close();

    /** */
    public static final int RECIPIENT_DEVICE = 0;
    /** */
    public static final int RECIPIENT_INTERFACE = 1;
    /** */
    public static final int RECIPIENT_ENDPOINT = 2;
    /** */
    public static final int RECIPIENT_OTHER = 3;

    /**
     * クラスリクエストを送る。
     * データー転送を伴わないリクエストの場合、
     * wLength を 0, dir_in を false とすること。
     *
     * @param dir_in	USB デバイスからデータを受ける場合 true にする
     * @param recipient	RequestTyep の D4..D0 に指定する受信先を指定する。
     *                  次のいずれかを指定。
     * <ul>
     *	<li>RECIPIENT_DEVICE	デバイス
     *	<li>RECIPIENT_INTERFACE	インターフェース
     *	<li>RECIPIENT_ENDPOINT	エンドポイント
     *	<li>RECIPIENT_OTHER	その他
     * </ul>
     * @param bRequest	リクエストの番号
     * @param wValue	bRequest の値に応じて USB デバイスとの間で決められた
     *                  16bit 値
     * @param wIndex	bRequestの値に応じてUSBデバイスとの間で決められた
     *                  16bit 値。おもにindex 値や、オフセット値をわたすのに
     *                  使う
     * @param wLength	データーを送受信する場合の転送長(byte)
     * @param data	転送データの場所又は受信データの格納場所
     */
    public native void sendClassRequest(boolean dir_in,
                                        int recipient,
                                        int bRequest,
                                        int wValue,
                                        int wIndex,
                                        int wLength,
                                        byte[] data) throws UsbException;

    /**
     * デバイスリクエストを送る。
     *
     * <li> TODO 未テスト
     */
    public native void sendVendorRequest(boolean dir_in,
                                         int recipient,
                                         int bRequest,
                                         int wValue,
                                         int wIndex,
                                         int wLength,
                                         byte[] data) throws UsbException;

    /**
     * USB デバイスが存在するかどうか確認する。
     * 
     * @return true デバイスは存在して正常に動作している
     *         false デバイスが取り外された
     *         (デバイスドライバーが remove されている)
     * @throws UsbException 無効である
     */
    private native boolean available() throws UsbException;

    /**
     * USB デバイスをリセットする
     */
    private native void reset() throws UsbException;

    /**
     * USB デバイスのデバイスディスクリプターを得る
     */
//    private native DeviceDescriptor getDeviceDescriptor() throws UsbException;

    /**
     * USB デバイスのコンフィグレーションディスクリプターを得る。
     */
//    private native ConfigurationDescriptor getConfigurationDescriptor() throws UsbException;

    /**
     * QualifierDescriptor を得る
     */
//    private native DeviceQualifierDescriptor getDeviceQualifierDescriptor() throws UsbException;

    /**
     * デバイスの OtherSpeedConfigurationDescriptor を得る。
     * OtherSpeedConfigurationDescriptor は ConfigurationDescriptor と同じ
     * フォーマットである。
     * 得られる内容が OtherSpeedConfigurationDescriptor であること以外は、
     * getConfigurationDescriptor と同様。 
     */
//    private native ConfigurationDescriptor getOtherSpeedConfigurationDescriptor() throws UsbException;

    // ------------------------------------------------------------------------

    /** */
    static {
        System.loadLibrary("UusbdWrapper");
    }
}

/* */
