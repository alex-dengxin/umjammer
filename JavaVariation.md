# Java Variation #

### Doja to MIDP2.0 ###

|javax.microedition.midlet.Midlet|com.nttdocomo.ui.IApplication|
|:-------------------------------|:----------------------------|
|Choic                           |ListBox(ListBox.CHECK\_BOX | ListBox.MULTI\_SELECTION)|
|TextField                       |TextBox                      |
|TextField#set/getString         |TextBox#set/getText          |
|STAR                            |Display.KEY\_ASTERISK        |
|NUM#                            |Display.KEY_#_|
|searviceRepaint                 |                             |
|Alart                           |Dialog                       |
|Display.getDispaly(this).setCurrent|Display.setCurrent           |
|Timer                           |com.nttdocomo.util.Timer     |
|TimerTask                       |com.nttdocomo.util.TimerListener|
|Midlet#getGameAction            |Canvas#getKeypadState        |

## android ##

### Info ###
| |Java|android|
|:|:---|:------|
| |java.awt.Image|Bitmap |
| |Graphics|Canvas |
| |Graphics#drawImage|Canvas#drawBitmap|

  * MediaPlayer
|type|status|
|:---|:-----|
|.au |NG    |
|.wav|OK    |
|.mid|OK    |

### はまったところ ###

  * Rect は (x, y, w, h) じゃなくて (l, t, r, b)
  * キー入力のおまじない setFocusable(true);

### はじめてのあんどろいど ###

  * [LodeRunner (オリジナル右)](http://umjammer.googlecode.com/svn/trunk/x-lr/index.html)を移植してみた(左下)
  * ついでに iαppli にも移植してみた(左上)
    * DOJA は android に比べると J2SE に近いね

![http://lh5.ggpht.com/_JchUHfE3WF4/SnAFap3KEcI/AAAAAAAAAGQ/fTDNz5Uw_Lo/s288/android.lr.png](http://lh5.ggpht.com/_JchUHfE3WF4/SnAFap3KEcI/AAAAAAAAAGQ/fTDNz5Uw_Lo/s288/android.lr.png)