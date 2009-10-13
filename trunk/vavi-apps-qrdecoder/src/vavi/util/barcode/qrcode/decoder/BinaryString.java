/*
 *  Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * このクラスは、'0'と'1'で表されるバイナリ−データを簡便に扱う為に
 * 作られたクラスです。10進→2進、2進→10進の相互変換、及び連結等を
 * サポートしています。
 * 注意事項 このクラスはBorland C++Builderで使用する事を前提にコーディングさ
 * れています。Microsoft Visual C++で使用する場合には、以下の注意に
 * 従って書き換えてください。
 * ・pragma を消す。
 * ・vcl.h インクルード文を消す。
 * ・GetDataByAnsiStringメソッドを消す。
 * ・Exception関係
 * 以上でVCでコンパイルできるようになります。
 * 
 * @version	新規作成 2002/11/08(Fri) 石戸谷顕太朗
 *          実装完了 2002/11/09(Sat) 石戸谷顕太朗
 *          追加変更 2002/11/10(Sun) 石戸谷顕太朗
 *          追加変更 2002/11/13(Wed) 石戸谷顕太朗
 *          追加変更 2002/12/12(Thu) 石戸谷顕太朗
 *          追加変更 2003/02/24(Mon) 石戸谷顕太朗
 *          追加変更 2003/02/25(Tue) 石戸谷顕太朗
 */
class BinaryString {
 
    /** デフォルトコンストラクタ */
    public BinaryString() {
        this.length = 0;
        maxLength = 0;
    }

    /** コピーコンストラクタ */
    public BinaryString(BinaryString temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        if (temp.GetDataByBoolArray() == null) {
            return;
        }
        this.length = temp.GetLength();
        maxLength = temp.GetMaxLength();
        List<Boolean> v = temp.GetDataByBoolArray();
        data.addAll(v);
    }

    /** List を引数にとって初期化するコンストラクタ */
    public BinaryString(final List temp) {
        // @@@ List<型> で振り分け
    }

    /** Boolean の配列を引数にとって初期化するコンストラクタ */
    private void initBooleanList(final List<Boolean> temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        if (temp.size() == 0) {
            return;
        }
        this.length = temp.size();
        maxLength = temp.size();
        data.addAll(temp);
    }

    /**
     * char* で初期化するコンストラクタ。
     * "0101110"のように'0'と'1'のみで構成された文字列をBinaryStringに変換する。
     * '0'と'1'以外の文字が変換中に現れたら、中断し、例外を投げる。
     */
    public BinaryString(byte[] tch) {
        if (!data.isEmpty()) {
            data.clear();
        }
        String temp = new String(tch);
        if (temp.length() == 0) {
            return;
        }
        this.length = temp.length();
        maxLength = temp.length();

        for (int i = 0; i < temp.length(); i++) {
            char p = temp.charAt(i);
            if (p == '0') {
                data.add(Boolean.FALSE);
            } else if (p == '1') {
                data.add(Boolean.TRUE);
            } else {
                release();
                throw new IllegalArgumentException("Can't convert from char*");
            }
            p++;
        }
    }

    /**
     * string で初期化するコンストラクタ。
     * "0101110"のように'0'と'1'のみで構成された文字列をBinaryStringに変換する。
     * '0'と'1'以外の文字が変換中に現れたら、中断し、例外を投げる。
     */
    public BinaryString(String temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        if (temp == null) {
            return;
        }
        this.length = temp.length();
        maxLength = temp.length();

        for (int p = 0; p < length; p++) {
            if (temp.charAt(p) == '0') {
                data.add(Boolean.FALSE);
            } else if (p == '1') {
                data.add(Boolean.TRUE);
            } else {
                release();
                throw new IllegalArgumentException("Can't convert from string");
            }
        }
    }

