/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;


/**
 * GPS Format.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 */
public interface GpsFormat {
    /**
     * Raw データを出力させる可能性がある場合は戻り値の {@link GpsData} へ
     * {@link BasicGpsData#setRawData(byte[])} メソッドで line を設定しておいてください。
     */
    GpsData parse(byte[] line);

    /** */
    byte[] format(GpsData gpsData);
}

/* */
