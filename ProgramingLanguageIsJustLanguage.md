# プログラムはやっぱり言語 #


---


オブジェクト指向設計を習い，そのレビューを社内でやったとき俺なりの考えちょっといれて評判が良かったものです．

## プログラムは文章として読めなければならない ##
次章にその実例を挙げる．
## お題 ##
以下の文をシステムとして捉えオブジェクト指向言語で表現せよ

> _The quick brown fox jumps over the lazy dog._

良くある例じゃん．狐は敏捷性と色のプロパティを持っててジャンプする能力があると．犬は敏捷性のプロパティか．．．
```
 class Fox {
  field Color
  field Agilely
 
  method jumpsOver(Animal) {
   ...
  }
 }
 
 class Dog {
  field Agility
 }
```
まあこんなものか．さあ一般化するか．狐と犬は両方動物だからくくって，プロパティも敏捷性と色両方くくっても OK．
```
 class Animal {
  field Color
  field Agility
 }
 
 class Fox extends Animal {
  Fox(color, agility) {
   Color = color
   Agility = agility
  }
 
  method jumpsOver(Animal) {
   ...
  }
 }
 
 class Dog extends Animal {
  Dog(agility) {
   Agility = agility
  }
 }
```
こんな感じかな？これをもとにシステムを記述すると．．．
```
  theQuickBrownFox = new Fox("brown", "quick")
  theLazyDog = new Dog("lazy")
 
  theQuickBrownFox.jumpsOver(theLazyDog)
```
ねっ！うまく設計するとプログラムが文章になるでしょ？
ハンガリアン表記なんてクソ以外の何者でもないです．

英語ってつくづくコンピュータと相性いいですね．日本語だとこうはいかん．
```
「すばしっこい茶色い狐がだらけた犬を飛び越える．」 

 すばしっこい茶色い狐.飛び越える(だらけた犬)
```
うーん．．．

---


2002/06/03