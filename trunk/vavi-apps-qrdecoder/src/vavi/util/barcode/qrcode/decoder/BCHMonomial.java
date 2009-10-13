/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスはガロア体 GF(2) 上の単項式を表現する。
 * 単項式と単項式の加算、乗算をサポートし、ソートの為の比較演算子を
 * サポートする。
 * 単項式 * x ^ X を表現する {@link BCHMonomial} は、
 * {@link #valPower} = X (xの次数)
 * だけをデータとして持つ。
 *
 * @version	新規作成 2003/02/20(Thu) 石戸谷　顕太朗
 */
class BCHMonomial implements Comparable<BCHMonomial> {
    /** コンストラクタ */
    private BCHMonomial() {
        power = 0;
    }
    
    /** */
    public BCHMonomial(final BCHMonomial source) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        power = source.power;
    }
    
    /** */
    public BCHMonomial(final int power) {
        this.power = power;
    }

    /** = 演算子の多重定義 */
    public final BCHMonomial operatorLet(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (right == this) {
            throw new IllegalArgumentException("right is same to this");
        }
        power = right.power;
        return this;
    }

    /**
     * 乗算代入演算子の多重定義
     * 次数を足すだけ
     */
    public final BCHMonomial operatorMultiplyLet(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power += right.power;
        return this;
    }

    /**
     * 加算代入演算子の多重定義
     * 次数が同じなら、0 になる (xor をとるということ)
     */
    public final BCHMonomial operatorPlusLet(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power ^= right.power;
        return this;
    }

    /*
     * 除算代入演算子の多重定義
     * 次数を引く
     */
    public final BCHMonomial operatorDivideLet(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power -= right.power;
        return this;
    }

    /**
     * 乗算演算子の多重定義
     * 次数を足すだけ
     */
    public BCHMonomial operatorMultiply(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power += right.power;
        return this;
    }

    /**
     * 加算演算子の多重定義
     * 次数が同じなら、0 になる（XORをとるということ）
     */
    public BCHMonomial operatorPlus(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power ^= right.power;
        return this;
    }

    /** 
     * 除算演算子の多重定義
     * 次数を引く
     */
    public BCHMonomial operatorDivide(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        power -= right.power;
        return this;
    }

    /** 比較演算子の多重定義 */
    public boolean operatorLessThan(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (power < right.power) {
            return true;
        }
        return false;
    }

    /** 比較演算子の多重定義 */
    public boolean operatorGreaterThan(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (power > right.power) {
            return true;
        }
        return false;
    }

    /** 等号演算子の多重定義 */
    public boolean operatorEqual(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (power == right.power) {
            return true;
        }
        return false;
    }
    
    /** 不等号演算子の多重定義 */
    public boolean operatorNotEqual(final BCHMonomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (power != right.power) {
            return true;
        }
        return false;
    }

    /** 次数を返す */
    public final int getPower() {
        return power;
    }
    
    /** */
    private int power;

    /** */
    public int compareTo(BCHMonomial target) {
        return power - target.power;
    }
}

/* */
