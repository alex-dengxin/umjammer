# BD-J #

System Software Update v2.50 以降 AVCHD がディスク以外から？再生されなくなりました。よって終了〜

  * [MP3 再生のソース](http://code.google.com/p/umjammer/source/browse/trunk/vavi-sound-bdj)まで書いたのに...

## PS3 ##

  * ADA
    * 1016832 bytes
## Info ##

### javax.tv ###

  * codec

|type|codec|
|:---|:----|
|audio|ima4, |
|video|cinepak|

  * content

|type|codec|
|:---|:----|
|video|quicktime|

  * protocol

|type|
|:---|
|file|
|http|

## Develop ##

  * [hdcookbook](https://hdcookbook.dev.java.net/)
  * bdj-ps3
  * [xletview](http://xletview.sourceforge.net/)

  * off screen image は Component#createImage を使う
    * new BufferedImage() はダメっぽい
  * Conponent#getToolkit() が NoSuchMethodError
    * xletview ~~だけ？？？~~ → xletview のバグ
  * GZipInputStream でエラーが起きる
    * on PS3
  * String のメソッドのバージョン
    * replace → 自作
    * split → StringTalknizer

  * お約束の LodeRunner

![http://lh5.ggpht.com/_JchUHfE3WF4/SnAFa-21XII/AAAAAAAAAGU/eepnqPx_7vk/s288/BD-J.2008082323530000.png](http://lh5.ggpht.com/_JchUHfE3WF4/SnAFa-21XII/AAAAAAAAAGU/eepnqPx_7vk/s288/BD-J.2008082323530000.png)

  * [Apple II Emulator](http://code.google.com/p/umjammer/source/browse/trunk/vavi-games-appleii-bdj) - [My Blog](http://vavivavi.blogspot.com/2008/09/bd-j-appleii-emulator-bd-j-nes-apple-ii.html) [PS3 NEWS](http://www.ps3news.com/forums/playstation-3-news/playstation-3-bd-j-apple-ii-emulator-released-100986.html)
  * [2ch Browser](http://code.google.com/p/umjammer/source/browse/trunk/vavi-apps-mona-bdj) - [My Blog](http://vavivavi.blogspot.com/2008/09/blog-ps3-homebrew-bd-j-2ch-ps3-bd-j-2ch.html)
  * [Tetris](http://code.google.com/p/umjammer/source/browse/trunk/vavi-games-tetris-bdj)

### xletview ###

  * bugs
    * RandomAccessFile
    * constructor(File,String) not such method
    * user.dir not set like FileInputStream
    * javassist によって xjava.`*` に置換されるクラスのうち自クラスの型もしくは配列を返すメソッドがダメになる
      * File#listFiles() x3
      * Toolkit#getToolkit()

  * differences
    * ColorModel
      * PS3 ではαが効いてる
      * xletview はαなくても OK な場合がある -> BufferedImage#getRGB/setRGB

## Idea ##

  * Virtual Keyboard
    * via network
    * PBP has ServerSocket
    * http://synergy2.sourceforge.net/