    /** 8bitの符号付データを渡された時のコンストラクタ */
    public BinaryString(byte temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        this.length = 1 * 8;
        maxLength = 1 * 8;
        int offset = (int) Math.pow(2, length - 1);
        for (int i = 0; i < length; i++) {
            //一番左のビットを取り出す。
            if (((temp << i) & offset) != 0) {
                data.add(Boolean.TRUE);
            } else {
                data.add(Boolean.FALSE);
            }
        }
    }

    /** 16bitの符号付整数を渡された時のコンストラクタ */
    public BinaryString(short temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        this.length = 2 * 8;
        maxLength = 2 * 8;
        int offset = (int) Math.pow(2, length - 1);
        for (int i = 0; i < length; i++) {
            //一番左のビットを取り出す。
            if (((temp << i) & offset) != 0) {
                data.add(Boolean.TRUE);
            } else {
                data.add(Boolean.FALSE);
            }
        }
    }

    /** 32bitの符号付整数を渡された時のコンストラクタ */
    public BinaryString(int temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        this.length = 4 * 8;
        maxLength = 4 * 8;
        int offset = (int) Math.pow(2, length - 1);
        for (int i = 0; i < length; i++) {
            //一番左のビットを取り出す。
            if (((temp << i) & offset) != 0) {
                data.add(Boolean.TRUE);
            } else {
                data.add(Boolean.FALSE);
            }
        }
    }

    /** 32bitの符号付整数を渡された時のコンストラクタ */
    public BinaryString(long temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        this.length = 4 * 8;
        maxLength = 4 * 8;
        int offset = (int) Math.pow(2, length - 1);
        for (int i = 0; i < length; i++) {
            //一番左のビットを取り出す。
            if (((temp << i) & offset) != 0) {
                data.add(Boolean.TRUE);
            } else {
                data.add(Boolean.FALSE);
            }
        }
    }
    
    /** 8bitの符号無整数を渡された時のコンストラクタ */
//    public BinaryString(unsigned char data) {
//        if (!Data.empty()) {
//            Data.clear();
//        }
//        Length = sizeof(unsigned char) * 8;
//        MaxLength = sizeof(unsigned char) * 8;
//        int offset = pow(2, Length - 1);
//        for (int i = 0; i < Length; i++) {
//            // 一番左のビットを取り出す。
//            if ((temp << i) & offset) {
//                Data.add(true);
//            } else {
//                Data.add(false);
//            }
//        }
//    }
    
    /** 16bitの符号無整数を渡された時のコンストラクタ */
//    public BinaryString(unsigned short data) {
//        if (!Data.empty()) {
//            Data.clear();
//        }
//        Length = sizeof(unsigned short) * 8;
//        MaxLength = sizeof(unsigned short) * 8;
//        unsigned int offset = pow(2, Length - 1);
//        for (unsigned int i = 0; i < Length; i++) {
//            // 一番左のビットを取り出す。
//            if ((temp << i) & offset) {
//                Data.push_back(true);
//            } else {
//                Data.push_back(false);
//            }
//        }
//    }

    /** 32bitの符号無整数を渡された時のコンストラクタ */
//    public BinaryString(unsigned int data) {
//        if (!Data.empty()) {
//            Data.clear();
//        }
//        Length = sizeof(unsigned int) * 8;
//        MaxLength = sizeof(unsigned int) * 8;
//        unsigned int offset = pow(2, Length - 1);
//        for (unsigned int i = 0; i < Length; i++) {
//            //  一番左のビットを取り出す。
//            if ((temp << i) & offset) {
//                Data.push_back(true);
//            } else {
//                Data.push_back(false);
//            }
//        }
//    }

    /** 32bitの符号無整数を渡された時のコンストラクタ */
//    public BinaryString(unsigned long data) {
//        if (!Data.empty()) {
//            Data.clear();
//        }
//        Length = sizeof(unsigned long) * 8;
//        MaxLength = sizeof(unsigned long) * 8;
//        int offset = pow(2, Length - 1);
//        for (int i = 0; i < Length; i++) {
//            //一番左のビットを取り出す。
//            if ((temp << i) & offset) {
//                Data.add(true);
//            } else {
//                Data.add(false);
//            }
//        }
//    }

