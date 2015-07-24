# Language Comparison #


## LodeRunner ##

|**platform**|**version**|**result**|**comment**|
|:-----------|:----------|:---------|:----------|
|Assembler   |i286 for PC-9801|●         |オリジナルをディスアセンブルしたもの|
|C           |PC-9801 MS-DOS MSVC|●         |Assembler コードからハンドディスコンパイルしたもの|
|            |           |          |           |
|[Java SE](http://hccweb1.bai.ne.jp/~hcj64001/umjammer/lr/index.html)|6  w/ [JApplet](http://umjammer.googlecode.com/svn/trunk/x-lr/index.html)|●         |C から移植、以下のリファレンス|
|[Android](android.md)|1.0 [r1](https://code.google.com/p/umjammer/source/detail?r=1)|●         |~~repaint copyarea の最適化~~|
|[DOJA](android.md)|5.1        |○         |実機で動かん...  |
|[Java ME](http://java.sun.com/javame/index.jsp)|WTK2.5.2   |○         |イメージのスケーリングができない|
|C# (WinForms)|3.5        |○         |repaint copyarea がダメダメ|
|C# (WinForms)|[mono](http://www.mono-project.com/Main_Page) 2.0.1 (Mac)|○         |           |
|C# (WPF/Silverlight?)|b1?        |×         |           |
|C# (moonlight)|           |          |WPF相当は無い   |
|C# (XNA)    |2.0        |△         |offscreen  |
|C# ([mono.XNA](http://code.google.com/p/monoxna/))|           |          |バージョンが古い   |
|Java (CLI)  |[IKVM](http://www.ikvm.net/) 0.36.0.11|×         |イメージ読み込みで落ちる、JNI が無理？|
|C# (JVM)    |[JaCIL](http://www.cs.rit.edu/~atg2335/project/) 0.7.0.0|          |use ikvm   |
|BD-J (Emulator)|[xletview](http://www.xletview.org/)|●         |           |
|BD-J ([PS3](http://www.jp.playstation.com/ps3/))|< FW v2.50 |●         |結構簡単に動いた！  |
|[lwuit](https://lwuit.dev.java.net/)|           |          |いったい幾つ UI 作ればいいんだ...WORA は？|
|[STAR](http://www.nttdocomo.co.jp/service/imode/make/content/iappli/technical_data/star/index.html)|           |○         |実機が無い...   |
|Java (iPhone)|[jocstrap](http://svn.saurik.com/repos/menes/trunk/jocstrap/README)|          |           |
|Java (iPhone)|[xmlvm](http://xmlvm.org/overview/)|          |           |
|Java (SWT)  |SWT 1.2    |●         |           |
|Java (SDL)  |[jsdl](http://jsdl.sourceforge.net/)|          |           |
|Java (SDL)  |[sdljava](http://sdljava.sourceforge.net/)|○         |           |
|Java (Character)|[Charva](http://www.pitman.co.za/projects/charva/index.html)|          |           |
|Java (KDK)  |[KDK](http://kdk-javadocs.s3.amazonaws.com/index.html)|△         |？ [なんで作れるの？](http://lab.klab.org/young/2010/03/kindle-development-kit-kdk-%E3%82%A2%E3%83%97%E3%83%AA%E4%B8%80%E7%95%AA%E4%B9%97%E3%82%8A%E3%82%92%E7%9B%AE%E6%8C%87%E3%81%99%EF%BC%81/)|
|            |           |          |           |
|ActionScript ([FLEX](http://www.adobe.com/products/flex/flexdownloads/)/[AIR](http://www.adobe.com/jp/products/air/tools/sdk/))|3.0        |×         |Thread モデルを Event モデルに変更する必要が...|
|[JavaFX](https://openjfx.dev.java.net/ja/)|1.0        |×         |           |
|[Python](http://www.python.org/)|[Jython](http://www.jython.org/) 2.1 w/ awt|●         |           |
|[Scala](http://www.scala-lang.org/)|2.7.1 w/ awt|●         |           |
|[Ruby](http://www.ruby-lang.org/ja/)|[JRuby](http://www.jruby.org/) 1.1.5 w/ awt|●         |           |
|JavaScript  |[Rhino](https://developer.mozilla.org/ja/Rhino) 1.7R1 w/ awt|×         |jsc で Java クラスを継承するとか反則気味|
|JavaScript  |`<canvas/>`|          |           |
|[Groovy](http://groovy.codehaus.org/)|1.5.7      |●         |           |
|Lisp        |[Clojure](http://clojure.org/) w/ awt|          |           |
|C++ (SDL)   |g++ SDL    |●         |           |
|C++ (iPhone SDL)|SDL 1.3    |○         |スケーリング     |
|C++ (iPhone UIKit)|           |          |           |
|C++ (cocos2d)|[cocos2d](http://code.google.com/p/cocos2d-iphone/) |          |           |
|C++ (Cocoa) |           |          |           |
|PHP         |[Quercus](http://quercus.caucho.com/) w/ awt |          |           |
|Perl        |[JPerl](http://www.javainc.com/projects/jperl/) w/ awt |          |           |
|Lua         |[LuaJ](https://sourceforge.net/projects/luaj/) w/ awt |          |           |

## Comparison ##

### ActionScript ###

  * javascript で汎用言語を作りました？
  * version 3.1

| |Java|actionscript|
|:|:---|:-----------|
|Constructor|`class Foo` → `Foo()`| `class Foo` → `function Foo()`|
|2D array|`new foo[x][y];`|`new Array(x); for(...) array[x] = new Array(y);`|
| |`System.currentTimeMills()`|`new Date().time`|
| |`byte[]`|`flash.utils.ByteArray`|

  * IO (AIR のみの気がする)
```
 var file:FileStream = new FileStream();
 file.open(new File(mapName), FileMode.READ);
 file.position = level * 0x200;
 file.readBytes(buf, 0, 0x200);
 file.close();

 var file:FileStream = new FileStream();
 file.open(new File(mapName), FileMode.WRITE);
 file.position = level * 0x200;
 file.writeBytes(buf, 0, 0x200);
 file.close();
```
  * IDE
    * Eclipse Plugin ... 期限終了
    * [axdt](http://axdt.org/)

### JavaScript ###

  * one of ll
  * TODO
    * `<canvas/>`

| |Java|JavaScript|
|:|:---|:---------|
| |`class C {}`|`function C() {}`|
| |`int foo(int bar) {}`|`C.prototype.foo = function(bar) {}`|


### JavaFX ###

  * 悪いけど方向性が見えない、~~言語としての魅力もわからない~~
    * Blu-ray オーサリングに BD-J じゃなくてこっちが採用だったらまだ話はわかったけど
    * 正式リリースで全然文法変わってるんですけど
  * version 1.0
  * [参考](http://gihyo.jp/dev/serial/01/javafx/0003?page=1)

| |Java|JavaFX|
|:|:---|:-----|
|interface|`interface I`|`abstract class I`|
| |`%` |` mod`|
|bitwise|`& | ~` ...| `javafx.util.Bits.xxx()`|
| |`System.err.println("foo: " + bar);`|`println("foo: {bar}");`|

  * IDE
    * Eclipse Plugin ... 言語のアップデートについていっていない
    * [NetBeans](http://www.netbeans.org/features/javafx/index.html) ... イマイチだなぁ

### Groovy ###

  * better ruby???
    * を目指してるようだが、Java に縛られて自由になりきれてない感あり
    * JSR になって[文法がかなり変わってる](http://docs.codehaus.org/display/GROOVY/Japanese+Migration+From+Classic+to+JSR+syntax)ので、ネットのサンプルがかなり当てにならない
  * Java の文法そのままでも動くみたい
    * あえて Ruby から移植してみた
    * メソッドのオーバーロードの推論が引数 Object に対してのみなので他の型は要指定
  * ファイル名と違うクラスがインタプリタから参照されない？？？
    * コンパイルすれば OK っぽい

| |Java|Groovy|
|:|:---|:-----|
|Method|`int foo(int bar) { ... }`|`int foo(bar) { ... }` |
|定数|`static final int foo = 10`|`def foo = 10`|
|クラス変数|`static int foo`|???   |
| |`for (int i = 0; i < 10; i++)` |      `for (i in 0 .. 10)` |
| |`for (int i = 0; i < 10; i++)` |      `for (i in 0 ..< 9)` |
| |`for (int i = 9; i >= 0; i--)` |      ~~`9.downto(0)`~~ `for (i = 9; i >= 0; i--)`|
| |`for (int i = 0; i < 10; i++)` |      `10.times` (`break` できない)|
| |`string.substring(3)`|`string[3 .. -1]`|
| |`new String(new char[] { 'a' })`| `a.bytes[0]`|
| |`charValue = 'a'`|`charValue = 'a' as char`|
| |`Math.max(a，b) Math.min(a，b)`|`[a，b].max() [a，b].min()`|
| |`System.err.println("foo" + bar)`|`println "foo $bar"`|
| |`new Foo(bar，buz)`|`[bar，buz] as Foo`|
|integer division|`i = 9 / 5`|`i = 9.intdiv(5)`|
|main|    |`   static void main(args) { ... }`|
|awt|    |`Component#graphics` のオーバーライドに注意|

  * IDE
    * [Eclipse Plugin](http://groovy.codehaus.org/Eclipse+Plugin)

### C# ###

  * Java + Delphi のこだわり
    * partial なじめない
    * メソッド名の最初大文字許せない
  * version MS 3.5, mono 2.01

| |Java|C#|
|:|:---|:-|
|Ticks|`System.currentTimeMills()`|`DateTime.Now.Ticks // nano sec`|
|Syntax|    |`switch` が fall down しない → `goto case` Label|
|2D array|`array = new foo[x][y]`|`array = new foo[x][]; for (...) array[i] = new foo[y];`|
|IO|    |`using (...)` `*`1|
|Thread Switching|`Thread.yield()`|`Thread.Sleep(0) // milli sec`|
|Library Dependency|`-cp foo.jar`|`*`2 `App.exe.Config`|
| |`System.out.println("foo:" + bar)`|`Console.WriteLine("foo {0}"，bar.ToString())`|
|Comment|`/** ... */`|` ///`|

  1. IO というよりリソースクロージングのための構文糖衣みたい
  1. ([ここ](http://support.microsoft.com/kb/837908/ja) に書いてあるがまさに「日本語でOK」状態...)
```
 <?xml version="1.0" encoding="utf-8"?>
 <configuration>
  <runtime>
    <assemblyBinding xmlns="urn:schemas-microsoft-com:asm.v1">
      <dependentAssembly>
        <assemblyIdentity name="IKVM.OpenJDK.ClassLibrary"
                          publicKeyToken="13235d27fcbfff58"
                          culture="neutral" />
        <codeBase version="0.36.0.5"
                  href="file:///c:/usr/local/ikvm/bin/IKVM.OpenJDK.ClassLibrary.dll" />
      </dependentAssembly>
      <dependentAssembly>
        <assemblyIdentity name="IKVM.Runtime"
                          publicKeyToken="13235d27fcbfff58"
                          culture="neutral" />
        <codeBase version="0.36.0.5"
                  href="file:///c:/usr/local/ikvm/bin/IKVM.Runtime.dll" />
      </dependentAssembly>
         :
```
  * IDE
    * [emonic](http://emonic.sourceforge.net/) ... 重い、よくハングする

### Python ###

  * 軽いけど高機能
  * version 2.5
  * v3.0 で大幅なアップデートが入るらしい
    * self が無くなれば結構完成形かも(デコレータとかで必要なので無くならない？)
    * ライブラリはもう少し整理して欲しいか
  * TODO
    * デコレータ
    * ダックタイピング

| |Java|Python|
|:|:---|:-----|
|package|    |`＿init＿.py` を各ディレクトリに置く|
| |`true`|`True`|
| |`false`|`False`|
| |`null`|`None`|
| |` && ` `|``|` ` !`| `and or not`|
| |` ++ --`|無い    |
| |`switch ... case ...`|無い    |
|Method|`int foo(int bar) { ... }`|`def foo(self，bar):`|
| |`if (C1) ... else if (C2) ... else ...`| `if C1: ... elif C2: ... else: ...`|
| |`C ? A : B`| `A if C else B`|
|interface|    |空メソッドのclassで代用|
|継承|`class A extends B implements C`|`class A(B，C)` 多重継承あり|
|定数|    |めんどくさいので放置|
|フィールド|    |プログラムの実行順で参照される前に代入する必要がある|
|メソッド|    |`self.method()`|
|Constractor|    |`def ＿init＿(self):`|
| |`byte[] a = new byte[3];`|`a = [0] * 3`|
| |`byte[][] a = new byte[3][4]`|`a = [[ 0 ] * 4] * 3`|
| |`byte[] a =｛ 0，1，2 ｝`|` a = [0，1，2]`|
| |`for (int i = 0; i < 10; i++)`|`        for i in range(10):`|
| |`for (int i = 9; i >= 0; i--)`|`        for i in range(9，-1，-1):`|
| |`string = string + integer`| `string = string + str(integer)`|
| |`integer = Integer.parseInt(string)`| `integer = int(string)` |
| |`charValue = 'a'`|`charValue = ord('a')`|
| |`string.charAt(0)`|`ord(string[0])`|
| |`new String(new char[] { 'a' })`|`chr('a')`|
| |`System.currentTimeMills()`|`time.time()`|
| |`Thread.sleep(10)`|`time.sleep(0.01)`|
|長さ|    |`len(any_sequence)`|
|文字列操作|    |`any_sequence[x:y]`|
|main|    |`if ＿name＿ == "＿main＿":`|
| |`main(String[] args)`|`sys.argv[n]`|
| |`System.out.println("foo: " + bar)`|`print("foo: %d" % (bar))`|
|Comment| `/** ... */`| `""" ... """`|
|Comment| `//`| `#`  |
|awt|    |`Component#graphics` のオーバーライドに注意|

  * バイナリ IO
```
 file = open("/foo/path/", "rb")
 file.seek(0x200)
 arr = array('b', file.read(0x200))
 file.close()
```
  * 例外処理
```
 try:
   ...
 except:
   ...
```
  * IDE
    * [Pydev](http://pydev.sourceforge.net/) ... なかなかよろしい、もうちょっと機能増やそう

### Scala ###

  * 期待の関数型言語
    * ちょっと手続き型に引っ張られ過ぎ感も否めない
  * version 2.7.2

| |Java|Scala|
|:|:---|:----|
|interface|    |`trait`|
|継承|`class C extends A, B implements I, J`|`class C extends A with B with I with J`|
| |`final`|`val`|
| |`int` |`Int`|
| |`byte`|`Byte`|
| |`char` |`Char`|
| |`byteValue = (byte) integerValue`| `byteValue = integerValue.asInstanceOf[Byte]`|
|keyword重複|無し  |`Thread.```````yield```````()`|
|Method|`int foo(int bar) { ... }`|` def foo(bar:Int):Int { ... }`|
|型指定宣言|`byte b = 0`|`var b:Byte = 0`|
|配列|`array[index]`| `array(index)`|
|フィールド|    |宣言をすべて書く ソース上で使用するより前に書く|
| |`++ --`|無し   |
| |`switch case C: break;`|`match case C =>`|
| |`for (int i = 0; i < 10; i++)`| `for (i <- 0 until 10)`|
| |`for (int i = 9; i >= 0; i--)`| `for (i <- 9 to 0 by -1)`|
| |`for (int i = 0; i <= 10; i++)`| `for (i <- 0 to 10)`|
| |`break/continue`|無し`*`1|
| |`C ? T : F`|`if (C) T else F`|
|例外処理|    |`*`2 |
|Constructor|    |`def this(foo:Foo)`|
|array|`byte[][] a = new byte[3][4]`|`a:Array[Array[Byte]] = new Array(3); for (i <- 0 until 3) a[i] = new Array(4);`|
| |`byte[] a = new byte[0x200]`|`var a = new Array[Byte](0x200)`|
| |`byte[] a = { 1，2，3 }`|    `val a:Array[Int] = Array(1，2，3)`|
| |`System.err.println("foo: " + bar)`|`printf("foo:｛0｝"，bar)`|
|main|    |`*`3 |

  1. break, continue の代わり(ホントはちゃんと関数言語らしく書くべき)
```
 class Continue extends Exception {
 }
 var LOOP = true
 while (LOOP) {
   try {
     :
     // break
     LOOP = false;
     throw new Continue;
 
     :
     // continue
     throw new Continue;
 
     :
   } catch {
     case Continue =>
   }
 }
```
  1. 例外処理
```
 try {
 
 } catch {
   case e:Exception =>
 }
```
  1. main
```
 object Main {
   def main(args: Array[String]) {
   }
 }
```
  * IDE
    * [Scala Eclipse Plugin](http://www.scala-lang.org/node/94) ... たまにエラーでハングする

### Ruby ###

  * better perl
    * どんな書き方でも結構動くから初心者が移植するとバグを見つけにくい
  * version 1.8
  * v1.9 で早くなるらしい

| |Java|Ruby|
|:|:---|:---|
|package|    |??? |
| |`interface`|~~`module` + 空メソッドで代用~~ 消す方がいいかも|
| |`class A extends B`|`class A < B`|
| |`class A implements B`|` class A include B` (`B`は`Module`でMix-in)|
|Method|`int foo(int bar) { ... }`|`def foo(bar) ... end`|
|定数|`static final int foo = 10`|`Foo = 10 # 頭文字が大文字！`|
|インスタンス変数|`int foo`|`@foo`|
|クラス変数|`static int foo`|`@@foo`|
| |`++ --`|無し  |
| |`if (C1) ... else if (C2) ... else ...`|`if C1 then ... elsif C2 then ... else ... end`|
| |`switch case A ... break; ... default: ... break;`|`case when A ... else ... end`|
| |`for (int i = 0; i < 10; i++)` |      `for i in 0 ... 10 do ... end`|
| |`for (int i = 0; i < 10; i++)` |     `for i in 0 .. 9 do ... end`|
| |`for (int i = 9; i >= 0; i--)` |      `for 9.downto(0) do |i| ... end`|
| |`for (int i = 0; i < 10; i++)` |      `10.times do ... end`|
| |`while (true)`|`loop do`|
| |`continue`|`next`|
| |`string = string + integer`| `string = string + integer.to_s`|
| |`integer = Integer.parseInt(string)`|`integer = string.to_i`|
| |`string.substring(3)`|`string[3 .. -1]`|
| |`new String(new char[] { 'a' })`| `a.chr`|
| |`charValue = 'a'`|`charValue = ?a`|
| |`byte[] a = new byte[3]`|`a = Array.new(0x200)`|
| |`byte[][] a = new byte[3][4]`|`a = Array.new(3) for i in 0 .. 2 do a[i] = Array.new(4) end`|
| |`byte[] a = { 1，2，3 }`|`a = [1，2，3]`|
| |`Math.max(a，b) Math.min(a，b)`|`[a，b].max [a，b].min`|
| |`Thread.sleep(10)`|`sleep(0.01)`|
| |`System.currentTimeMills()`|`Time.new`|
| |`System.err.println("foo: " + bar)`|`puts "foo: #{bar}`"|

  * 同じディレクトリのファイルの取り込み
```
 $: << File.dirname(__FILE__)
 require "FileName"
```
  * Constructor
    * Java
```
 class A {
   int a;
   A(int a) {
     this.a = a;
   }
 }
```
    * Ruby
```
 class A
   attr :a
   def initialize(a)
     @a = a
   end
 end
```
  * 例外処理
    * Java
```
 try {
   ...
 } catch (E e) {
   ...
 } finally {
   ...
 }
```
    * Ruby
```
 begin
   ...
 rescue E => e
   ...
 ensure
   ...
 end
```
  * IDE
    * [RDT](http://update.aptana.com/update/rdt/3.2/) ... たまに重くなる
    * ruby-debug-ide がデフォルトではうまくインストールされないので以下を実行
```
 $ jruby -S gem install jruby-openssl
 $ curl 'http://rubyforge.org/frs/download.php/47154/ruby-debug-base-z.y.z-java.gem'
 $ jruby -S gem install ruby-debug-base-x.y.z-java.gem
 $ jruby -S gem install ruby-debug
 $ jruby -S gem install ruby-debug-ide
```
  * loop という関数を定義していてはまった...orz
    * 移植時には先言語のキーワードに注意することを忘れないようにしないと
```
 def loop
   ...
 end
 
 loop do # 定義されたメソッドがコールされる...orz
   ...
 end
```
  * C からの移植で宣言をそのままにしない
    * C
```
 int a = 0, int b = 0, int c = 0
```
    * Ruby
```
 a = 0, b = 0, c = 0 # 意図したものと違う評価
```

### Clojure ###

  * IDE
    * [enclojure](http://enclojure.org/)