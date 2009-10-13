/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.List;


/**
 * このクラスは入力されてきたCDataCodeWordをRSブロックに分けて格納
 * し、ErrorCodeGeneratorにRSブロックを渡し、ECブロックを取得する為
 * インターフェイスを用意します。
 *
 * @version 新規作成 2002/11/14(Sat) 石戸谷　顕太朗
 */
class Message {

    /** コンストラクタ */
    public Message() {
        numberOfBlocks = 0;
        data.clear();
        rsBlocks.clear();
        ecBlocks.clear();
    }

    /**
     * 配置用に並べ替えられたデータを受取り、RSブロックとECブロックに正しく分割し
     * 格納するメソッド。
     */
    public void setData(final BinaryString data, final Symbol sym) {
        if (data == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        if (sym == null && sym.isPartial()) {
            throw new IllegalArgumentException("Invalid Symbol");
        }
        if (data.GetLength() != sym.getWholeCodeWords() * 8 + sym.getRemainderBits()) {
            throw new IllegalArgumentException("Invalid Data");
        }
        // とりあえずデータクリア。
        rsBlocks.clear();
        ecBlocks.clear();
        rsLength.clear();
        ecLength.clear();
        data.clear();

        // 初期化処理開始
        int b1	= sym.getRsBlock1();
        int b2	= sym.getRsBlock2();
        int dwb1	= sym.getRSBlock1DataCodeWords();
        int dwb2	= sym.getRsBlock2DataCodeWords();
        int block = 0;
        int index = 0;
        int c = 0;
        data.SetMaxLengthByByte(sym.getWholeCodeWords());
        numberOfBlocks = b1 + b2;
        wholeECCW = sym.getECCodeWords();
        wholeDCW = sym.getWholeCodeWords() - wholeECCW;
        BinaryString bs = new BinaryString();

        for (int i = 0; i < b1; i++) {
            bs.SetMaxLengthByByte(dwb1);
            rsBlocks.add(bs);
            rsLength.add(new Integer(sym.getRSBlock1WholeCodes()));
            ecLength.add(new Integer(sym.getRSBlock1WholeCodes() - dwb1));
        }
        for (int i = 0; i < b2; i++) {
            bs.SetMaxLengthByByte(dwb2);
            rsBlocks.add(bs);
            rsLength.add(new Integer(sym.getRsBlock2WholeCodes()));
            ecLength.add(new Integer(sym.getRsBlock2WholeCodes() - dwb2));
        }

        // データを突っ込む
        for (int i = 0; i < wholeDCW; i++) {
            if (index < rsBlocks.get(block).GetLength() / 8) {
                rsBlocks.get(block).operatorPlusLet(data.GetSubByte(c, 1));
                data.operatorPlusLet(data.GetSubByte(c, 1));
                c++;
            }
            block = (i + 1) % numberOfBlocks;
            if (block == 0) {
                index++;
            }
        }
        block = 0;
        index = 0;
        for (int i = 0;i < wholeECCW; i++) {
            if (index < ecBlocks.get(block).GetLength() / 8) {
                ecBlocks.get(block).operatorPlusLet(data.GetSubByte(c, 1));
                data.operatorPlusLet(data.GetSubByte(c, 1));
                c++;
            }
            block = (i + 1) % numberOfBlocks;
            if (block == 0) {
                index++;
            }
        }
    }
    
    /** データコードワードの設定及びメッセージの初期化 */
    public void setDataCodeWord(final DataCodeWord data, final Symbol sym) {
        if (data == null) {
            throw new IllegalArgumentException("DataCodeWord is NULL");
        }
        int b1 = sym.getRsBlock1();
        int b2 = sym.getRsBlock2();
        int dwb1 = sym.getRSBlock1DataCodeWords();
        int dwb2 = sym.getRsBlock2DataCodeWords();
        int pos = 0;

        this.data.SetMaxLengthByByte(sym.getWholeCodeWords());
        this.data = data.getDataCodeWord();
        numberOfBlocks = b1 + b2;
        wholeECCW = sym.getECCodeWords();
        wholeDCW = sym.getWholeCodeWords() - wholeECCW;

        for (int i = 0; i < b1; i++) {
            rsBlocks.add(data.getSubDataCodeWord(pos, dwb1));
            rsLength.add(new Integer(sym.getRSBlock1WholeCodes()));
            ecLength.add(new Integer(sym.getRSBlock1WholeCodes() - dwb1));
            pos += dwb1;
        }
        for (int i = 0; i < b2; i++) {
            rsBlocks.add(data.getSubDataCodeWord(pos, dwb2));
            rsLength.add(new Integer(sym.getRsBlock2WholeCodes()));
            ecLength.add(new Integer(sym.getRsBlock2WholeCodes() - dwb2));
            pos += dwb2;
        }
    }

    /** ECCodeWord をブロックの末尾に格納 */
    public void addECBlock(BinaryString ecwords) {
        if (ecwords == null) {
            throw new IllegalArgumentException("ecwords is NULL");
        }
        ecBlocks.add(ecwords);
        data.operatorPlusLet(ecwords);
    }

    /** indexビット目のビットを返す。 */
    public boolean get(final int index) {
        if (data == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        return data.at(index);
    }

    /** Dataを配置用に並び替えて取得できるメソッド */
    public BinaryString getData() {
        BinaryString ret = new BinaryString();

        int block = 0;
        int index = 0;
        int c = 0;
        for (int i = 0; c < wholeDCW; i++) {
            if (index < rsBlocks.get(block).GetLength() / 8) {
                ret.operatorPlusLet(rsBlocks.get(block).GetSubByte(index, 1));
                c++;
            }
            block = (i + 1) % numberOfBlocks;
            if (block == 0) {
                index++;
            }
        }
        block = 0;
        index = 0;
        c = 0;
        for (int i = 0; c < wholeECCW; i++) {
            if (index < ecBlocks.get(block).GetLength() / 8) {
                ret.operatorPlusLet(ecBlocks.get(block).GetSubByte(index, 1));
                c++;
            }
            block = (i + 1) % numberOfBlocks;
            if (block == 0) {
                index++;
            }
        }
        return ret;
    }

    /** Dataメンバの取得メソッド */
    public BinaryString getPlainData() {
        return data;
    }

    /** データの長さを返す */
    public int getDataLength() {
        if (data == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        return data.GetMaxLength();
    }

    /** ブロックの数を返す */
    public int getNumberOfBlocks() {
        if (data == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        return numberOfBlocks;
    }

    /** index 番目のRSブロックの長さを返す。 */
    public int getRSLength(final int index) {
        if (index >= numberOfBlocks) {
            throw new IllegalArgumentException("index overflow");
        }
        return rsLength.get(index).intValue();
    }

    /** index 番目のエラー訂正語ブロックの長さを返す */
    public int getECLength(final int index) {
        if (index >= numberOfBlocks) {
            throw new IllegalArgumentException("index overflow");
        }
        return ecLength.get(index).intValue();
    }

    /** index 番目の RS ブロックを返す */
    public BinaryString getRSBlockAt(final int index) {
        if (index >= numberOfBlocks) {
            throw new IllegalArgumentException("index overflow");
        }
        return rsBlocks.get(index);
    }

    /** 全てのデータコード語の数 */
    private int wholeDCW;

    /** 全てのエラー訂正語の数 */
    private int wholeECCW;

    /** RS ブロックの数 (ECブロックの数はRSブロックの数に同じ。) */
    private int numberOfBlocks;

    /** RS ブロック、ECブロックを連結して格納しているデータ */
    private BinaryString data;

    /** RS ブロックの総コード語数 */
    private List<Integer> rsLength;

    /** EC ブロックのコード語数 */
    private List<Integer> ecLength;

    /** RS ブロック */
    private List<BinaryString> rsBlocks;

    /** エラー訂正語のブロック */
    private List<BinaryString> ecBlocks;
}

/* */
