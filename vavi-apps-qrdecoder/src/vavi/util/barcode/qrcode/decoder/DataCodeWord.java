/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;


/**
 * 文字モード指示子、文字数指示子、文字列を受取って連結しQRCodeの
 * データ語としての体裁を整えて格納するクラス。
 *
 * @version 新規作成 2002/12/12(Thu) 石戸谷　顕太朗
 *          追加変更 2002/12/14(Sat) 石戸谷　顕太朗
 */
class DataCodeWord {
    /** */
    public DataCodeWord() {
        charCount.clear();
        mode.clear();
        string.clear();
        dataCodeWord.clear();
    }

    /** DataCodeWord の初期化をする関数 */
    public void setDataCodeWord(final BinaryString mode, final BinaryString count, final BinaryString str, final Symbol sym) {
        if (count == null || mode == null || str == null) {
            throw new IllegalArgumentException("Invalid arg");
        }
        charCount = count;
        this.mode = mode;
        string = str;
        dataCodeWord.operatorPlusLet(mode);
        dataCodeWord.operatorPlusLet(charCount);
        dataCodeWord.operatorPlusLet(string);
        padDataCodeWord(sym);
    }
    
    /** 文字数指示子の取得関数 */
    public BinaryString getCharCount() {
        return charCount;
    }

    /** 文字モード指示子の取得関数 */
    public BinaryString getMode() {
        return mode;
    }

    /** 文字列の取得関数 */
    public BinaryString getString() {
        return string;
    }

    /** DataCodeWordの取得関数 */
    public BinaryString getDataCodeWord() {
        return dataCodeWord;
    }

    /** index から count 個のデータ語を返す関数 */
    public BinaryString getSubDataCodeWord(final int index, final int count) {
        return dataCodeWord.GetSubByte(index, count);
    }

    /**
     * モード指示子、文字数指示子、文字列の順に結合されたデータコードの末尾に埋め草
     * ビット及び埋め草ワードを連結する関数。
     */
    private void padDataCodeWord(final Symbol sym) {
        if (charCount == null || mode == null || string == null) {
            throw new IllegalArgumentException("One or more Data was not Initialized");
        }
        int wcodeword = sym.getDataCodeWords();
        dataCodeWord.SetMaxLengthByByte(wcodeword);
        
        // 終端パターン(0000)の付加、シンボル容量を満たしていたら終了
        if (dataCodeWord.GetLength() < wcodeword * 8) {
            int end = 8 - dataCodeWord.GetLength() % 8;
            for (int i = 0; i < end && i < 4; i++) {
                dataCodeWord.add(false);
            }
        } else {
            return ;
        }
        
        // 埋め草ビット(0)の付加、シンボル容量を満たしていたら終了
        if (dataCodeWord.GetLength() < wcodeword * 8) {
            int remaind = dataCodeWord.GetLength() % 8;
            if (remaind != 0) {
                int padbit = 8 - remaind;
                for (int i = 0; i < padbit; i++) {
                    dataCodeWord.add(false);
                }
            }
        } else {
            return;
        }
        
        // 埋め草コード語(11101100及び00010001)の付加、シンボル容量を満たしていたら終了
        if (dataCodeWord.GetLength() / 8 < wcodeword) {
            int padword = wcodeword - dataCodeWord.GetLength() / 8;
            for (int i = 0; i < padword; i++) {
                if ((i % 2) != 0) {
                    dataCodeWord.operatorPlusLet(new BinaryString("00010001"));
                } else {
                    dataCodeWord.operatorPlusLet(new BinaryString("11101100"));
                }
            }
            
        } else {
            return;
        }
    }
    
    /** */
    private BinaryString charCount;
    /** */
    private BinaryString mode;
    /** */
    private BinaryString string;
    /** */
    private BinaryString dataCodeWord;
}