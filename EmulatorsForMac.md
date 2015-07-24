# PC #

## PC-8801 ##

### j80 ###
```
 $ java -jar j80.jar cfg/PC-8801mkIISR.cfg
```
| **j80** | **quasi88** | **m88** |
|:--------|:------------|:--------|
|        R\_80    |N88N.ROM     |N80.ROM    |
|        USR0    |N88N.ROM     |N80.ROM    |
|        R\_88    |N88.ROM      |N88.ROM  |
|        R4E0    |N88EXT0.ROM  |N88\_0.ROM  |
|        R4E1    |N88EXT1.ROM  |N88\_1.ROM  |
|        R4E2    |N88EXT2.ROM  |N88\_2.ROM  |
|        R4E3    |N88EXT3.ROM  |N88\_3.ROM  |
|        FONT    |N88KNJ1.ROM  |KANJI1.ROM |
|        JIS1    |N88KNJ1.ROM  |KANJI1.ROM |
|        JIS2    |N88KNJ2.ROM  |KANJI2.ROM |
|        DISK    |N88SUB.ROM   |DISK.ROM   |

`cfg/PC-8801mkIISR.cfg`
```
// separator must be tab!
OPTION  -power  -powdsk -smooth=1       -bus=0  -prn=0  -tape=4

// use line at last
-D88    disk/Foo.D88       1

```

## PC-9801 ##

### NP2 for OSX ###

  * HID Utilities 必要
  * コンパイルはできたが動かん...orz

### xnp2 ###

  * 素直にこちらを使う
  * サウンドとかも OK

### T98next w/ wine ###

  * ~~サウンドが鳴らない？~~
    * wine のバージョンが悪かったみたい