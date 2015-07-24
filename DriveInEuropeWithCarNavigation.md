
# ヨーロッパでカーナビドライブ #


---

みなさんこんにちは，私来月(2003/3/15 執筆)から一ヶ月間ヨーロッパへ旅行に行きます．レンタカーを借りて旅しようと思っています．SONY のハンディ GPS を持っているので旅の軌跡をこいつに記録させようかと思い立ちました．ついでにカーナビとして使えないかと検討し始めたらハマリました．あと一ヶ月もないのに，レンタカーもまだ予約してないのに，ソフトもまだぜんぜん出来上がってないのに，大丈夫か？

無事帰ってまいりました(2003/05/05)．結果だけなら[こちらへ](http://code.google.com/p/umjammer/wiki/DriveInEuropeWithCarNavigation#結果報告)．ソフトウェアは[こちらへ](http://code.google.com/p/umjammer/wiki/DriveInEuropeWithCarNavigation#ソフトウェア)．

## Microsoft Autoroute ##
ヨーロッパでの PC カーナビの定番は [Microsoft AutoRoute](http://www.microsoft.com/uk/homepc/autoroute/) みたいです(ホントか？あんまし調べてない．．．)．

GPS は[NMEA](http://www.nmea.org/) ~~2.0 or later とかいう規格~~ でないとダメらしい？

→ NMEA-0183 Version 2.0 以降という意味です．

## SONY PCQ-HGR3S ##
# PCQ-HGR3S とは？ #
SONY 製のハンディ GPS レシーバです．Navin'You 買ったときについてきた奴です．せっ，生産中止．．．いきなりかい．どうも Navin'You 関連は VAIO の販促用だったみたいである．

### PCQ-HGR3S を NMEA GPS として MS AutoRoute につなぐ ###

~~たぶん以下のようにすればいけるはず．~~
```
 [GPS] PCQ-HGR3S
 
    | (HGR format)
    V
 
 [I/O] USB
 
    | ... generic USB driver (*1)
    V
 
 [Software] HGR format to NMEA format converter (*2)
 
    | (NMEA format)
    V
 
 [I/O] COM port 1
 
    | ... virtual COM port link driver (*3)
    V
 
 [I/O] COM port 2
 
    |
    V
 
 [カーナビ] MS Autoroute
```

~~よって必要なものは，~~

  * ~~汎用 USB ドライバ~~
  * ~~HGR -> NMEA フォーマットコンバータ~~
  * ~~COM ポートをクロスケーブルでつないだように見せかけるドライバ~~

こっちのほうがオールフリーでいけるので良さげ 030321

```
 [GPS] PCQ-HGR3S
 
    | (HGR format)
    V
 
 [I/O] USB
 
    | ... generic USB driver (*1)
    V
 
 [Software] HGR format to NMEA format converter (*2)
 
    | (NMEA format)
    V
 
 [I/O] TCP/IP port
 
    | ... virtual COM port driver via network (*3)
    V
 
 [I/O] Virtual COM port
 
    |
    V
 
 [カーナビ] MS Autoroute
```

よって必要なものは，

  * 汎用 USB ドライバ
  * HGR -> NMEA フォーマットコンバータ
  * ネットワークアクセス可能な仮想 COM ポートドライバ

### 近いもの ###
  * [HGR1US SONY-HGR1/3をシリアル接続に見せるドライバ](http://www.vector.co.jp/soft/win95/home/se181280.html) ... Win 9x 用でかつ出力フォーマットが HGR か IPS-5000 (一番俺のシステムに近かったがシェアだし 9x 用じゃねぇ)
  * [AnyGPS](http://www.vector.co.jp/soft/win95/home/se150051.html) ... 上と同じ作者，今わかったのだがこれがあれば俺のシステム(2. のコンバータのところね)必要ないことに気付いた...まぁ俺の方はネットワークでもデータ飛ばせるからアドバンテージがあるとしよう...
## 汎用 USB ドライバ ##
和製がありました．

  * [uusbd.sys](http://www.otto.to/~kasiwano/toppage12.htm) ... 結構有名みたい．
VID を 054c (ソニー) ，PID を 0040 （HGR3) にしてインストールすれば OK ！

あと USB 用の Java API を組まなければいけません．[ここ](http://hccweb1.bai.ne.jp/~hcj64001/cgi-bin/fswiki/wiki.cgi?page=%C5%FD%B9%E7%A5%A2%A1%BC%A5%AB%A5%A4%A5%D0%A5%D7%A5%ED%A5%B8%A5%A7%A5%AF%A5%C8)で JNI やっといてよかった．とってもスムーズに JNI が組めるようになってる俺．暇があったら [JSR80](http://javax-usb.sourceforge.net/) の Win32 実装にしようと思ったのだが，API が煩雑すぎだし残り半月仕事しながらじゃ無理なので断念．

とりあえずその場しのぎの [Java USB API](http://code.google.com/p/umjammer/source/browse/trunk/vavi-apps-gps/src/main/java/vavi/uusbd/)

## HGR -> NMEA フォーマットコンバータ ##
もちろん俺は Java しかできないので Java で組みます．

### HGR 解析 ###
  * [Sony IPS GPS Data Format](http://happy.emu.id.au/neilp/gps/ipsformat2.htm) ... IPS-2010 解析
  * [IPS Format](http://www.asahi-net.or.jp/~KN6Y-GTU/ips/ipsformat.txt) ... IPS 解析
  * [AnyGPS](http://www.vector.co.jp/soft/win95/home/se150051.html) ... いろんな GPS のコンバータみたい
  * [PCQlib](http://www.ht.sfc.keio.ac.jp/~nandu/gps/gps.html) ... Linux 用の HGR コントロールライブラリ
  * [USB Sniffer](http://benoit.papillault.free.fr/usbsnoop/index.en.php) ... Navin'You とのセッションの解析に使用
  * [HGR 解析](http://navi.org/aizu/gps/) ... ~~私のメールリクエストに答えて復活していただきました！感謝！~~

### Navin'You 5.5 と HGR3S の USB セッション ###
|**コマンド**|**I/O**|**データ**|
|:-------|:------|:------|
|パワーオン   |←      |`!POWON\r\n`|
|        |→      |`ROM    OK\r\n`|
|        |       |`RS232C OK\r\n`|
|        |       |`CLOCK  NG\r\n`|
|        |       |`\r\n` |
|        |       |`        ----< SONY GLOBAL POSITIONING SYSTEM >-----\r\n`|
|        |       |`\r\n` |
|        |       |`                               (C)Copyright 1991,1997   Sony Corporation.\r\n`|
|        |       |`\r\n` |
|        |       |       |
|???     |←      |!PC\r\n  |
|        |→      |OK\r\n  |
|ID 取得   |←      |!ID\r\n  |
|        |→      |IDDTPCQ-HGR3,1.0.00.07281\r\n  |
|???     |←      |!GP    |
|        |→      |OK\r\n  |
|???     |→      |@VF040\r\n  |
|        |←      |\r\n   |
|        |       |@VF040\r\n|
|        |       |\r\n   |
|        |       |       |
|測地系の設定  |→      |@SKB\r\n  |
|        |←      |       |
|        |       |       |
|測地系の設定 (2 回しないと効かない？)|→      |@SKB\r\n  |
|        |←      |\r\n   |
|        |       |@SKB\r\n|
|        |       |\r\n   |
|        |       |       |
|パワーオフ   |←      |!PUOFF\r\n|
|        |→      |       |

## NMEA ##
  * [Understanding NMEA 0183](http://pcptpp030.psychologie.uni-regensburg.de/trafficresearch/NMEA0183/index.html) ...
  * [GM-38:マーキュリー取扱説明書](http://www.spa-japan.co.jp/DataBank/GM38m5.html) ...
  * [NMEA-0183フォーマット](http://bg66.soc.i.kyoto-u.ac.jp/forestgps/nmea.html) ...
  * [GARMIN GPS 12 XL](http://www.ne.jp/asahi/gps/nori/GARMIN/) ... 閉鎖
## 開発状況 ##
  * 03/15 やっと HGR3S パワーオンできた．こんなペースで大丈夫か？
  * 03/21 AutoRoute に[現在位置表示](http://picasaweb.google.co.jp/lh/photo/KjHB64wqxOBmLRBpzgL2UA?feat=directlink)できました！(プライバシー全開なんで自動車アイコンの位置はずらしてあります．それにしてもヨーロッパ用のソフトの地図に今住んでいる場所の地名が載っていることにちょっと感動)
  * 03/26 [マルチキャスト機能](http://picasaweb.google.co.jp/lh/photo/nABPQK905aRMiCXNcWqrbQ?feat=directlink)をつけてみた．クライアントは [NmMonitor](http://www.surveytec.com/groom/gsoft/nmmoni/index.html) (NMEA), Navin'You (IPS-5000), MS AutoRoute (NMEA) の 3 つ．ちなみに Navin'You と AutoRoute は別パソコン上
  * 04/10 M$ AutoRoute の欠点として方向が出ません。私結構方向音痴なので困ってしまって、旅行中に[こんなの](http://picasaweb.google.co.jp/lh/photo/NKwNoK6dki95aEbGr8hP1g?feat=directlink)作りました(made in France (笑))。Navin' You でいう天空図ですね。
## TODO ##
  * まだ AutoRoute に Altitude と Time of fix が表示されていない ~~(NMEA センテンスが足りない？)~~
    * → Altitude は 3D 測定時のみ？Time of fix は非測定になったときの最後の時間を表示？？？
  * IPS と NMEA 間の衛星の信号強度の変換式が分かっていない
    * → x 3 にしておいた(AnyGPS は x 2 していた)
  * IPS dop = Q or not で測位の ON/OFF を判断していいのか？
    * → 緯度が 'n' または 's' で非測位時，'N' または 'S' で測位中 (from AnyGPS)
  * Map Datum の変更
    * → @SKA コマンドで変更できました
  * 対ヨーロッパ事前テスト用のダミー GPS 情報入力プラグイン
    * ~~MS AutoRoute でホントにいいの？~~
  * HGR 生データのロガープラグイン(ナビソフトに任すのではなく自分で取っておく)

## COM ポートをクロスケーブルでつないだように見せかけるドライバ ##

シェアか製品しかないみたい，何でこんなものに１万円近く払わにゃならんのか？~~どうせ一ヶ月なんで試用モードで２回インストールすることに決定．開発時は本物の COM ポートをクロスケーブルでつないでやることにする．(持っていくノートにはもちろんCOM ポートなど無い)~~

  * [VSPD XP3](http://www.mks.zp.ua/vspdxp.php) ... 試用期間 14 日，起動時にウザいダイアログあり
  * [VCP 300](http://www.ise-ics.co.jp/ics/system/virtualcomportcontents.html) ... 試用制限 5 分，和製
  * [NULL\_COM 仮想シリアルポートドライバ](http://www.vector.co.jp/soft/win95/hardware/se150050.html) ... この開発者俺の欲しいものばっか作ってるがシェアだし 9x 用

## ネットワークアクセス可能な仮想 COM ポートドライバ ##

別に COM to COM じゃなくでも Network to COM でよかったことに気付いた．こちらはフリーがあります．つーか，はるかにこっちのほうがいろんな事できる(GPS データを違うマシンに飛ばしたり，GPS クライアントを複数にしたり)．なんで COM to COM だと有料で，これだと無料なんだろうね？

  * [DeviceComm Manager](http://www.lantronix.com/support/utils/devicecomm/) ... 使い勝手いいです．フリー！
  * [NPCOMM](http://www.yk.rim.or.jp/~kurohara/npcomm.html) ... 和製，シェア(今はフリー？)，なぜかインストールできなかったので断念
  * [Comfoolery!](http://www.brianpoe.com/comfoolery/) ... これもか？

## ソフトウェア ##

http://umjammer.googlecode.com/svn/trunk/vavi-apps-gps/


---


## 結果報告 ##

とりあえずは大成功か？M$ AutoRoute はヨーロッパではめちゃくちゃ役に立ちます．これなしでは旅できなくなるくらい便利でした．

あと本当は飛行機内で GPS 記録したかったんだけどあの窓では HGR は衛星捉えてくれませんでした．マッハで移動する軌跡取りたかったのに．．．

とった記録を再現してみた。パリ−ツールズの移動の一部です。再現ソフトは[こちら](http://gpsmap.sourceforge.net/)。下記「疑問」の補正はしてないのでちょっとずれている。右上のにぎやかそうなところがパリです。見ればわかるがパリ以外ホント田舎ばっか。

![http://lh3.ggpht.com/_JchUHfE3WF4/SmmqJM5pBlI/AAAAAAAAAFc/YY_Uox3hbxo/s288/replay.png](http://lh3.ggpht.com/_JchUHfE3WF4/SmmqJM5pBlI/AAAAAAAAAFc/YY_Uox3hbxo/s288/replay.png)

## 疑問 ##

### @VF コマンド ###

なんか AutoRoute に表示されているポイントが実際より SW 400m 位ずれてたんだよね．そんでふと思って，起動時に @VF040 ってあったよなぁ？その 4 って400m の 4 かもって．@VF000 ってやってみたら案の定ポイントぴったり合いやがんの(でも，WGS84 と Tokyo Datum の差異は '''NW''' 約400m じゃなかったっけ？)．これに気付いたのが 4/26 ごろ．もう旅終わりやん．記録データ全部補正してやらんといかん．くっそー．なまじっか M$ AutoRoute にマップ補正機能があったのも気付くの遅れた原因だよなぁ．

## おまけ(その１) ．．．アウトレット ##

イタリアの有名ブランドアウトレットショップ行ってきたので紹介しちゃおう．とはいってもここは技術志向サイト(笑)なので緯度経度で表示してみた．だって他のサイトみたいに住所書かれてもさっぱりわからんしね(イタリアでは，アメリカみたいに家に番号が振られているのが少ないのでわかりづらい)．駐車場に停めた地点なので確実です．ホント田舎なのだがさすがブランド大好き日本人，どちらも数名に出会ってしまった．

### The Mall (GUCCI, GIORGIO ARMANI, Yves Saint Laurant等) ###

  * [Latitude 43.70217 North](http://maps.google.co.jp/maps?hl=ja&q=N43.70217,+E11.46401&lr=&ie=UTF8&z=17&ll=43.702172,11.464008&spn=0.005468,0.009677&t=k&om=1&iwloc=A)
  * [Longitude 11.46401 East](http://maps.google.co.jp/maps?hl=ja&q=N43.70217,+E11.46401&lr=&ie=UTF8&z=17&ll=43.702172,11.464008&spn=0.005468,0.009677&t=k&om=1&iwloc=A)

ここは田舎にぽつんとあるので分かりやすい．現在建て増し工事中である．

### I PELLETTIERI D'ITALIA (Prada, MiuMiu, JIL SANDER) ###

  * [Latitude 43.51292 North](http://maps.google.co.jp/maps?hl=ja&ie=UTF8&q=N43.51292,+E11.59702&z=16&ll=43.51292,11.59702&spn=0.010971,0.019355&t=k&om=1)
  * [Longitude 11.59702 East](http://maps.google.co.jp/maps?hl=ja&ie=UTF8&q=N43.51292,+E11.59702&z=16&ll=43.51292,11.59702&spn=0.010971,0.019355&t=k&om=1)

こっちは看板がない(見えない？)のでちょっと難しいかも．Montevarchi から見て右手に Agip のガスステ，左手にぎざぎざ(いわゆる工場)屋根の工場があるところの，ぎざぎざ屋根の工場の奥のほうである．

ここの情報得るのにアメリカのあるサイトを見たんだがそこの説明が秀逸．「前述の方法で見つけられなければ車をとめて待つことである．しばらくすると若いカップル(たぶん日本人)が乗ったタクシーが通るであろうから，それについて行けば良い」だって．．．

## おまけ(その２) ．．．流行ちう ##

パリの女子高校生の間でベルボトムが大流行ちうである．ベルボトムの中のスニーカーはまさしくガンダムと呼べるほどどデカイのを履いている．でも高校卒業したような年齢の人は全然そんなことはない．ここら辺は女子高生間で流行ったものは若年世代すべてに流行ってしまう日本と違うところか？

フランス全体ではインラインスケートやっている人がものすげー目立った．それも移動手段として．イタリアに行くと極端に減る．[ヒーリーズ](http://www.heelys.jp/)持ってけば良かったと少し後悔．．．


---

2003/03/15