# Development Environment #

## Building Development Environment ##
### w/ cygwin, Windows XP ###
  * vfdecrypt (for cygwin latest ~~download~~)
```
 $ gcc vfdecrypt.c -lcrypto -o vfdecrypt.exe
 $ vfdecrypt -i xxx-yyyy-z.dmg -o decrypted-a.b.c.dmg -k key
```
|version|code|ipsw|dmg|key|
|:------|:---|:---|:--|:--|
|1.0.1  |Heavenly|    |694-5262-39.dmg|28c909fc6d322fa18940f03279d70880e59a4507998347c70d5b8ca7ef090ecccc15e82d|
|1.1.1  |Snowbird|iPhone1,1\_1.1.1\_3A109a\_Restore.ipsw|022-3602-17.dmg|f45de7637a62b200950e550f4144696d7ff3dc5f0b19c8efdf194c88f3bc2fa808fea3b3|
|1.1.2  |Oktoberfest|iPod1,1\_1.1.2\_3B48b\_Restore.ipsw|022-3724-1.dmg|70e11d7209602ada5b15fbecc1709ad4910d0ad010bb9a9125b78f9f50e25f3e05c595e2|
|1.1.3  |LittleBear|    |022-3743-100.dmg|11070c11d93b9be5069b643204451ed95aad37df7b332d10e48fd3d23c62fca517055816|
|1.1.4  |LittleBear|    |   |   |
|1.2    |Aspen|    |   |   |
|2.0    |    |    |   |2cfca55aabb22fde7746e6a034f738b7795458be9902726002a8341995558990f41e3755 |

  * [toolcahin](http://code.google.com/p/iphone-dev/wiki/Building)
    * llvm は [revision 42498](https://code.google.com/p/umjammer/source/detail?r=42498) を使用
    * cygwin は[パッチ](http://code.google.com/p/iphone-dev/issues/attachment?aid=-6114562444342259473&name=odcctools_cygwin.patch)が必要

  * .dmg at Windows XP
    * [PowerISO](http://www.poweriso.com/) ~~これしか無理？~~ (ISOBuster ×, dmg2img ×, dmg2iso ×, dmg2iso.pl ×)
      * PowerISO でもシンボリックリンクが普通のファイルになってしまうので MacOSX10.4u.sdk/usr/lib 以下を修正する、他にも include, Frameworks とかいっぱい壊れている
      * iPhone SDK アクセスできひんし...
    * [HFSExplorer](http://hem.bredband.net/catacombae/hfsx.html) フリーであるやんけ！それも一番性能いいし、Java やし
      * シンボリックリンクも問題なし！

### [XCode](http://developer.apple.com/tools/download/) ###
  * .pkg
    * [xar](http://code.google.com/p/xar/)
      * libxml2, libxml2 devel ... cygwin

  * build
> .so -> .dll
> include/config.h comment out acl
    * extract
```
 $ xar -xvf foo.pkg
```
  * Payload
```
 gunzip -c Payload | cpio -i
```
    * cygwin はファイル名 260 までらしいし、XacRett, 7z はうまく動かんし、ったく Windows アプリは...
    * ダメな場合は Linux で
    * ドキュメントの長ーいファイル名は Windows に対する嫌がらせかw
### [iPhone SDK](http://developer.apple.com/iphone/) ###

  * User Agent 偽装で OK

### w/ Mac OS X 10.5.5 ###

|version|code|ipsw|dmg|key|
|:------|:---|:---|:--|:--|
|2.1    |    |    |   |9714f2cb955afa550d6287a1c7dd7bd0efb3c26cf74b948de7c43cf934913df69fc5a05f|

  * マカーになった
  * Mac Book Pro 10.5.5 で最新の iTunes だと QuickPwn1.1, PwnageTool2.1 は JB できない
    * DFU モードになるとエラー 2001 (PwnageTool)、もしくは進まなくなる (QuickPwn)
    * リストアでもエラー 21
    * WinPwn2.1 を素直に使おう

## Runtime Environment ##
### ARM Emulator ###

  * http://www.skyeye.org/index.shtml