    /** 8bitsの符号無し整数型unsigned charのベクターを受け取るコンストラクタ */
    private void initByteList(final List<Byte> temp) {
        if (!data.isEmpty()) {
            data.clear();
        }

        int size = 1 * 8;
        int offset = (int) Math.pow(2, size - 1);
        this.length = temp.size() * size;
        maxLength = temp.size() * size;

        for (Byte p : temp) {
            for (int i = 0; i < size; i++) {
                // 一番左のビットを取り出す。
                if (((p.byteValue() << i) & offset) != 0) {
                    data.add(Boolean.TRUE);
                } else {
                    data.add(Boolean.FALSE);
                }
            }
        }
    }

    /** 16bitsの符号無し整数型unsigned shortのベクターを受け取るコンストラクタ */
    private void initShortList(final List<Short> temp) {
        if (!data.isEmpty()) {
            data.clear();
        }
        
        int size = 2 * 8;
        int offset = (int) Math.pow(2, size - 1);
        this.length = temp.size() * size;
        maxLength = temp.size() * size;
        
        for (Short p : temp) {
            for (int i = 0; i < size; i++) {
                // 一番左のビットを取り出す。
                if (((p.shortValue() << i) & offset) != 0) {
                    data.add(Boolean.TRUE);
                } else {
                    data.add(Boolean.FALSE);
                }
            }
        }
    }

    /** 32bitsの符号無し整数型unsigned intのベクターを受け取るコンストラクタ */
    private void initIntegerList(final List<Integer> temp) {
        if (!data.isEmpty()) {
            data.clear();
        }

        int size = 4 * 8;
        int offset = (int) Math.pow(2, size - 1);
        this.length = temp.size() * size;
        maxLength = temp.size() * size;

        for (Integer p : temp) {
            for (int i = 0; i < size; i++) {
                // 一番左のビットを取り出す。
                if (((p.intValue() << i) & offset) != 0) {
                    data.add(Boolean.TRUE);
                } else {
                    data.add(Boolean.FALSE);
                }
            }
        }
    }

    /** 32bitsの符号無し整数型unsigned longのベクターを受け取るコンストラクタ */
    private void initLongList(final List<Long> temp) {
        if (!data.isEmpty()) {
            data.clear();
        }

        int size = 8 * 8;
        int offset = (int) Math.pow(2, size - 1);
        length = temp.size() * size;
        maxLength = temp.size() * size;

        for (Long p : temp) {
            for (int i = 0; i < size; i++) {
                // 一番左のビットを取り出す。
                if (((p.longValue() << i) & offset) != 0) {
                    data.add(Boolean.TRUE);
                } else {
                    data.add(Boolean.FALSE);
                }
            }
        }
    }

    /** 長さを指定して Data を格納する */
    public BinaryString(int temp, int length) {
        if (!data.isEmpty()) {
            data.clear();
        }
        if (length == 0) {
            length = 0;
            maxLength = 0;
            return ;
        }
        this.length = length;
        maxLength = length;
        int offset = (int) Math.pow(2, length - 1);
        for (int i = 0; i < length; i++) {
            // 一番左のビットを取り出す。
            if (((temp << i) & offset) != 0) {
                data.add(Boolean.TRUE);
            } else {
                data.add(Boolean.FALSE);
            }
        }
    }

    /** 等号演算子の多重定義 */
    public boolean operatorEqual(final BinaryString right) {
        if (right == null) {
            throw new IllegalArgumentException("right is NULL");
        }
        if (right.GetLength() != length) {
            return false;
        }
        Iterator<Boolean> p = data.iterator();
        Iterator<Boolean> rp = right.data.iterator();
        while (p.hasNext() && rp.hasNext()) {
            if (p.next() != rp.next()) {
                return false;
            }
        }
        return true;
    }

