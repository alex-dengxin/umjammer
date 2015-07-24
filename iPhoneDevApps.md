# Develop Applications #

  * すべて cygwin の [touch dev toolchain](iPod.md)
  * システムライブラリは自分の touch のイメージ Snowbird
  * たくさんあるがインストールマニアではない、すべて Java と gnash のための布石である。(quasi88 はオマケだけどね;-P)
  * after `llvm-gcc install`
```
 mv /usr/local/lib/{libstd++*,libiberty*} /usr/local/arm-apple-darwin/lib
```

## Applications ##

  * `undefined ___eprintf` は assert.h の `assert()` が GNU C の場合それに置換されオブジェクトが無い？？？ため
  * `--prefix=/usr/local/arm-apple-darwin` すると実機上でもそうなってしまうんだが、そうしないとクロス環境でコンフリクトするしどうすりゃいいんだ？？？
    * 実機からソースフォルダをマウントして make install を実機上で行うってのは邪道？？？

### [HelloApplication](http://iphone.fiveforty.net/wiki/index.php/UIKit_Hello_World) ###
開発環境構築に苦労したわりにはあっさり動きやがった...
[iPod touch](http://picasaweb.google.co.jp/lh/photo/Gz0Sdjz65_OtT7qO_fhipg?feat=directlink)

### [lv](http://www.ff.iij4u.or.jp/~nrt/lv/) ###
  * version 4.51
  * CC を arm-apple-darwon-gcc に変更
  * `-ltermcap` を `-lncurses` に変更
  * 動作確認

  * [lv (arm-apple-darwin,sjis default)](http://hccweb1.bai.ne.jp/~hcj64001/umjammer/files/lv)
### [nkf (utf8)](http://www01.tcp-ip.or.jp/~furukawa/nkf_utf8/) ###
  * version 2.0
  * CC を arm-apple-darwon-gcc に変更
  * 動作確認

  * [nkf (arm-apple-darwin,sjis default)](http://hccweb1.bai.ne.jp/~hcj64001/umjammer/files/nkf)
### [jamvm](http://jamvm.sourceforge.net/) ###

  * version 1.5.0, 1.5.1
  * 動作確認！
  * jamvm の boot class コピーし忘れたら gnu classpath のやつ使いやがって、はまった...
  * SwingSet2 を動かすのが最終目標

  * TODO [llvm](http://llvm.org/) 使いたいなぁ
  * cpu [ARM1176JZF-S 互換](http://japanese.engadget.com/2007/07/01/iphone-arm11-cpu-600-mhz/)のはずだから[jazelle](http://www.jp.arm.com/products/multimedia/index.html) も使えるんだよね？使いてぇ！arm さん SDK タダで頂戴よ？。ついでに vm もね☆
  * シャレにならない遅さ...orz クラスロードがヤバい、このままじゃ実用にならない(涙目)
    * zlib 使ってるはず？なのに
    * 浮動小数点系？ arm 難い...
  * [android](http://code.google.com/android/) の dalvik 今の[ターゲット](http://www.cdmatech.com/products/msm7200_chipset_solution.jsp) [arm](http://d.hatena.ne.jp/eggman/20071108/1194556626) やんけ！ elf を march-o に変換できりゃ iTouch で動くんか？？？
    * ~~Jazelle 対応してるんかなぁ？~~
    * ~~↑してたらすげぇよなぁ、オープンソースで Jazelle 手に入るぞ！~~
    * ~~JBlend とか涙目？？？~~
```
 $ cp -rp src/os/bsd/arm src/os/darwin/ # 1.5.1 から要らない
 $ vi src/os/darwin/arm/callNative.S
 :28d
 :1,$s/callJNIMethod/_callJNIMethod/
 :wq
 $ vi configure
 add arm-apple-darwin
 add src/os/darwin/i386/Makefile

 $ export CFLAGS=-D__ARM_EABI__
 $ export LDFLAGS="-L$SNOWBIRD/usr/lib"
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --with-classpath-install-dir=/usr/local/arm-apple-darwin/classpath
 $ make
 $ make install

 $ scp /usr/local/arm-apple-darwin/bin/jamvm touch:/usr/local/bin/
 $ scp -r /usr/local/arm-apple-darwin/lib/libjvm*.dylib touch:/usr/local/arm-apple-darwin/lib/
 $ scp -r /usr/local/arm-apple-darwin/share/jamvm touch:/usr/local/arm-apple-darwin/share/
```

  * [kaffe](http://www.kaffe.org/) コンパイルしてる[奴](http://www.mail-archive.com/kaffe@kaffe.org/msg12754.html)いる
  * JIT 付きもあるね ... [cacao](http://www.cacaojvm.org/)
  * [libffi](http://sources.redhat.com/libffi/) ... jni のみに影響？
  * -Xmx8M -Xss256K が結構重要 → かなり動くぞこいつ！
### [GNU classpath](http://www.gnu.org/software/classpath/) ###
  * version 0.96.1
  * jamvm の布石
```
 $ export CFLAGS=-DNDEBUG
 $ export LDFLAGS=-L$SNOWBIRD/usr/lib
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --disable-gtk-peer --disable-gconf-peer --disable-plugin --prefix=/usr/local/arm-apple-darwin/classpath --with-vm=/usr/local/bin/jamvm
 $ make
 $ make install

 $ scp -r /usr/local/arm-apple-darwin/classpath touch:/usr/local/arm-apple-darwin/
```
  * ~~がー、 CP932 入ってねぇ... orz~~
### SDL ([iphone-sdl-mame](http://code.google.com/p/iphone-sdl-mame/) の物を使用) ###

  * gnash の布石
  * jsdl の布石
  * quasi88 の布石
```
 $ vi configure.in
 EXTRA_CFLAGS+=-DNDEBUG
 EXTRA_LDFLAGS+="-L$SNOWBIRD/usr/lib -liconv -lm -lobjc -framework CoreSurface -framework CoreFoundation -framework UIKit -framework GraphicsServices-framework CoreAudio -framework AudioToolbox"

 $ export SNOWBIRD=/your/install/path
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
  * audio for iPhone の作成
    * 参考 [NES.app](http://iphone.natetrue.com/nesapp/)
    * 参考 [AudioToolbox](http://variogram.com/latest/?p=25)
    * 071031 音キタ！！！
      * けどバッファリングがうまく行かないのか重いのかぶつぶつだよ(･∀･)
  * thread 関連がうまく行ってなさそうな気がするのだが...

### SDL (iphone-sdl-mame の構造が変わった) ###
```
 $ svn checkout -r 81 http://iphone-sdl-mame.googlecode.com/svn/trunk
 $ export CFLAGS "-DDEPRECATED_IN_MAC_OS_X_VERSION_10_5_AND_LATER= \
                  -DAVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5= \
                  -DAVAILABLE_MAC_OS_X_VERSION_10_5_AND_LATER="
 $ ./configure --host=arm-apple-darwin \
               --prefix=/usr/local/arm-apple-darwin
```
### [libpng](http://www.libpng.org/pub/png/libpng.html) ###
  * SDL\_image の布石
```
 $ export LDFLAGS="-L$SNOWBIRD/usr/lib -L/usr/local/arm-apple-darwin/lib"
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [libjpeg](http://www.ijg.org/) ###

  * SDL\_image の布石
  * shared がうまく作れん...
  * ./libtool を arm-apple-darwin にする
```
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --enable-shared
 $ vi Makefile
 s/gcc/arm-apple-darwin-gcc/
 s/ar/arm-apple-darwin-ar/
 s/ld/arm-apple-darwin-ld/
 s/nm/arm-apple-darwin-nm/
 $ vi libtool
 s/gcc/arm-apple-darwin-gcc/
 s/ar/arm-apple-darwin-ar/
 s/ld/arm-apple-darwin-ld/
 s/nm/arm-apple-darwin-nm/
 $ make
 $ make install-lib
```

### [libtiff](http://www.remotesensing.org/libtiff/) ###
  * SDL\_image の布石
```
 $ export CFLAGS=-DNDEBUG
 $ export LDFLAGS="-L/$SNOWBIRD/usr/lib -L/usr/local/arm-apple-darwin/lib"
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [SDL\_image](http://www.libsdl.org/projects/SDL_image/) ###
  * jsdl の布石
```
 $ export CFLAGS=-DNDEBUG
 $ export LDFLAGS=-L$SNOWBIRD/usr/lib -L/usr/local/arm-apple-darwin/lib -L/usr/local/lib -lSDLMain -framework UIKit
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ ./autogen.sh
 $ make
 $ make install
```
### [freetype2](http://www.freetype.org/) ###

  * DL\_ttf の布石
```
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [SDL\_ttf](http://www.libsdl.org/projects/SDL_ttf/) ###

  * jsdl の布石
```
 $ export LIBS="-lSDLmain"
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --with-freetype-prefix=/usr/local/arm-apple-darwin --with-sdl-prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [SDL\_mixer](http://www.libsdl.org/projects/SDL_mixer/) ###
  * jsdl の布石
```
 $ export LDFLAGS="-L/usr/local/arm-apple-darwin/lib -lstdc++"
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --disable-music-mod --with-sdl-prefix=/usr/local/arm-apple-darwin --with-smpeg-prefix=/usr/local/arm-apple-darwin --disable-music-native-midi
 $ make
 $ make install
```
### [SDL\_gfx](http://www.ferzkopp.net/joomla/content/view/19/14/) ###

  * jsdl の布石
  * shared が作れん...
```
 $ export SDL_CONFIG=/usr/local/arm-apple-darwin/bin/sdl-config
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --disable-mmx
 $ make
 $ make install
```
### [smpeg](http://svn.macports.org/repository/macports/distfiles/smpeg/) ###

  * jdsl の布石
  * ~~本家はよくわからんサイトだ~~ → [こっち](http://icculus.org/smpeg/)が本家やんか！いい加減な情報流しやがって...
  * patch を[ここ](http://svn.rpmforge.net/svn/trunk/rpms/smpeg/)から取ってくる
  * 再生できた！けど音が鳴らん... SDL Audio 動いてるみたいだけど？？？
```
 smpeg-0.4.4-PIC.patch
 smpeg-0.4.4-fixes.patch
 smpeg-0.4.4-gcc32.patch
 smpeg-0.4.4-gcc41.patch
 smpeg-0.4.4-gnu-stack.patch
 smpeg-0.4.4-m4.patch
```
  * macport 用の [patch](http://svn.macports.org/repository/macports/trunk/dports/multimedia/smpeg/files/)
```
 patch-Makefile.in
 patch-glmovie-tile.c
 patch-glmovie.c
 patch-smpeg.h
```
```
 $ export SDL_CONFIG=/usr/local/arm-apple-darwin/bin/sdl-config
 $ export LIBS=-L/usr/local/arm-apple-darwin/lib -lSDL -SDLmain -lstdc++ -lobjc -framework UIKit
 $ patch -p1 < *.patch
 $ patch -p1 < patch*
 $ ./autogen.sh
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make

 $ vi include/smpeg.h
 1,$s@\<SDL@\<SDL/SDL@
 $ make install
```

### [jsdl](http://jsdl.sourceforge.net/) ###
  * sdlawt の布石
  * ぐまー、これ古いのかよ！今は sdljava らしい...
  * makefile を arm-apple-darwin 対応に (x264 参照)
```
 $ vi makefile
  :
 $ vi jni/*.c
 s@\<SDL@\<SDL/SDL@
 $ make
```
### [sdljava](http://sdljava.sourceforge.net/) ###

  * sdlawt の布石
  * Makefile を arm-apple-darwin に対応
```
 CFLAGS=-O3 -fPIC -ffast-math -DNDEBUG -falign-loops=16 -DARCH_ARM -SYS_MACOSX -fomit-frame-pointer -fno-common
 LDFLAGS=-Wl,-flat_namespace -Wl,-undefined -Wl,suppress
 JAVA_HOME=/usr/local/arm-apple-darwin/classpath

 $ cp etc/build/linux/Makefile src/sdljava/native
 $ vi src/sdljava/native/Makefile
  :
 $ cd src/sdljava/native
 $ make
```
### [sdlawt](http://sourceforge.net/projects/sdlawt/) ###

  * 未完製品、というかぜんぜん手がつけられてへんのとちゃうんか？？？
  * ~~jamvm がスペシャル遅い件、萎えた...~~
  * [midpath](http://midpath.thenesis.org/en/StartingPoints) ... SDL バインディング実装がある
  * [Open JDK](http://openjdk.java.net/groups/awt/) ...
  * [Harmony AWT](http://harmony.apache.org/subcomponents/classlibrary/awt.html)
  * [SWT\_AWT](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/awt/package-summary.html) ... ちょっと違う

### [bind](http://www.isc.org/index.pl?/sw/bind/index.php) ###

  * mount.cifs の布石
  * configure の /dev/random チェック、クロスで通らないからはずす
  * 要らんかった...orz → -DBIND\_8\_COMPAT
```
 $ vi configure
 disable /dev/random check for cross compiling
 $ vi lib/bind/configure
 disable /dev/random check for cross compiling

 $ export CFLAGS -DNDEBUG
 $ export BUILD_CC gcc
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin --enable-threads
```
### [mount.cifs](http://us4.samba.org/samba/) ###

  * クロスで /usr/local にインストールする布石
  * configure 中の smbmount, mount.cifs の linux チェックに darwin 追加
  * うーん、無理そう (include `linux/*.h` が鬼門)
    * touch に mount.hfs があったから作れるはずなんだけどね
```
 $ export CFLAGS="-DBIND_8_COMPAT -NDEBUG"
 $ export LDFLAGS="-L$SNOWBIRD/usr/lib -framework CoreFoundation"
 $ ./configure --host=arm-apple-darwin --enable-static --with-smbmount --with-cifsmount --without-utmp --with-readline --with-libiconv --without-krb5 
```
### [macfuse](http://code.google.com/p/macfuse/) ###

  * クロスで /usr/local にインストールする布石
  * samba がダメなら
```
 $ vi vi lib/helper.c
 #ifdef __APPLE__
 #include <sys/syslimits.h>
 #endif
 $ vi darwin_configure.sh
 CFLAGS="-D__FreeBSD__=10 -D_POSIX_C_SOURCE=200112L -I$MACFUSE_SRCROOT/common -O
 -g" LDFLAGS="-framework CoreFoundation" ./configure --host=arm-apple-darwin --pr
 efix=/usr/local/arm-apple-darwin --disable-dependency-tracking

 $ ./darwin_configure.sh /usr/local/arm-apple-darwin/src/macfuse/core/10.5/fusefs
```
### [quasi88](http://www.117.ne.jp/~show/pc8801/pc88emu.html) ###

  * ~~画面の初期化でエラー終了~~
  * SDL device "iphone" を追加
```
 $ vi makefile
  :
 $ make
```
  * [iPod touch](http://picasaweb.google.co.jp/lh/photo/ou7bEhzf9bysSfMt5wX8LA?feat=directlink)
### [lame](http://lame.sourceforge.net/index.php) ###

  * ffmpeg の布石
```
 $ vi libmp3lame/version.h
 s/LAME_RELEASE_VERSION ?/LAME_RELEASE_VERSION 2/

 $ export LDFLAGS=-L$SNOWBIRD/usr/lib
 $ export CFLAGS=-DNDEBUG
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [faad2](http://www.audiocoding.com/) ###

  * ffmpeg の布石
```
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [faac](http://www.audiocoding.com/) ###

  * ffmpeg の布石
  * なんか frontend で linker warning 出るけど使わないから無視
```
 $ bootstrap
 $ export CFLAGS=-DNDEBUG
 $ export LDFLAGS="-L/usr/local/arm-apple-darwin/Snowbird/usr/lib -L/usr/local/lib -L/usr/local/arm-apple-darwin/lib"
 $ ./configure --host=arm-apple-darwin --prefix=/usr/local/arm-apple-darwin
 $ make
 $ make install
```
### [x264](http://www.videolan.org/developers/x264.html) ###

  * ffmpeg の布石
  * めんどくさ
```
 $ ./bootstrap
 $ ./configure \
    --host=arm-apple-darwin \
    --prefix=/usr/local/arm-apple-darwin \
    --enable-pthread \
    --enable-shared \
    --extra-ldflags="-L/usr/local/arm-apple-darwin/Snowbird/usr/lib -lpthread" \
    --extra-cflags="-I/usr/local/arm-apple-darwin/include -D__LITTLE_ENDIAN__ -DNDEBUG"

Makefile
 Index: Makefile
 ===================================================================
 --- Makefile    (revision 680)
 +++ Makefile    (working copy)
 @@ -73,11 +73,11 @@
  default: $(DEP) x264$(EXE)
 
  libx264.a: .depend $(OBJS) $(OBJASM)
 -       ar rc libx264.a $(OBJS) $(OBJASM)
 -       ranlib libx264.a
 +       arm-apple-darwin-ar rc libx264.a $(OBJS) $(OBJASM)
 +       arm-apple-darwin-ranlib libx264.a
 
  $(SONAME): .depend $(OBJS) $(OBJASM)
 -       $(CC) -shared -o $@ $(OBJS) $(OBJASM) -Wl,-soname,$(SONAME) $(LDFLAGS)
 +       $(CC) -dynamiclib -Wl,-flat_namespace -Wl,-undefined -Wl,suppress -o $@ $(OBJS) $(OBJASM) $(LDFLAGS) -install_name /usr/local/arm-apple-darwin/lib/libx264.dylib
 
  x264$(EXE): $(OBJCLI) libx264.a
         $(CC) -o $@ $+ $(LDFLAGS)
 @@ -153,8 +153,8 @@
         install -m 644 libx264.a $(DESTDIR)$(libdir)
         install -m 644 x264.pc $(DESTDIR)$(libdir)/pkgconfig
         install x264 $(DESTDIR)$(bindir)
 -       ranlib $(DESTDIR)$(libdir)/libx264.a
 -       $(if $(SONAME), ln -sf $(SONAME) $(DESTDIR)$(libdir)/libx264.so)
 +       arm-apple-darwin-ranlib $(DESTDIR)$(libdir)/libx264.a
 +       $(if $(SONAME), ln -sf $(SONAME) $(DESTDIR)$(libdir)/libx264.dylib)
         $(if $(SONAME), install -m 755 $(SONAME) $(DESTDIR)$(libdir))
 
  install-gtk: libx264gtk.a
  comment out #undef NDEBUG
config.mak
 8,9c8,9
 < CC=gcc
 < CFLAGS=-O4 -ffast-math -DNDEBUG -Wall -I. -I/usr/local/arm-apple-darwin/include -D__LITTLE_ENDIAN__ -DNDEBUG -falign-loops=16 -DARCH_ARM -DSYS_MACOSX -s -fomit-frame-pointer
 ---
 > CC=arm-apple-darwin-gcc
 > CFLAGS=-O3 -ffast-math  -Wall -I. -I/usr/local/arm-apple-darwin/include -D__LITTLE_ENDIAN__ -DNDEBUG -falign-loops=16 -DARCH_ARM -DSYS_MACOSX -s -fomit-frame-pointer -fno-common -DPIC
 11c11
 < LDFLAGS=-L$(SNOWBIRD)/usr/lib -L$(SNOWBIRD)/usr/lib -lpthread -lm -lmx -s
 ---
 > LDFLAGS= -pie -L$(SNOWBIRD)/usr/lib -lpthread -lm
 20,21c20,21
 < CONFIGURE_ARGS= '--host=arm-apple-darwin' '--prefix=/usr/local/arm-apple-darwin' '--enable-pthread' '--enable-shared' '--extra-ldflags=-L$(SNOWBIRD)/usr/lib -lpthread' '--extra-cflags=-I/usr/local/arm-apple-darwin/include -D__LITTLE_ENDIAN__ -DNDEBUG'
 < SONAME=libx264.so.56
 ---
 > CONFIGURE_ARGS= '--host=arm-apple-darwin' '--prefix=/usr/local/arm-apple-darwin' '--enable-pthread' '--enable-shared' '--extra-ldflags=-L$(SNOWBIRD)/usr/lib -lpthread -single_module' '--extra-cflags=-I/usr/local/arm-apple-darwin/include -D__LITTLE_ENDIAN__ -DNDEBUG'
 > SONAME=libx264.56.dylib

 $ make
 $ make install
```
### [xvid](http://www.xvid.org/) ###

  * ffmpeg の布石
### [amr](http://www.penguin.cz/~utx/amr.en.html) ###

  * ffmpeg の布石
  * ボイスレコーダの変換に使えるみたいだけど iTouch には無用...
### [ffmpeg](http://ffmpeg.sourceforge.net/index.php) ###

  * gnash の布石
  * m4a の変換は faad2 だけだとダメみたい、faac が必要。
  * 動作確認！
  * 遅っ！
  * 本当は --arch=armv4 のはず、そうするとアセンブラでエラー → generic で我慢...
    * [ここ](http://www.wickedpsyched.net/iphone/media/ffmpeg/)は --arch=arm でやってるなぁ、Mac だと通るのかしら？
```
 $ ./configure \
  --extra-cflags="-DNDEBUG -I/usr/local/arm-apple-darwin/include" \
  --extra-ldflags="-L/usr/local/arm-apple-darwin/lib" \
  --prefix=/usr/local/arm-apple-darwin \
  --cross-prefix=arm-apple-darwin- \
  --cross-compile \
  --target-os=darwin \
  --arch=armv6 \
  --disable-ffserver \
  --enable-gpl \
  --enable-pp \
  --disable-vhook \
  --enable-swscaler \
  --enable-pthreads \
  --enable-swscaler \
  --enable-memalign-hack \
  --disable-ipv6 \
  --enable-libmp3lame \
  --enable-libfaad \
  --enable-libfaac \
  --enable-libx264

 # libavcodec/cook.c CFLAGS=-O1
 # libavcodec/retecontrol.c CFLAGS=-O0

 #undef NDEBUG # を全部消す

 $ make
 $ make install
```
  * ffplay 動くけど何も映らん...
```
 $ vi config.mak
 SDL_LIBS = -lSDL -lSDLmain -framwork UIKit
 $ make ffplay
```
### [boost](http://www.boost.org/) ###

  * gnash の布石
  * jam ってなんやねん... コンパイル大変だ...
```
 $ cp tools/build/v1/darwin-tools.jam tools/build/v1/arm-apple-darwin-tools.jam
 $ vi tools/build/v1/arm-apple-darwin-tools.jam
  :
 $ vi libs/regex/build/Jamfile
  :

 $ ./configure --prefix=arm-apple-darwin --with-python=no --with-icu=$SNOWBIRD/usr/lib --with-toolset=arm-apple-darwin 
 $ make
```
### [gnash](http://www.gnashdev.org/) ###
  * boost 要るのか...
    * [PS3 Linux](PS3Linux.md) でどうしたんだっけ？ yum ったのかな？