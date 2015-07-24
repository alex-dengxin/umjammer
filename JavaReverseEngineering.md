
# Tools #

  * [jad](http://www.varaneckas.com/jad) (1.5.8g)
    * 長所
      * 今のところ最強
    * 短所
      * Exception 周りが下手、finally は絶望的
      * switch もダメダメ
      * for, while の扱いが逆なんじゃない？
        * 2重ループ、if ... else if ... else ... とかも弱い
      * inner class もちょっとおかしい
      * synchronize もダメ
      * enum 超面倒
      * 開発終了？

  * [JD](http://java.decompiler.free.fr/) (core 0.6.0)
    * 長所
      * Exception はまあまあ
      * switch も~~まあまあ~~* break が抜けてる気がするのだが
        * できない場合も多いな
      * 開発中
    * 短所
      *~~いまいち~~かなり信用出来ない
        * jad はうまく行かなかったところに byte code っぽいものを残すのだが、JD は適当にエラーのないコードを出力してるような
      * CUI がない

> jad で最初逆コンパイルしておかしいところを JD で補完していくのがいいと思う

# How To #

> 面倒になるのが同じ文字で大文字、小文字のクラス名に難読化されている場合、ファイルシステムが対応してないとか、Eclipse が判別してくれないとかあるのでそれを解除する。

> あとメソッドのオーバーロードのバインディングミスも発見しづらいので最初に回避するようにしておく

## Anti Obfuscation ##

> 難読化を解除するために難読化ツールを使用する。

  * [ProGuard](http://proguard.sourceforge.net/)
    * method overload を積極的にしているのを解除する (-useuniqueclassmembernames)
    * クラス名に同じ文字の大文字、小文字を許容するも解除 (-dontusemixedcaseclassnames)
    * 難読化されていないパッケージ、クラス名は保存しておく (-keeppackagenames, -keepnames)

```
-dontshrink
-dontoptimize
-useuniqueclassmembernames
-dontusemixedcaseclassnames
-dontpreverify
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keeppackagenames your.packages.**

-keepnames class your.packages.**
```

## Decompile ##

  * jad
    * オーバーロードのバインドミスを無くすために -safe オプションを付ける
      * 難読化されていない場合はしなくても多分大丈夫
    * あとは好みのフォーマットオプション
      * TODO -noctor

```
 $ find . -name \*.class -exec jad -s .java -safe -space -r -nonlb -ff {} \;
```

## Hand Completion ##

### for ###

  * パターンでわかる


```
        i = 0;
          goto _L1
_L3:

         :

        i++;
_L1:
        if (i < 10) goto _L3; else goto _L2
_L2:

```

```

    for (i = 0; i < 10; i++) {
        :
    }
```

### for (iterable) ###
TODO
### while ###
TODO
### else if ###
TODO
### break/continue ###

TODO

### exception ###

  * 何となく分かる、try がどこに入るのかがいまいちよくわからない

### finally ###

  * exception 処理っぽいところで同じコードが何回も出てきたらそれは finally 節

```
        break MISSING_BLOCK_LABEL_43;
        Exception exception;
        exception;
        a.c();
        throw exception;
        a.c();
        return;
```

```
        } catch (Exception exception) {
        } finally {
            a.c();
        }
```

### switch ###

  * 人力ではキツイので JD に頼る

```
        a;
        JVM INSTR tableswitch -1 6: default 523
    //                   -1 523
    //                   0 60
    //                   1 200
    //                   2 523
    //                   3 499
    //                   4 499
    //                   5 507
    //                   6 515;
           goto _L1 _L1 _L2 _L3 _L1 _L4 _L4 _L5 _L6
_L2:
           :
```

手動でやる場合はこういう感じ

```
        switch (a) {
//        JVM INSTR tableswitch -1 6: default 523     _L1
    //                   -1 523                       _L1
    //                   0 60                         _L2
    //                   1 200                        _L3
    //                   2 523                        _L1
    //                   3 499                        _L4
    //                   4 499                        _L4
    //                   5 507                        _L5
    //                   6 515;                       _L6
//           goto _L1 _L1 _L2 _L3 _L1 _L4 _L4 _L5 _L6
//_L1:
        default:
        case -1:
        case 0:
        case 2:
    :

//_L2:
        case 0:
    :

//_L3:
        case 1:
    :

//_L4:
        case 3:
        case 4:
    :

//_L5:
        case 5:
    :

//_L6:
        case 6:
    :
```

  * goto 以降を default から順に当てはめていく
  * switch の後が default 中に置かれてる事多し
  * break が continue になってたり
  * case 値の JVM code の行番号も参考に
    * switch 文より前にある場合とか発狂しそう

### synchronized ###

  * パターンでわかる

```
        Object obj = foo;
        JVM INSTR monitorenter ;
          :
        obj;
        JVM INSTR monitorexit ;
        throw ;
```

```
    synchronized(foo) {
        :
    }
```

### inner class ###

  * コンストラクタで super の場所が間違ってるので直す。
  * 親クラス参照を消す
  * コンパイラが作成したと思われる static メソッドをインライン化する (access$# ってやつね)


```
    
        foo.addActionListener(new g(this));

      :

    static void a(BaseClass a, String s) {
        System.out.println(s);
    }

    class g
        implements ActionListener {

        final BaseClass a;

        public void actionPerformed(ActionEvent actionevent) {
            BaseClass.a(a, "");
        }

            g() {
                a = BaseClass.this;
                super();
            }
    }

```

```
        foo.addActionListener(new g());

      :

    class g
        implements ActionListener {

        public void actionPerformed(ActionEvent actionevent) {
            System.out.println(s);
        }

            g() {
                super();
            }
    }
```

さらに進めて

```
        foo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionevent) {
                System.out.println("");
            }
        });
```

### class ###

  * 表記が変なので直す

```
        a = (new Class[] {
            0,
            java/lang/String,
            java/lang/Integer,
            [Ljava/lang/Object;,
            java/lang/Object,
            [I,
            [D,
        });
```

```
        a = (new Class[] {
            null,
            java.lang.String.class,
            java.lang.Integer.class,
            java.lang.Object[].class,
            java.lang.Object.class,
            int[].class,
            double[].class,
        });
```

### String ###

  * StringBuilder、StringBuffer を消す

```
  System.err.println((new StringBuilder("value: ")).append(value).toString());
```

```
  System.err.println("value: " + value);
```

### local variables created by compiler ###

  * 見やすくしたり、消したり
  * while の条件式中のやつは注意

```
 TODO sample
```

### double/float literal ###

  * 人が書いた数値に戻す
  * Math.PI 等

```
    double d1 = 0.29999999999999999D;
```

```
    double d1 = 0.3d;
```


### constants ###

  * すべてコードに埋め込まれてしまうのでもとに戻す
    * TODO eclispe のリファクタ機能で何とかならないのか？
    * TODO android の R.java とか

### enum ###

  * static {} の中身に注目
  * vaues(), valueOf() を削除
  * コンストラクタの第 1, 2 引数と super の削除
  * enum の引数関連は残す
  * あと他の場所では static フィールドに設定されているフィールド名が使用されているため~~分かるように残して~~それを残して後でリネーム出来るようにしておく

```
    public static final class Foo extends Enum {

        private static final Foo lM[];
        public static final Foo lN;
        public static final Foo lO;
        public static final Foo lP;
        byte lQ;

        public static Foo valueOf(String s) {
            return (Foo)Enum.valueOf(foo/bar/Buz$Foo, s);
        }

        public static Foo[] values() {
            Foo afoo[] = lM;
            int i = afoo.length;
            Foo afoo1[] = new Foo[i];
            System.arraycopy(((Object) (afoo)), 0, ((Object) (afoo1)), 0, i);
            return afoo1;
        }

        public byte cR() {
            return lQ;
        }

        static  {
            lO = new Foo("AAA", 0, (byte)0);
            lN = new Foo("BBB", 1, (byte)1);
            lP = new Foo("CCC", 2, (byte)3);
            Foo afoo[] = new Foo[3];
            afoo[0] = lO;
            afoo[1] = lN;
            afoo[2] = lP;
            lM = afoo;
        }

        private Foo(String s, int i, byte byte0) {
            super(s, i);
            lQ = byte0;
        }
    }

```

NG
```
    public static enum Foo {

        AAA(0), // lO
        BBB(1), // lN
        CCC(2); // lP

        byte lQ;

        public byte cR() {
            return lQ;
        }

        private Foo(byte byte0) {
            lQ = byte0;
        }
    }
```
BETTER
```
    public static enum Foo {

        lO(0), // AAA
        lN(1), // BBB
        lP(2); // CCC

        byte lQ;

        public byte cR() {
            return lQ;
        }

        private Foo(byte byte0) {
            lQ = byte0;
        }
    }
```

### assert ###

```
    package buz.bar;
    class Foo {

       static final boolean $assertionsDisabled;

          :

          if (!$assertionsDisabled && foo == 0)
              throw new AssertionError("assertion message");

          :

        static  {
            boolean flag;
            if (!buz.bar.Foo.class.desiredAssertionStatus())
                flag = true;
            else
                flag = false;
            $assertionsDisabled = flag;
        }
```

```
          assert foo != 0 : "assertion message";
```

### annotation ###

```
import java.lang.annotation.Annotation;

public interface UiThread
    extends Annotation {

    public abstract boolean value();
}
```

```
public @interface UiThread {
}
```

# Others #


Eclipse のリファクタリング機能がこれほど活躍できる場面はないと思う。

たまに Javadoc だけ公開されている場合がある。その場合は [Codavaj](http://codavaj.sourceforge.net/) を使用すればコピペで楽にコメントが付けられる。おまけに正しい引数名が得られるので、そこから芋づる式に内部の解析が進むこともある。

慣れてくると、ああこれ jad のソースだとわかるようになる。例えば [jflash](http://java.net/projects/jflash/sources/svn/content/trunk/prj/jflash/src/org/jflash/DisplayList.java?rev=38) の 508 行目とか [local variables created by compiler](https://code.google.com/p/umjammer/wiki/JavaReverseEngineering?ts=1388518694&updated=JavaReverseEngineering#local_variables_created_by_compiler) に相当するのがわかる。それ以前に変数名でわかっちゃうけどね。よくもまあ抜け抜けとリバエンしたやつをメジャーサイトに公開するよなぁ？

# ~~Todo~~ #

## やってしまったソース ##
オーバーロードでミスる事を知らなかったときにリバエンした 50000 行くらいのコードがあるのだが、もちろんエラーは出ないし、なんとちゃんと動いてしまってるので、どこでミスってるのかが簡単に検出できない。結果のデータが違うので明らかにミスっているのは分かるのだが、~~どうしたものか...~~

  * ~~call graph 作成とか？~~ → [作った](http://erik.doernenburg.com/2008/09/call-graph-visualisation-with-aspectj-and-dot/)けど、はっきり言って比較するのムリ

### 見つけた ###

オーバーロードのミスより、名前の優先順位が定数より変数が勝つみたいなのでそこがミスっていた。null と比較とかだとエラーにならない場合が出てくる。

```
   class Foo {
      Bar a;
   }

   class a {
      public static Foo a;
   }

   class b {
      Foo a;
      
      ... {
         if (a.a != null) { ... // compiler recognizes class b's field a (class Foo) 's field a,
                                     // but real source was class a's static field a
      }
   }
```

まあ [Anti Obfuscation](http://code.google.com/p/umjammer/wiki/JavaReverseEngineering?ts=1316976443&updated=JavaReverseEngineering#Anti_Obfuscation) しておけばいらない苦労だったんだけどね。

## 辞書の活用 ##

```
-packageobfuscationdictionary dic_file
```

## オープンソースが難読化されて組み込まれている場合 ##

どうしよう？

  * API を使用してるのはそんなに多くないと仮定して、使用している方からリバエン
    * すべてやる必要はない
  * スケルトンを作る (jad -nocode)

## GPL/LGPL のライブラリが組み込まれている場合 ##

リバースエンジニアリングの権利があるのでじゃんじゃんリバエンして公開しようｗ