    /** 不等号演算子の多重定義 */
    public boolean operatorNotEqual(final BinaryString right) {
        if (right == null) {
            throw new IllegalArgumentException("right is NULL");
        }
        if (right.GetLength() != length) {
            return true;
        }
        Iterator<Boolean> p = data.iterator();
        Iterator<Boolean> rp = right.data.iterator();
        while (p.hasNext() && rp.hasNext()) {
            if (p.next() != rp.next()) {
                return true;
            }
        }
        return false;
    }

    /** 代入演算子の多重定義。C++ 以外で実装する場合は Copy 等のメソッドを作る。 */
    public final BinaryString operatorPlus(final BinaryString right) {
        if (!data.isEmpty()) {
            data.clear();
        }
        if (right.GetDataByBoolArray().isEmpty()) {
            throw new IllegalArgumentException("Data is NULL");
        }

        List<Boolean> v = right.GetDataByBoolArray();
        data.addAll(v);
        length = v.size();
        maxLength = v.size();
        return this;
    }

    /**
     * 加算演算子の多重定義（バイナリ文字列の連結演算子）
     * 概念的にはPerlの"0001" . "0101"で"00010101"と連結される感じ。
     */
    public final BinaryString operatorXor(final BinaryString right) {
        if (right.GetDataByBoolArray().isEmpty()) {
            throw new IllegalArgumentException("Data is NULL");
        }

        List<Boolean> temp = data;
        List<Boolean> v = right.GetDataByBoolArray();
        temp.addAll(v);
        return new BinaryString(temp);
    }

    /** 加算代入演算子の多重定義。意味は加算演算子とほぼ同じ。 */
    public final BinaryString operatorLet(final BinaryString right) {
        if (right.GetDataByBoolArray() == null) {
            throw new IllegalArgumentException("Data is NULL");
        }

        List<Boolean> v = right.GetDataByBoolArray();
        data.addAll(v);
        this.length = data.size();
        return this;
    }

    /** OR 演算子の多重定義。 */
    public final BinaryString operatorPlusLet(final BinaryString right) {
        if (right.GetDataByBoolArray() == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        if (right.GetLength() != length) {
            throw new IllegalArgumentException("Length is not match");
        }
        List<Boolean> v = new ArrayList<Boolean>();
        Iterator<Boolean> p = data.iterator();
        Iterator<Boolean> rp = right.data.iterator();
        while (p.hasNext() && rp.hasNext()) {
            v.add(new Boolean(p.next().booleanValue() ^ rp.next().booleanValue()));
        }
        return new BinaryString(v);
    }

    /** XOR 代入演算子の多重定義 */
    public final BinaryString operatorXorLet(final BinaryString right) {
        if (right.GetDataByBoolArray() == null) {
            throw new IllegalArgumentException("Data is NULL");
        }
        if (right.GetLength() != length) {
            throw new IllegalArgumentException("Length is not match");
        }
        Iterator<Boolean> p = data.iterator();
        Iterator<Boolean> rp = right.data.iterator();
        while (p.hasNext() && rp.hasNext()) {
            Boolean b = p.next();
            b = new Boolean(b.booleanValue() ^ rp.next().booleanValue()); // @@@
        }
        return this;
    }

    /**
     * () 演算子の多重定義。
     * index (ビット) であらわされるオフセットから、count ビット抜き出して返す。
     * 領域をはみ出る時は例外を投げる。
     * GetSubBitに同じ
     */
    public final BinaryString operatorFunction(final int index, final int count) {
        if (data.isEmpty() || index > length || index + count > length) {
            throw new IllegalArgumentException("index overflow");
        }
        List<Boolean> temp = new ArrayList<Boolean>(count);
        temp.addAll(data.subList(index, index + count));
        return new BinaryString(temp);
    }

    /**
     * [] 演算子の多重定義
     * indexで与えられるインデックスのbitをひとつだけ返す。
     * 領域をはみ出る場合は例外を投げる。
     * Atと同じ
     */
    public final boolean operatorArray(final int index) {
        if (data.isEmpty() || index > length) {
            throw new IllegalArgumentException("index overflow");
        }
        return data.get(index).booleanValue();
    }

    /**
     * データをstring文字列で返す関数
     * "01011111"というような'0'と'1'で構成される文字列を返す。
     */
    public String GetDataByString() {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data NULL");
        }
        String str = "";
        for (Boolean p : data) {
            if (p.booleanValue()) {
                str += "1";
            } else {
                str += "0";
            }
        }
        return str;
    }

