/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved. 
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスは受け取ったシンボルから型番情報を生成する。
 *
 * @version	新規作成 2003/02/24(Mon) 石戸谷　顕太朗
 *          追加変更 2003/02/27(Tue) 石戸谷　顕太朗
 */
class QRVersionInfo {
    /** */
    final String VERSION_BCHG = "1111100100101";

    /** コンストラクタ */
    public QRVersionInfo() {
        version = 0;
    }

    /** 型番情報を作成し返すメソッド */
    public void initialize(final Symbol symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("invalid ECL");
        }

        version = symbol.getVersion();

        if (version < 7) {
            versionInfo = new BinaryString("000000000000000000");
            return;
        }

        if (versionInfo != null) {
            versionInfo.clear();
        }

        BinaryString code = new BinaryString(version, 6);
        BinaryString ec;
        BinaryString pad = new BinaryString();
        BCHECCodeGenerator gen = new BCHECCodeGenerator();

        // 誤り訂正語の生成 (codeに足しているのは12乗)
        ec = gen.execute(code.add(new BinaryString("000000000000")), new BinaryString(VERSION_BCHG));

        // 帰ってきたコードが 10 bit に満たなければ前を 0 で埋める。
        if (ec.GetLength() < 12) {
            for (int i = 0; i < 12 - ec.GetLength(); i++) {
                pad.add(false);
            }
            ec = pad.operatorPlus(ec);
        }

        versionInfo = code.operatorPlus(ec);
    }

    /** 型番情報を取得 */
    public BinaryString getVersionInfo() {
        return versionInfo;
    }

    // デコーダー用

    /** 型番情報の設定してある String をデコード */
    public void initialize(final BinaryString String) {
        // TODO ; この位置に固有の処理を追加してください。
    }

    /** 型番を取得 */
    public int getVersion() {
        return version;
    }
    
    /** */
    private int version;

    /** */
    private BinaryString versionInfo;
}

/* */
