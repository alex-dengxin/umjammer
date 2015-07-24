# Install #

## Version ##
```
 # cat /proc/version
 Linux version 2.6.16 (root@sandwich.rd.scei.sony.co.jp) (gcc version 4.1.0 20060
 304 (Red Hat 4.1.0-3)) #1 SMP Thu Dec 7 18:19:02 JST 2006
 # cat /etc/issue
 Fedora Core release 6 (Zod)
 Kernel \r on an \m

 # cat /proc/version
 Linux version 2.6.16 (root@sandwich.rd.scei.sony.co.jp) (gcc version 4.1.1 20061
 011 (Red Hat 4.1.1-30)) #1 SMP Wed Apr 25 07:29:40 JST 2007
```
## Concept ##

PS3 で linux を走らせるにあたって何に使用するのが妥当か？
自宅サーバーには荷が重いし、開発マシンなんてもってのほか。
せっかく HDMI で大型液晶テレビとオーディオに繋がってるんだから
AV プレイヤーとして使用することをコンセプトにインストールしてみた記録。

## Info ##

### os ###
  * http://cell.fixstars.com/ps3linux/index.php/PS3_Linux%E3%81%AE%E3%82%A4%E3%83%B3%E3%82%B9%E3%83%88%E3%83%BC%E3%83%AB
  * http://wiki.cellfan.info/index.php/PS3_Linux_FAQ
  * [fedora 6](http://wiki.cellfan.info/index.php/FC6%E3%81%AE%E7%BD%A0)

### others ###
  * http://yun.cup.com/ps3sdl.html
## Configuration ##

### xorg.conf & chkconfig ###

  * http://cell.fixstars.com/ps3linux/index.php/PS3_Linux%E3%82%92%E4%B8%80%E9%80%9A%E3%82%8A%E4%BD%BF%E3%81%A3%E3%81%A6%E3%81%BF%E3%82%8B

### font ###

  * http://www.a.phys.nagoya-u.ac.jp/~taka/linux/fc6note.html#japanesefonts
  * http://mix-mplus-ipa.sourceforge.jp/

  * http://blogs.itmedia.co.jp/oreore/2005/10/fedora_core__fe_ca95.html

## Software ##

## delete!! ##

  * 要らんものはばっさりと消す、主にgnome, IM 外国語, Office, サーバー, 開発系
  * C 系はあとで遊ぶため、カーネルアップデートのために残す
  * Java 系も消して自分で入れる
  * group delete なんてものがあるとは...
```
 GConf2-devel MySQL-python ORBit2-devel PyQt-devel ant ant-antlr ant-apache-bcel ant-apache-bsf ant-apache-log4j ant-apache-oro ant-apache-regexp
 ant-apache-resolver ant-commons-logging ant-javadoc ant-javamail ant-jdepend  ant-jsch ant-junit ant-manual ant-nodeps ant-scripts ant-swing ant-trax antlr antlr-javadoc antlr-manual aqbanking avalon-framework avalon-logkit
 axis bcel bcel-javadoc bsf bsh classpathx-jaf classpathx-jaf-javadoc classpathx-mail classpathx-mail-javadoc compat-gcc-34-g77 compat-libf2c-34
 cryptix-asn1-javadoc cryptix-javadoc curl-devel db4-devel dejagnu docbook-simple docbook-slides docbook-utils-pdf doxygen eclipse-bugzilla eclipse-cdt eclipse-changelog
 eclipse-ecj eclipse-jdt eclipse-pde eclipse-pde-runtime eclipse-platform eclipse-platform-sdk eclipse-rcp eclipse-rcp-sdk epiphany geronimo-specs geronimo-specs-compat gjdoc gnome-vfs2-devel gnu-crypto
 gnu-crypto-javadoc gnu-getopt gnu-getopt-javadoc hsqldb jadetex jakarta-commons-beanutils jakarta-commons-beanutils-javadoc jakarta-commons-codec
 jakarta-commons-collections jakarta-commons-collections-javadoc jakarta-commons-daemon jakarta-commons-daemon-javadoc jakarta-commons-dbcp jakarta-commons-dbcp-javadoc
 jakarta-commons-digester jakarta-commons-digester-javadoc jakarta-commons-discovery jakarta-commons-el jakarta-commons-el-javadoc jakarta-commons-fileupload
 jakarta-commons-fileupload-javadoc jakarta-commons-httpclient jakarta-commons-httpclient-javadoc
 jakarta-commons-launcher jakarta-commons-launcher-javadoc jakarta-commons-logging jakarta-commons-logging-javadoc jakarta-commons-modeler jakarta-commons-modeler-javadoc
 jakarta-commons-pool jakarta-commons-pool-javadoc jakarta-commons-validator jakarta-commons-validator-javadoc jakarta-oro jakarta-taglibs-standard
 jakarta-taglibs-standard-javadoc java-1.4.2-gcj-compat java-1.4.2-gcj-compat-devel java-1.4.2-gcj-compat-javadoc java_cup java_cup-javadoc java_cup-manual
 jdepend jdepend-demo jdepend-javadoc jessie jlex jlex-javadoc jpackage-utils
 jsch junit junit-demo junit-javadoc junit-manual jzlib ldapjdk ldapjdk-javadoc
 libbonobo-devel libgnome-devel libgsf-devel libswt3-gtk2 log4j log4j-javadoc log4j-manual lucene
 lucene-demo mx4j mx4j-javadoc mx4j-manual openoffice.org-core openoffice.org-langpack-af_ZA openoffice.org-langpack-ar openoffice.org-langpack-as_IN openoffice.org-langpack-bg_BG openoffice.org-langpack-bn openoffice.org-langpack-ca_ES openoffice.org-langpack-cs_CZ openoffice.org-langpack-cy_GB openoffice.org-langpack-da_DK openoffice.org-langpack-de openoffice.org-langpack-el_GR openoffice.org-langpack-es openoffice.org-langpack-et_EE
 openoffice.org-langpack-eu_ES openoffice.org-langpack-fi_FI openoffice.org-langpack-fr openoffice.org-langpack-ga_IE openoffice.org-langpack-gl_ES openoffice.org-langpack-gu_IN openoffice.org-langpack-he_IL
 openoffice.org-langpack-hi_IN openoffice.org-langpack-hr_HR openoffice.org-langpack-hu_HU openoffice.org-langpack-it openoffice.org-langpack-ja_JP openoffice.org-langpack-kn_IN openoffice.org-langpack-ko_KR openoffice.org-langpack-lt_LT openoffice.org-langpack-ml_IN openoffice.org-langpack-mr_IN
 openoffice.org-langpack-ms_MY openoffice.org-langpack-nb_NO openoffice.org-langpack-nl openoffice.org-langpack-nn_NO openoffice.org-langpack-nr_ZA openoffice.org-langpack-nso_ZA openoffice.org-langpack-or_IN openoffice.org-langpack-pa_IN openoffice.org-langpack-pl_PL openoffice.org-langpack-pt_BR
 openoffice.org-langpack-pt_PT openoffice.org-langpack-ru openoffice.org-langpack-sk_SK openoffice.org-langpack-sl_SI openoffice.org-langpack-sr_CS openoffice.org-langpack-ss_ZA
 openoffice.org-langpack-st_ZA openoffice.org-langpack-sv openoffice.org-langpack-ta_IN openoffice.org-langpack-te_IN openoffice.org-langpack-th_TH openoffice.org-langpack-tn_ZA
 openoffice.org-langpack-ts_ZA openoffice.org-langpack-ur openoffice.org-langpack-ve_ZA openoffice.org-langpack-xh_ZA openoffice.org-langpack-zh_CN openoffice.org-langpack-zh_TW
 openoffice.org-langpack-zu_ZA postgresql-jdbc regexp regexp-javadoc struts tomcat5 tomcat5-admin-webapps tomcat5-common-lib tomcat5-jasper
 tomcat5-jsp-2.0-api tomcat5-server-lib tomcat5-servlet-2.4-api tomcat5-webapps
 wsdl4j xalan-j2 xalan-j2-demo xalan-j2-javadoc xalan-j2-manual xalan-j2-xsltc
 xerces-j2 xerces-j2-demo xerces-j2-javadoc-apis xerces-j2-javadoc-impl xerces-j2-javadoc-other xerces-j2-javadoc-xni xerces-j2-scripts xml-commons xml-commons-apis xml-commons-apis-javadoc xml-commons-apis-manual xml-commons-resolver xml-commons-resolver-javadoc xml-commons-which
 xml-commons-which-javadoc xmlrp
```
### yum ###

  * http://www.a.phys.nagoya-u.ac.jp/~taka/linux/fc6note.html#yum
  * http://www.terrasoftsolutions.com/support/solutions/ydl_5.0/yum.shtml
```
 freshrpms-release-1.1-1.fc.noarch.rpm
```
### [icewm](http://www.icewm.org/) ###

  * 軽い Window Manager
  * [設定](http://www.st.rim.or.jp/~shindo/woody-upgrade/potato2woody-7.html)も簡単
```
 glib.ppc 1:1.2.10-26.fc6
 gtk+.ppc 1:1.2.10-55.fc6
 imlib.ppc 1:1.9.15-2.fc6
 icewm.ppc 1.2.30-13.fc6
```
### [firefox](http://www.mozilla-japan.org/products/firefox/) ###

  * アップデートしたら動いた
```
 # cd /usr/lib/firefox-1.5.0.10/plugins/
 # ln -s /usr/local/java/jre/bin/libjavaplugin_oji.so
```
### [audacious](http://audacious-media-player.org/Main_Page) ###

  * 今のところ唯一まともにプレイリストが扱えるプレイヤー
  * プレイリストが小さい、テレビ遠いから見にくいねん
```
 audacious-plugins-extras-1.3.3-1.fc6.ppc.rpm
```
### [bmpx](http://bmpx.beep-media-player.org/site/BMPx_Homepage) ###

  * gstreamer を使用するアーキテクチャ的にはまあまあよろしい
  * [D-Bus のセッティング](http://bmpx.beep-media-player.org/site/FAQ#Run_a_D-BUS_session_daemon_within_your_X_session)がいる
  * SJIS タグが扱えない ... 却下
  * あ、でも[ラジオ](http://www.lastfm.jp/dashboard/)系聴くのにはすげぇ便利かも
```
 gstreamer-tools.ppc 0.10.11-1.fc6
 gstreamer.ppc64 0.10.11-1.fc6
 gstreamer.ppc 0.10.11-1.fc6
 soundtouch.ppc 1.3.1-6.fc6
 directfb.ppc 0.9.25.1-3.fc6
 gimp-libs.ppc 2:2.2.13-1.fc6
 gimp-libs.ppc64 2:2.2.13-1.fc6

 bmpx-0.36.1-2.fc6.ppc.rpm

 mpeg2dec-0.4.1-1.fc6.ppc.rpm
 libmms-0.3-1.fc6.ppc.rpm
 gsm-1.0.12-1.fc6.ppc.rpm
 gstreamer-plugins-bad-0.10.3-3.fc6.ppc.rpm

 bmpx-0.36.1-2.fc6.ppc.rpm
 gstreamer-plugins-ugly-0.10.5-1.fc6.ppc.rpm
 xvidcore-1.1.2-1.fc6.ppc.rpm
 swfdec-0.3.6-2.fc6.ppc.rpm
 amrnb-0.0.1-2.fc6.ppc.rpm

 $ eval `dbus-launch --auto-syntax`
 $ bmp2 &
```
### [gtkpod](http://www.gtkpod.org/about.html) ###

  * 外部プレイヤーが面倒い
  * ソートもいちいち行うのでウザい
```
 libmp4v2.ppc 1.5.0.1-3.fc6
 libgpod.ppc 0.4.2-0.1.fc6
 gtkpod.ppc 0.99.8-3.fc6
```
### [xmms](http://www.xmms.org/) ###

  * audacious に後継
```
 glib.ppc 1:1.2.10-26.fc6
 gtk+.ppc 1:1.2.10-55.fc6
 xmms-libs.ppc 1:1.2.10-29.fc6
 xmms.ppc 1:1.2.10-29.fc6
```
### vlc ###

  * 今のところメインのビデオプレーヤー
  * 特に問題なし
```
 SDL_image.ppc 1.2.5-4.fc6
 libebml.ppc 0.7.7-2.fc6
 libfreebob.ppc 1.0.0-3.fc6
 libcdio.ppc 0.77-3.fc6
 lirc.ppc 0.8.1-1.fc6
 libmpcdec.ppc 1.2.2-4.fc6
 libid3tag.ppc 0.15.1b-3.fc6
 libcddb.ppc 1.3.0-1.fc6
 wxGTK.ppc 2.6.3-2.6.3.2.3.fc6
 libupnp.ppc 1.4.3-1.fc6
 jack-audio-connection-kit.ppc 0.102.20-3.fc6
 libmodplug.ppc 1:0.8.4-1.fc6
 libtar.ppc 1.2.11-8.fc6
 xosd.ppc 2.2.14-9.fc6
 libmatroska.ppc 0.8.0-4.fc6
 libdvdread.ppc 0.9.7-2.fc6
 aalib.ppc 1.4.0-0.11.rc5.fc6
 arts.ppc 8:1.5.6-0.1.fc6
 arts.ppc64 8:1.5.6-0.1.fc6

 vcdimager-0.7.23-5.fc6.ppc.rpm
 lame-3.97-1.fc6.ppc.rpm
 libdvbpsi-0.1.5-2.fc6.ppc.rpm
 libdvdnav-0.1.10-3.fc6.ppc.rpm
 libopendaap-0.4.0-2.fc6.ppc.rpm
 a52dec-0.7.4-8.fc6.ppc.rpm
 faac-1.25-1.fc6.ppc.rpm
 faad2-2.5-2.fc6.ppc.rpm
 libmad-0.15.1b-4.fc6.ppc.rpm
 x264-0.0.0-0.3.20061023.fc6.ppc.rpm
 vlc-0.8.6a-4.fc6.ppc.rpm
```
### [IBM J2SE 5.0](http://www-128.ibm.com/developerworks/java/jdk/linux/download.html) ###

  * 64bit は試した限り AWT, javax.sound が動かない
    * 何とかしちゃう人 http://blog.usopyon.net/?eid=411466
  * 32bit で我慢する...
```
 ibm-java2-ppc-sdk-5.0-4.0.ppc.rpm
```
### [GNU ClassPath](http://www.gnu.org/software/classpath/) ###

  * IBM の Java でコンパイルできん...
    * 結局消したもともとの Java 開発系を復活...
  * IBM J2SE 64bit Swing を[何とかして動かしちゃう人](http://blog.usopyon.net/?eid=411466)に触発されてやってみたがIBM の JVM で動かし方が分からん...
```
 GConf2-devel.ppc
 GConf2-devel.ppc64
 ORBit2-devel.ppc
 ORBit2-devel.ppc64
 cairo-devel.ppc
 cairo-devel.ppc64
 eclipse-ecj.ppc
 gtk2-devel.ppc
 gtk2-devel.ppc64
 java-1.4.2-gcj-compat-devel.ppc
 java-1.4.2-gcj-compat.ppc
 jpackage-utils.noarch
 libX11-devel.ppc
 libX11-devel.ppc64
 libXau-devel.ppc
 libXau-devel.ppc64
 libXcursor-devel.ppc64
 libXdmcp-devel.ppc
 libXdmcp-devel.ppc64
 libXext-devel.ppc64
 libXfixes-devel.ppc64
 libXft-devel.ppc64
 libXi-devel.ppc64
 libXinerama-devel.ppc64
 libXrandr-devel.ppc64
 libXrender-devel.ppc
 libXrender-devel.ppc64
 libXtst-devel.ppc
 libXtst-devel.ppc64
 mesa-libGL-devel.ppc64
 pango-devel.ppc
 pango-devel.ppc64
 xorg-x11-proto-devel.ppc
 xorg-x11-proto-devel.ppc64

 $ tar zxvf classpath-0.95.tar.gz
 $ cd classpath-0.95
 $ ./configure
 $ make
 # make install
```
### [Synergy](http://synergy2.sourceforge.net/) ###
```
 synergy.ppc 1.3.1-2.fc6
```
  * マウス＆キーボード使う側がサーバ
  * 使われる側がクライアント
  * サーバー側は双方リンク設定しないと帰ってこれない...orz
  * Host Name はいわゆる名前解決されるホスト名 IP アドレスで OK
```
 $ synergyc $SERVER_ADDRESS
```
  * おー、超便利！
### [Gnash](http://www.gnashdev.org/) ###

  * FSF の Flash プレーヤー
```
 # cd /lib/lib64
 # rm libexpat.so
 # ln -s ../../lib/libexpat.so.0.5.0 libexpat.so

 $ export CVS_RSH="ssh"
 $ cvs -z3 -d:pserver:anonymous@cvs.sv.gnu.org:/sources/gnash co gnash
 $ cd gnash/
 $ ./configure --enable-renderer=opengl --enable-media=GST --disable-klash --enable-write --disable-cygnal
 $ make
 # make install
 # mv $HOME/.firefox/plugins/libgnashplugin.so /usr/lib/firefox-1.5.0.10/plugins/




 # cd /lib/lib64
 # rm libexpat.so
 # ln -s ../../lib64/libexpat.so.0.5.0 libexpat.so
```
  * 惜しい、[YouTube](http://www.youtube.com/) の Videolet 起動したが動かん
  * 070527 まだ YouTube 動かんねぇ...
  * ~~最近の cvs server/Makefile に RemoveTagID 系を追加しないとコンパイルできないよ~~
### [IBM Java SE 6](https://www14.software.ibm.com/webapp/iwm/web/reg/download.do?source=swerpsw-java6-3&S_PKG=code&S_TACT=105AGX59&S_CMP=GR&lang=en_US&cp=UTF-8&dlmethod=http) ###

  * これも 64 Bit 版は AWT 動かない
  * 32 Bit 版相変わらず音でねぇ
    * ALSA 使ってるはずなんだけどねぇ？
```
 ibm-java-sdk-60-linux-ppc32-20070329.tgz
```
### [Tritonus](http://tritonus.org/) ###

  * Java Sound が鳴らないので試し中だがちっとも鳴らない...
```
 $ aclocal
 $ autoconf

 # rpm -ivh libcdda-devel-9.8-22828cl.ppc.rpm
 # yum --enablerepo=extras install fluidsynth-devel
 # yum install cdparanoia-devel

 # cp -vl src/lib/alsa/libtritonusalsa.so* /usr/local/lib
 # cp -vl src/lib/cdparanoia/libtritonuscdparanoia.so* /usr/local/lib
 # cp -vl src/lib/common/libtritonuscommon.so* /usr/local/lib
 # cp -vl src/lib/esd/libtritonusesd.so* /usr/local/lib
 # cp -vl src/lib/fluidsynth/libtritonusfluid.so* /usr/local/lib
 # cp -vl src/lib/lame/liblametritonus.so* /usr/local/lib
 # cp -vl src/lib/vorbis/libtritonusvorbis.so* /usr/local/lib
 # cp -vl /usr/local/src/tritonus/src/lib/fluidsynth/.lib/libtritonusfluid.so* /usr/local/lib
 # cp -vl /usr/local/src/tritonus/src/lib/fluidsynth/.libs/libtritonusfluid.so* /usr/local/lib

 # groupadd -g 1000 audio
 # usermod -G audio -a audio $ME
 # vi /etc/security/console.perms.d/50-default.perms
 s/600\ \<audio\>\ root\.root/660 <audio> root.audio/
 # pam_console_apply -r
```
### [Second Life](http://wiki.secondlife.com/wiki/Open_Source_Portal) ###

  * オープンソースの Second Life Viewer
  * [ここ](http://matsuu.blogspot.com/2007/02/playstation-3second-life-1.html)に触発されてやってみる
  * コンパイルに 10 時間くらいかかる...
    * スワップしまくりなのでクロスでコンパイルするのが吉
```
 $ export SLSRC=/usr/local/src/linden
 $ export OPENJPEG=/usr/local/src/OpenJpeg
 $ export ELFIO=/usr/local/src/ELFIO
 $ export XMLRPC=/usr/local/src/xmlrpc

 $ export CC=gcc34
 $ export CXX=g++34
 $ export CFLAGS=-fPIC
 $ export CXXFLAGS=-fPIC

 $ cp -a /usr/include/atk-1.0 ${SLSRC}/libraries/powerpc-linux/include/
 $ cp -a /usr/include/gtk-2.0 ${SLSRC}/libraries/powerpc-linux/include/
 $ cp -a /usr/lib/gtk-2.0/include/* ${SLSRC}/libraries/powerpc-linux/include/gtk-2.0/
 $ cp -a /usr/include/glib-2.0 ${SLSRC}/libraries/powerpc-linux/include/
 $ cp -a /usr/lib/glib-2.0/include/* ${SLSRC}/libraries/powerpc-linux/include/glib-2.0/
 $ cp -a /usr/include/pango-1.0 ${SLSRC}/libraries/powerpc-linux/include/

 $ cp -a /usr/include/cairo/* ${SLSRC}/libraries/powerpc-linux/include/

 $ cp -a /usr/include/apr-1/ ${SLSRC}/libraries/powerpc-linux/include/apr-1

 $ mkdir ${SLSRC}/libraries/ppc-linux/include/expat
 $ cp -a /usr/include/expat*.h ${SLSRC}/libraries/powerpc-linux/include/expat/

 $ mkdir ${SLSRC}/libraries/ppc-linux/include/zlib
 $ cp -a /usr/include/zlib*.h ${SLSRC}/libraries/powerpc-linux/include/zlib/

 $ cd ${OPENJPEG}/libopenjpeg
 $ patch -p1 < ../patch/OPJ_limit_tags_for_decode.patch
 $ patch -p1 < ../patch/OPJ_limit_tags_for_decode_UPDATED.patch
 $ cd ${OPENJPEG}/
 $ make
 $ mkdir ${SLSRC}/libraries/ppc-linux/include/openjpeg
 $ cp ${OPENJPEG}/libopenjpeg/openjpeg.h ${SLSRC}/libraries/powerpc-linux/include/openjpeg/
 $ cp ${OPENJPEG}/libopenjpeg.a ${SLSRC}/libraries/powerpc-linux/lib_release_client/

 $ cd ${ELFIO}/
 $ ./configure
 $ make
 $ mkdir ${SLSRC}/libraries/ppc-linux/include/ELFIO
 $ cp ${ELFIO}/ELFIO/*.h ${SLSRC}/libraries/powerpc-linux/include/ELFIO/
 $ cp ${ELFIO}/ELFIO/libelfio.so ${SLSRC}/libraries/powerpc-linux/lib_release_client/
 $ cd ${ELFIO}/ELFIO
 $ g++34 -fPIC -shared *.o -o libelfio.so

 $ mkdir ${SLSRC}/libraries/ppc-linux/include/jpeglib
 $ cp -a /usr/include/j*.h ${SLSRC}/libraries/powerpc-linux/include/jpeglib/
 $ touch ${SLSRC}/libraries/ppc-linux/include/jpeglib/jinclude.h

 $ mkdir ${SLSRC}/libraries/ppc-linux/include/llfreetype2
 $ cp -a /usr/include/freetype2/freetype/ ${SLSRC}/libraries/powerpc-linux/include/llfreetype2/
 $ cp -a /usr/include/ft2build.h ${SLSRC}/libraries/powerpc-linux/include/llfreetype2/freetype/

 $ cd ${XMLRPC}/
 $ ./configure
 $ make
 $ mkdir ${SLSRC}/libraries/ppc-linux/include/xmlrpc-epi
 $ cp -a ${XMLRPC}/src/*.h ${SLSRC}/libraries/ppc-linux/include/xmlrpc-epi/
 $ cp ${XMLRPC}/src/.libs/xmlrpc.a ${SLSRC}/libraries/powerpc-linux/lib_release_client/

 $ cd ${SLSRC}/indra
 $ sed SConstract
 s/i686/powerpc/g
 s/g\+\+\-3\.4/g++34/
 s/DLL_FMOD=1/DLL_FMOD=0/
 $ scons DISTCC=no BTARGET=client BUILD=release MOZLIB=no
```
  * ~~あり？~~ ← -fPIC 付けまくって解決
```
 $ ( cd newview && LD_LIBRARY_PATH=../../libraries/powerpc-linux/lib_release_client:${LD_LIBRARY_PATH}  ./secondlife-powerpc-bin )
 ./secondlife-powerpc-bin: error while loading shared libraries: ../../libraries/
 powerpc-linux/lib_release_client/libelfio.so: R_PPC_REL24 relocation at 0x0ff8ee
 e4 for symbol `__cxa_atexit' out of range
```
  * なんかファイルが無いってまだ起動しない
    * gtk+ のエラーダイアログは出た
## Kernel Update ##

### [Addon 20070425](ftp://ftp.uk.linux.org/pub/linux/Sony-PS3) ###

  * 説明書には FC6 が入っていたとしても install-fc をやれと書いてある
    * アホか、カーネルアップデートで毎回クリーンインストールしろってか！
  * 手動で行う
    * カーネル
    * その他
```
 # rpm -ivhU --force --nodeps /mnt/cdrom/target/kernel-2.6.16-20070425.ppc64.rpm
 # cp /mnt/cdrom/target/initrd.img-2.6.16 initrd.img

 # rpm -ivhU --nodeps /mnt/cdrom/target/glibc-kernheaders-2.6.16-20070425.ppc.rpm
 # rpm -ivhU --force /mnt/cdrom/target/kexec-tools-1.101-4.ppc64.rpm
 # rpm -ivhU /mnt/cdrom/target/ps3pf_utils-1.0.10-1.ppc.rpm
 # rpm -ivhU /mnt/cdrom/target/vsync-sample-1.0.2-1.ppc.rpm
```
    * /etc/yum.conf
```
 # exclude PS3 specific packages
 exclude=kernel-* glibc-kernheaders-* kernel-headers-* kexec-tools-*
```
    * kboot はこの ADDON CD についてくる version 1.1 ベースのものを使うと /etc/kboot.conf のデバイスのロード順位が変わって自動起動しなくなったので古いのを使う(いまいち納得できてないのだが...)
# Use #

### Mount iPod ###

  * iPod の調子が悪くてすぐに切れる...
```
 # mkdir /mnt/ipod
 # mount -r -t vfat /dev/sdf2 /mnt/ipod
```

### Mount Windows via Network ###

  * iPod もこれなら OK
```
 # mkdir /mnt/cifs1
 # mount -r -t cifs -o username=$USER_NAME,ip=$IP_ADDRESS,codepage=cp932,iocharset=utf8 //$PC_NAME/$PC_SHARED_DIR /mnt/cifs1
```
### Xconsole ###
```
 # visudo
 $ sudo xconsole &
```
  * TODO /etc/security/console.perms.d/50-default.perms

### MIDI ###

  * デバイスの準備
  * たぶん必要なかった...
```
 # modprobe snd-seq-midi # もともとあったかも ???
 # modprobe snd-virmidi index=1 # 要らん
```
  * timidity ダイレクトはほぼ完璧
```
 $ timidity -id -Os foo.mid
```
  * timidity サーバモードはパラメータの調節が必要(何で？？？)
```
 $ timidity -iA -Os --sequencer-ports=2 -EFresamp=1 EFreverb=n -q2.0/100 &
 $ cat /proc/asound/seq/clients
 :
 Client 128 : "TiMidity" [User]
 :
 $ pmidi -p 128:0 foo.mid
```
  * fluidsynth は重いのはダメ
```
 $ fluidsynth /usr/share/soundfonts/VintageDreamsWaves-v2.sf2 bar.mid
```
# Result #

結果です。IceWM 上で Audacious の音楽聴きながら Firefox で WEB サーフィンするくらいならぜんぜん快適です。

[PS3Linux](http://picasaweb.google.co.jp/lh/photo/kkYqInr2UBJ8nQ9iWrWC0w?feat=directlink)

うぉ、比較物がないので大きさが伝わらん(笑)。52 inch
の 1920x1080 dot by dot の広大なスクリーンなのに...
壁紙と Start アイコンは Sony のページから拝借。
```
 $ display -window root wallpaper.png

 $ convert start.png start.xpm
 $ cp start.xpm /usr/share/icewm/taskbar/
```
# TODO #

  * ibm java se 5
  * ibm java se 6
    * 音がでねぇ

  * mount.vfat
    * iPod の調子が悪い？と途中で切れる...
    * syslog にものすごい量のメッセージが...
    * 確かに iPod の調子は悪いのだが Windows からは OK ってことは、やっぱり SONY が iPod を嫌ってるとしか思えん...

  * xorg.conf のローカルセッティングはどこに書く？
    * ~~mkfont`*` で毎回上書きされる...orz~~
    * ~~reboot ？で毎回上書きされる...orz~~
      * kudzu スクリプトが書き換えてるみたい
```
 # mv /etc/X11/xorg.conf-vfb /etc/X11/xorg.conf-vfb.orig
```
  * audacious
    * ~~ALSA 出力でノイズ~~
      * カーネルアップデートで OK
    * メイン画面の文字化け

  * firefox
    * ~~明朝体~~

  * screen
    * utf8 がうまく動かん
      * http://www.dekaino.net/screen/01install.html

  * IceWM
    * font 設定
    * glyph not found: 169
    * icewm-session で icewmtray, icembg を disable に

  * X11
    * disable ipv6

  * UXTerm
    * 日本語フォント設定

  * ADDON 20070425
    * コピー残り
```
 # cp /etc/kudzu	   	$SYSIMAGE/etc/init.d
 # cp /etc/ワイヤレス
```
    * 古い install-fc とちゃんと diff とる

  * Window Manager
    * http://www.xfce.org/ こんなのある
    * http://www.enlightenment.org/ こいつも使ってみたいなぁ