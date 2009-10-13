/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * このクラスはガロア体GF(2)上の多項式を表現する。
 * 多項式と多項式の加算、乗算をサポートする。
 *
 * @version	新規作成 2003/02/20(Thu) 石戸谷　顕太朗
 */
class BCHPolynomial {
    /** */
    protected BCHPolynomial() {}

    /** コピーコンストラクタ */
    public BCHPolynomial(final BCHPolynomial source){
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        List<BCHMonomial> v;
        v = source.polynomial;
        Collections.<BCHMonomial>sort(v);
        this.polynomial.addAll(v);
    }

    /** 単項を引数にとるコンストラクタ */
    public BCHPolynomial(final BCHMonomial temp) {
        if (temp == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        this.polynomial.add(temp);
    }

    /** BinaryStringを引数にとって初期化するコンストラクタ。 */
    public BCHPolynomial(final BinaryString str) {
        if (str == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        int max = str.GetLength();
        int n;
        for (int i = 0; i < max; i++) {
            n = max - i;
            if (str.at(i)) {
                this.polynomial.add(new BCHMonomial(n - 1));
            }
        }
        if (this.polynomial.isEmpty()) {
//          Initialized = false;
        } else {
            Collections.sort(this.polynomial);
        }
    }

    /** リストを引数に取るコンストラクタ */
    private BCHPolynomial(final List temp) {
        // @@@
    }

    /** 項のリストを引数に取るコンストラクタ */
    private void initBCHMonomialList(final List<BCHMonomial> source) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        Iterator<BCHMonomial> p = source.iterator();
        while (p.hasNext()) {
            int prep = this.polynomial.indexOf(p);
            if (prep == this.polynomial.size() - 1) {
                this.polynomial.add(p.next());
            }
        }
        Collections.sort(this.polynomial);
    }

    /** unsigned intのリストを引数にとって初期化するコンストラクタ */
    private void initIntegerList(final List<Integer> source) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        for (Integer p : source) {
            int prep = this.polynomial.indexOf(new BCHMonomial(p));
            if (prep == this.polynomial.size() - 1) {
                this.polynomial.add(new BCHMonomial(p));
            }
        }
        Collections.sort(this.polynomial);
    }

    /** 代入演算子の多重定義 */
    public final BCHPolynomial operatorLet(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }

        List<BCHMonomial> v;
        v = right.polynomial;
        this.polynomial.addAll(v);
        return this;
    }

    /** 加算代入演算子の多重定義 */
    public final BCHPolynomial operatorPlusLet(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        this.polynomial = plus(this, right).polynomial;
        return this;
    }

    /** 乗算代入演算子 */
    public final BCHPolynomial operatorMultiplyLet(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        this.polynomial = multiply(this, right).polynomial;
        return this;
    }

    /** */
    public final BCHPolynomial operatorDivideLet(final BCHPolynomial right) {
        throw new UnsupportedOperationException();
    }
    
    /** 剰余代入演算子の多重定義 */
    public final BCHPolynomial operatorModuloLet(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        this.polynomial = remainder(this, right).polynomial;
        return this;
    }

    /** 加算演算子の多重定義 */
    public BCHPolynomial operatorPlus(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        return plus(this, right);
    }

    /** 乗算演算子の多重定義 */
    public BCHPolynomial operatorMultiply(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        return multiply(this, right);
    }

    /** 剰余演算子の多重定義 */
    public BCHPolynomial operatorModulo(final BCHPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right operand is null");
        }
        return remainder(this, right);
    }

    /** indexで指定される位置の項を帰す */
    public BCHMonomial get(int index) {
        return polynomial.get(index);
    }

    /** データを文字列で取得 */
    public String toString() {
        StringWriter buffer = new StringWriter();

        int count = 0;
        for (BCHMonomial monomial : polynomial) {
            if (monomial.getPower() == 0) {
                buffer.write("1");
            } else if (monomial.getPower() == 1) {
                buffer.write("X");
            } else {
                buffer.write("X^");
                buffer.write(monomial.getPower());
            }
            count++;
            if (count < polynomial.size() - 1) {
                buffer.write(" + ");
            }
        }
        return buffer.toString();
    }
    
    /** データをunsigned intの配列で取得 */
    public List<Integer> getDataByUINTArray() {
        List<Integer> ret = new ArrayList<Integer>();
        for (BCHMonomial p : polynomial) {
            ret.add(new Integer(p.getPower()));
        }
        return ret;
    }

    /** データをBinaryStringで取得 */
    public BinaryString getDataByBinaryString() {
        int temp = 0;
        for (BCHMonomial p : polynomial) {
            temp += Math.pow(2, p.getPower());
        }
        return new BinaryString(temp, polynomial.get(0).getPower() + 1);
    }

    /**
     * 実際に加算処理する関数。
     * 同次数の項を消す。
     */
    private BCHPolynomial plus(final BCHPolynomial a, final BCHPolynomial b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        List<BCHMonomial> ret = a.polynomial;

        for (BCHMonomial pb : b.polynomial) {
            int pret = ret.indexOf(pb);
            if (pret == ret.size() - 1) {
                ret.add(pb);
            } else {
                ret.remove(pret);
            }
        }
        return new BCHPolynomial(ret);
    }

    /**
     * 実際に乗算処理する関数。
     * 相手が単項式でない限りは計算しないで例外を出す。ただ項の次数を足すだけ。
     */
    private BCHPolynomial multiply(final BCHPolynomial a, final BCHPolynomial b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        if (!(a.polynomial.size() == 1 || b.polynomial.size() == 1)) {
            throw new IllegalArgumentException("cannot multiply polynomial and polynomial");
        }
        List<BCHMonomial> ret = new ArrayList<BCHMonomial>();
        Iterator<BCHMonomial> p;
        BCHMonomial m;
        if (a.polynomial.size() == 1) {
            m = a.polynomial.get(0);
            p = b.polynomial.iterator();
        } else {
            m = b.polynomial.get(0);
            p = a.polynomial.iterator();
        }
        while (p.hasNext()) {
            ret.add(p.next().operatorMultiply(m));
        }
        return new BCHPolynomial(ret);
    }

    /**
     * 実際に剰余を求める関数。
     * Galoisとは違い、普通の除算をし余を求める。
     */
    private BCHPolynomial remainder(final BCHPolynomial g, final BCHPolynomial i) {
        if (g == null || i == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        if (g.get(0).getPower() > i.get(0).getPower()) {
            throw new IllegalArgumentException("Invalid Paramiter");
        }

//      BCHMonomial m;
        BCHPolynomial ret = i;
//      List<BCHMonomial> pret;
        BCHMonomial pi = i.polynomial.get(0);
        BCHMonomial pg = g.polynomial.get(0);
        int n = pi.getPower() - pg.getPower();

        while (true) {
            ret = ret.operatorPlus(g.operatorMultiply(new BCHPolynomial(new BCHMonomial(n))));
            pi = ret.polynomial.get(0);
            if (pi.getPower() < pg.getPower()) {
                break;
            }
            n = pi.getPower() - pg.getPower();
        }
        return ret;
    }
    
    /** */
    private List<BCHMonomial> polynomial;
}

/* */