    /** bool の配列で、データを返す関数 () 演算子 */
    public List<Boolean> GetDataByBoolArray() {
        return data;
    }

    /**
     * unsigned intの配列でデータを返す関数。
     * 1バイトづつ整数に直したものを配列に格納して返します。
     */
    public List<Integer> GetDataByUINTArray() {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data NULL");
        }
        List<Integer> v = new ArrayList<Integer>();
        int temp;
        for (Boolean p : data) {
            temp = 0;
            for (int i = 7; i >= 0; i--) {
                if (p.booleanValue()) {
                    temp += Math.pow(2, i);
                }
            }
            v.add(new Integer(temp));
        }
        return v;
    }

    /**
     * UCHARの配列でデータを返す関数。
     * 1バイトづつ整数に直したものを配列に格納して返します。
     */
    public List<Byte> GetDataByUCHARArray() {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data NULL");
        }
        List<Byte> v = new ArrayList<Byte>();
        byte temp = 0;
        for (Boolean p : data) {
            for (int i = 7; i >= 0; i--) {
                if (p.booleanValue()) {
                    temp += Math.pow(2, i);
                }
            }
            v.add(new Byte(temp));
        }
        return v;
    }

    /** Length を返す。Length はビット。 */
    public int GetLength() {
        return length;
    }

    /** MaxLength をビット単位で返す。 */
    public int GetMaxLength() {
        return maxLength;
    }

    /** MaxLengthをバイト単位で返す。 */
    public int GetMaxLengthByByte() {
        return maxLength / 8;
    }

    /** MaxLengthをセットする。ビット単位。 */
    public void SetMaxLength(int len) {
        maxLength = len;
    }

    /** maxLength をバイト単位でセットする。 */
    public void SetMaxLengthByByte(int len) {
        maxLength = len * 8;
    }

    /** index で指定されたオフセットから、count 個の bit を取り出して返す。 */
    public BinaryString GetSubBit(final int index, final int count) {
        if (data.isEmpty() || index > length || index + count > length) {
            throw new IllegalArgumentException("index overflow");
        }
        List<Boolean> temp = new ArrayList<Boolean>(count);
        temp.addAll(data.subList(index, index + count));
        return new BinaryString(temp);
    }

    /** index で指定された位置のバイトを unsigned int になおして返す関数。 */
    public int getSubByte(final int index) {
        if (data.isEmpty() || (index * 8) >= length) {
            throw new IllegalArgumentException("index overflow");
        }
        int temp = 0;
        int c = 0;
        for (int i = 7; index * 8 + c < data.size() && i >= 0; i--) {
            Boolean p = data.get(index * 8 + c);
            if (p.booleanValue()) {
                temp += Math.pow(2, i);
            }
            c++;
        }
        return temp;
    }

    /** indexで指定された位置のバイトをunsigned intになおして返す関数。 */
    public char GetSubByteByUCHAR(final int index) {
        if (data.isEmpty() || (index * 8) >= length) {
            throw new IllegalArgumentException("index overflow");
        }
        char temp = 0;
        int c = 0;
        for (int i = 7; index * 8 + c < data.size() && i >= 0; i--) {
            Boolean p = data.get(index * 8 + c);
            if (p.booleanValue()) {
                temp += Math.pow(2, i);
            }
            c++;
        }
        return temp;
    }

    /**
     * index で指定された位置から Byte 分を unsigned int の配列にして返す関数。
     * 8 Bit で 1 つの unsigned int
     */
    public List<Integer> GetSubByteByUINTArray(final int index, final int byte_) {
        if (data.isEmpty() || index >= length || index + byte_ * 8 > length) {
            throw new IllegalArgumentException("index overflow");
        }
        List<Integer> v = new ArrayList<Integer>();
        int temp = 0;
        int count = 0;
        int c = 0;
        while (index * 8 + c < data.size() && count < (index + byte_) * 8) {
            for (int i = 7; i >= 0; i--) {
                Boolean p = data.get(index * 8 + c);
                if (p.booleanValue()) {
                    temp += Math.pow(2, i);
                }
                c++;
            }
            v.add(new Integer(temp));
            count++;
        }
        return v;
    }

    /**
     * index で指定された位置から Byte 分を UCHAR の配列にして返す関数。
     * 8 Bit で 1 つのブロック
     */
    public List<Byte> GetSubByteByUCHARArray(final int index, final int byte_) {
        if (data.isEmpty() || index >= length || index + byte_ * 8 > length) {
            throw new IllegalArgumentException("index overflow");
        }
        List<Byte> v = new ArrayList<Byte>();
        int temp = 0;
        int count = 0;
        int c = 0;
        while (index * 8 + c < data.size() && count < (index + byte_) * 8) {
            for (int i = 7; i >= 0; i--) {
                Boolean p = data.get(index * 8 + c);
                if (p.booleanValue()) {
                    temp += Math.pow(2, i);
                }
                c++;
            }
            v.add(new Byte((byte) (temp & 0xff)));
            count++;
        }
        return v;
    }
    
    /** indexで指定された位置からByte分をバイナリストリングにして返す関数。 */
    public BinaryString GetSubByte(final int index, final int byte_) {
        if (data.isEmpty() || index > length || index + byte_ * 8 > length) {
            throw new IllegalArgumentException("Error : index overflow");
        }
        List<Boolean> v = new ArrayList<Boolean>();
        v.addAll(data.subList(index * 8, index * 8 + byte_ * 8));
        return new BinaryString(v);
    }

    /**
     * ユーティリティー
     * データをクリアする関数。
     * あくまでもデータをクリアするだけで、maxLength はクリアしない。
     */
    public void clear() {
        if (!data.isEmpty()) {
            data.clear();
        }
        length = 0;
    }

    /** []関数と同じ index が領域からはみ出ている場合は例外を投げる。 */
    public boolean at(final int index) {
        if (data.isEmpty() || index > length) {
            throw new IllegalArgumentException("index overflow");
        }
        return data.get(index).booleanValue();
    }

    /** ＋演算子と同じ。 */
    public final BinaryString add(BinaryString temp) {
        if (temp.GetDataByBoolArray() == null) {
            throw new IllegalArgumentException("index overflow");
        }
        if (length + temp.GetLength() > maxLength) {
            throw new IllegalArgumentException("Data Overflow");
        }

        data.addAll(temp.GetDataByBoolArray());
        length = data.size();
        return this;
    }

    /** index番目のビットを反転させる。 */
    void flip(int index) {
        if (data.isEmpty() || index >= length) {
            throw new IllegalArgumentException("Error : index overflow @CBinaryString::Flip");
        }
        data.set(index, new Boolean(!data.get(index).booleanValue()));
    }

    /** 1bitを配列の一番後ろに入れる。 */
    void add(boolean bit) {
        this.length += 1;
        data.add(new Boolean(bit));
    }

    /**
     * データの解放をする関数。
     * すべてのデータメンバの値をリセットする。
     */
    private void release() {
        if (!data.isEmpty()) {
            data.clear();
        }
        this.length = 0;
        maxLength = 0;
    }

    /** データ */
    private List<Boolean> data;

    /** 今現在の長さ (データが入ってるところまでの長さ) */
    private int length;

    /** 最大の長さ */
    private int maxLength;
}