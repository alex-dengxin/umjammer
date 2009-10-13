/*
 * Copyright (c) 2002-2003 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;

import java.io.IOException;


/**
 * このクラスは受け取った文字列からQRコードを生成して返す。
 * 各テーブルのCFileReaderは、外部で宣言するがその生存期間は必ず
 * CQREncoderよりも長く取ること。
 *
 * @version	新規作成 2003/02/25(Tue) 石戸谷　顕太朗
 */
class QREncoder {
    enum CharMode {
        NUMERIC(1),
        ALPHA(2),
        ASCII(4),
        KANJI(8);
        int value;
        CharMode(int value) {
            this.value = value;
        }
        int getValue() {
            return value;
        }
    }

    /** コンストラクタ */
    public QREncoder() {
    }

    /** エンコーダーを初期化する関数 */
    public void initialize(final String pfr, FileReader pow, final String vfr, FileReader ver, final String efr, FileReader exp) throws IOException {
        pTable.initialize(pfr, pow);
        vTable.initialize(vfr, ver);
        eTable.initialize(efr, exp);
    }

    /** 実際に QRCode を生成するメソッド */
    public BinaryImage execute(final String str, final CharMode mode, final VersionTable.ErrorCollectionLevel ecl, final int ver, final boolean quiet) {

        CharacterCodeConverter con = new AsciiCodeConverter();
        Symbol sym = vTable.getSymbol(ver, ecl);
        DataCodeWord codeword = new DataCodeWord();
        Message msg = new Message();
        QRCodeImage image = new QRCodeImage();
        MaskSelector sel = new MaskSelector();
        ECCodeGenerator ecgen = new ECCodeGenerator();
        QRFormInfo fi = new QRFormInfo();
        QRVersionInfo vi = new QRVersionInfo();
        int cclength = 0;
        int unit = 0;

        fi.initialize(sym, MaskDecorator.Mask.NOTMASKED);
        vi.initialize(sym);
        // モード指示子と変換クラスの選択
        switch (mode) {
        case NUMERIC:
            cclength = sym.getCharCountNumeric();
            // TODO 未実装
            break;
        case ALPHA:
            cclength = sym.getCharCountAlpha();
            // TODO 未実装
            break;
        case ASCII:
            cclength = sym.getCharCountAscii();
            unit = 8;
            con = new AsciiCodeConverter();
            break;
        case KANJI:
            cclength = sym.getCharCountKanji();
            // TODO 未実装
            break;
        }

        // メッセージの変換
        BinaryString code = con.convert(str);

        // メッセージの構築
        codeword.setDataCodeWord(new BinaryString(mode.getValue(), 4), new BinaryString(code.GetLength() / unit, cclength), code, sym);
        msg.setDataCodeWord(codeword, sym);

        // エラー訂正語の格納
        for (int i = 0; i < msg.getNumberOfBlocks(); i++) {
            msg.addECBlock(ecgen.execute(msg.getECLength(i), msg.getRSLength(i), msg.getRSBlockAt(i), pTable, eTable));
        }

        // 各種情報のセット
        image.setMode(QRCodeImage.Mode.ENCODER);
        image.setMessage(msg, sym);
        image.setMaskCode(sel.rateMask(image));
        image.setFormInfo(fi.getFormInfo());
        if (ver >= 7) {
            image.setVersionInfo(vi.getVersionInfo());
        }
        if (quiet) {
            BinaryImage ret = new BinaryImage();
            BinaryImage temp = image.getQRCodeImage();
            ret.initialize(temp.getMaxRow() + 8, temp.getMaxCol() + 8, false);
            return ret.or(4, 4, temp);
        } else {
            return image.getQRCodeImage();
        }
    }

    /** */
    private PowerTable pTable = new PowerTable();
    /** */
    private VersionTable vTable = new VersionTable();
    /** */
    private ExpressionTable eTable = new ExpressionTable();
    
    //----
    
    /** */
    public static void main (String[] args) throws Exception {
        QREncoder encoder = new QREncoder();
        CSVFileReader pFile = new CSVFileReader();
        CSVFileReader vFile = new CSVFileReader();
        CSVFileReader eFile = new CSVFileReader();
        
        encoder.initialize("power.csv", vFile, "version.csv", pFile, "expression.csv", eFile);
        
        BinaryImage image = encoder.execute("test", CharMode.ASCII, VersionTable.ErrorCollectionLevel.L, 1, true);
        
        PBMImage pbm = new PBMImage();
        pbm.initialize(image);
        pbm.saveToFile("test.pbm", 1);
    }
}

/* */
