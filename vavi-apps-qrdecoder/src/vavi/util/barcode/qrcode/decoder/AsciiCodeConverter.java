/*
 * Copyright (c) 2002-2003 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.List;


/**
 * このクラスは、与えられた文字列を8バイトで区切られたビット列に直すクラス
 *
 * @version	新規作成 2002/11/25(Sut) 柴田 真武
 *          追加変更 2003/02/25(Tue) 石戸谷 顕太朗
 */
class AsciiCodeConverter implements CharacterCodeConverter {
    /** コンストラクタ */
    public AsciiCodeConverter() {
    }

    /** BinaryString に変換する */
    public BinaryString convert(final String data) {
        List<Byte> v = new ArrayList<Byte>();
        for (int i = 0; i < data.length(); i++) {
            v.add(new Byte((byte) data.charAt(i)));
        }
        return new BinaryString(v);
    }

    /** BinaryString に変換する */
    public BinaryString convert(final byte[] data) {
        List<Byte> v = new ArrayList<Byte>();
        for (int i = 0; i < data.length; i++ ) {
            v.add(new Byte(data[i]));
        }
        return new BinaryString(v);
    }

    /** BinaryString に変換する */
    public BinaryString convert(final List<Byte> data) {
        return new BinaryString(data);
    }
}

/* */
