# PS2Linux #

この記事敗北して Linux Kit 買っちゃったのでボツです。あらかじめお断りしておきます。

## コンセプト ##

PS3Linux が結構あっさり行ってしまったので、長年の懸案であった
PS2Linux もやってみようとしてはまりまくり...
俺の奴は SCPH-10000 なので firewire がついてます。HDTV のハードディスクレコーダに仕立てるのが最終目標。

PS2 の電源オンのみで起動DVDなしで起動できればいいね。

### 持ってる物 ###

  * PS2 SCPH-10000 本体
  * PlayStation BB Unit
    * 外付けディスク
    * ネットワークカード
  * メモリージャグラー

肝心の [PS2 Linux Kit](http://www.ps2linux.com/) が諸事情により手に入っていないです。こいつのせいでいろいろ調べまくる羽目に...

なので PS2 Linux Kit 持ってる人はここの情報必要ないです。
持ってない人が如何に Linux を PS2 に入れるか、使えるかがこのページの意義です。

あと、ゲーム関連(バックアップ等)も興味ないのでそっち方面で来た人もお帰りください。
### 何をすればいい ###

これが分かるのにものすごい調べたんだけど...

  * BB Navi は Linux なので、こいつの telnetd を起動できるようにすればあとはやり放題？

  * http://hp.vector.co.jp/authors/VA008536/ps2linux/bbnavi.html
  * http://hp.vector.co.jp/authors/VA008536/ps2linux/akmem.html
## 長い道のり ##

### [PS2 Independence Exploit](http://en.wikipedia.org/wiki/PS2_Independence_Exploit) ###

こいつを利用してメモリカードと仮ブートCDを使って Home Brew プログラムを
動かすことができます。

  * 仮ブートCD (PSのゲームCDのどれかでよい)
  * C-TITLE (ググれ)
  * [ps2link](http://ps2dev.org/ps2/Loaders)
  * [ps2client](http://ps2dev.org/ps2/Loaders)
  * [ps2sdk](http://ps2dev.org/ps2/Projects)
  * psu file manager (ググれ)
  * メモリージャグラー (オクれ)

C-TITLE で TITLE.DB 作成、
PS2LINK.ELF を BOOT.ELF にリネーム
```
 $ psu a BIDATA-SYSTEM.psu TITLE.DB BOOT.ELF IPCONFIG.DAT IOMANX.IRX PS2DEV9.IRX PS2IP.IRX PS2LINK.IRX PS2SMAP.IRX ps2netfs.irx
```
メモリージャグラーで転送して、仮ブート CD をセットして PS2 をリセット。
おー、ps2link 動いた、動いた。

### PS2 とセッション ###

次は PC で、
```
 $ ps2client -t 1 -h 192.168.1.xxx execiop rom0:SIO2MAN
 $ ps2client -t 1 -h 192.168.1.xxx execiop rom0:MCMAN
 $ ps2client -t 1 -h 192.168.1.xxx execiop host:ps2atad.irx
 $ ps2client -t 10 -h 192.168.1.xxx execiop host:ps2hdd.irx -n 2 -o 4
 $ ps2client -t 1 -h 192.168.1.xxx execiop host:ps2fs.irx
 $ ps2client -t 1 -h 192.168.1.xxx execiop mc0:BIDATA-SYSTEM/ps2netfs.irx
```
ps2hdd はタイムアウト長めが吉
```
 $ ps2client -t 1 -h 192.168.1.xxx dir hdd0:
 
 [Contents of hdd0:]
 
  ---------x     262144 09-30-2003 22:55:19 __mbr
  -r--------     262144 09-30-2003 22:55:20 __net
  -r--------     524288 09-30-2003 22:55:21 __system
  -r--------    1048576 09-30-2003 22:55:59 __sysconf
  -r--------    2097152 09-30-2003 22:57:05 __common
  -r--------    2097152 09-30-2003 22:57:05 __contents
  -r--------    2097152 09-30-2003 22:57:05 __contents
  -r--------    2097152 09-30-2003 22:57:05 __contents
  -r--------    2097152 09-30-2003 22:57:06 __contents
  -r--------    2097152 09-30-2003 22:57:06 __contents
  --w---x---    1048576 09-30-2003 22:57:07 __linux.4
  --w---x---    1048576 09-30-2003 22:59:34 __linux.5
  --w---x---     262144 04-08-2006 15:04:25 __linux.6
  --w-----w-     262144 09-30-2003 23:04:11 __linux.2
  --w---x---     524288 09-30-2003 23:04:25 __linux.7
  --w---x---    1048576 09-30-2003 23:01:51 __linux.1
  :
  ----------     262144 05-13-2007 03:03:41 __empty
  :
 
 [26 Files - 24117248 Bytes]
```
きたきた、`＿linux.#` とかを fs にマウントしてやれば BB Navi のファイルが見えそうだ。多分 `__linux.1` に Linux のコアファイルがありそう。
```
 $ ps2client -t 1 -h 192.168.1.60 mount pfs0: hdd:__linux.1
```
あり？、エラーでマウントできないよ...つか書式がさっぱり分からん。[ググって](http://forums.ps2dev.org/viewtopic.php?t=465&view=previous&sid=90a8fb397abddd530dbd8880a082735b)も情報ほとんどないし。
あとちょっとなのに...

[ここ](http://hp.vector.co.jp/authors/VA008536/ps2linux/bbnavi.html)よく読めば `__linux.1` マウントするのにパッチがいるって書いてあるやん。[これ](http://www.geocities.jp/ps2linux_net/bn/mount.html)か？
うわー、ps2fs Type 88 用のIRX つくんの？そもそも作れるのか？Linux DVD 無しはつらいなぁ...
普通の奴は Type 100 で pfs らしいのでマウントできたかも？

メモカブートで Linux 動かないかなぁ？

  * [Linux Diskless Client](http://hp.vector.co.jp/authors/VA008536/ps2linux/diskless.html)
  * [initrd](http://achurch.org/ps2/)

今回はここまで、う〜、長くなりそう...

### [TGE](http://lists.topica.com/lists/tgedev) ###

ネットを調べてると [TGE](http://svn.ps2dev.org/listing.php?repname=ps2ware&path=%2Ftrunk%2FTGE%2F&rev=0&sc=0) なるまさに欲しいものがあった！
が、頓挫してやがる...

## 掟破り ##

もう面倒なので、正攻法はヤメヤメ。

ハードドライブ分解して PC に接続して直接セクタ書き換えることにした。
あーあ、シール破っちゃったよ...Maxter の普通の IDE なんやね。
BBNavi のバージョンは 3.0。

  * rc.conf
    * ~~こっち書き換えんの忘れてた...~~ → 書き換える必要なさそう
    * ~~今夜こそ...~~

  * inet.conf
    * 2箇所もありやがる...前バージョンの痕跡か？
```
 セクタ 17860354
        17922178
```
あれー inetd 起動してないよ？？？

  * TODO /etc/rc.d/rc.sysinit

## 敗北 ##

PS2 Linux Kit Beta がオークションででてたので落札しちゃいました...orz
(だって￥3000で買えたんだもん...)

Sync on Green のディスプレイ持ってないし、そもそも DVD だけ(VGAケーブルもない)
なのでブラインドインストールと思ってググると Linux Kit 1.0 用の説明ばっかりで
あせりました。でも select + [R1](https://code.google.com/p/umjammer/source/detail?r=1) で NTSC ぜんぜん映ります。

ようやく PS2 で Linux が起動できた... うれしいけどいまいち達成感がない...

### boot without DVD ###

  * install Linux beta 1
  * [make kernel](http://hp.vector.co.jp/authors/VA008536/ps2linux/kernel3.html) 2.2.26
    * [patch](http://hp.vector.co.jp/authors/VA008536/ps2linux/bblinux2.diff.gz)
  * [akmem](http://hp.vector.co.jp/authors/VA008536/ps2linux/akmem.html)
    * [mount bbnav](http://hp.vector.co.jp/authors/VA008536/ps2linux/kernel2.html#bblinux2)
    * [copy in.telnetd in.ftpd](http://www.geocities.jp/ps2linux_net/bn/telnet.html)
    * [modify /etc/rc.d/rc.sysinit](http://www.geocities.jp/ps2linux_net/bn/ps2boot.html)

### Current ###

  * 普通に BBN (2.4.17) 起動 (telnet)
  * ボタン押し起動で 2.2.26 起動 (telnet,ssh)