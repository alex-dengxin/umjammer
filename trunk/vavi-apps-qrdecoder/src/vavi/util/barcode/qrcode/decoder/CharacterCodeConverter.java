/*
 * Copyright (c) 2002-2003 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.List;


/**
 * このクラスは、与えられた文字列をビット列に直すクラスの抽象基底クラス
 *
 * @version	新規作成 2002/11/25(Sut) 柴田　真武
 *          追加変更 2003/02/25(Tue) 石戸谷　顕太朗
 */
interface CharacterCodeConverter {

    BinaryString convert(final String data);
    BinaryString convert(final byte[] data);
    BinaryString convert(final List<Byte> data);
}

/* */
