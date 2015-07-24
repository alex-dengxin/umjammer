# はじめに #

PSN がハックされて、家族のために作ったアカウントがメルアド失効のためあぼーんしてしまった。
サポートに3時間かけて繋がったものの個人情報に誤りがあると言われて無理ですとの一点張り。
3年前くらいにとったアカウント情報なんて覚えてるか、ボケ。
いまどき電話対応のみ<sup>[a1]</sup>なのと、S◯NYのせいでこちらが不都合被っているのに個人情報なのでと一点張りで
客を助けようとする努力を一切しない対応にアタマ来たので GT5 のチート技を晒す。
日本語でまとめたのあまり見つからなかったからね。

# 方法 #

ほとんどの方法は[ここ](http://psx-scene.com/forums/789760-post627.html)から参照しています。

## 前提条件 ##

  1. ファーム 3.55 以前で置いてある PS3(1)
  1. PS3(1) で作ってある PSN アカウントを持ってること <sup>[1]</sup>
  1. もう一台の PS3(2) <sup>[2]</sup>

> `[1]` アカウントアクティベーション<sup>[3]</sup>してないと(2011-06-03 現在)困ること<sup>[4]</sup>があるかも？<br>
<blockquote><code>[2]</code> PS3(1) をアップデートして二度と Jailbreak しないつもりなら要らない<br>
<code>[3]</code> コンテンツかゲームを一回でもダウンロードすればいいみたい<br>
<code>[4]</code> PSN アカウントのメールアドレスが失効してたりすると俺みたいになる<br></blockquote>

<h2>必要なもの</h2>

<ol><li>前提条件<br>
</li><li>CFW <sup>[5]</sup><br>
</li><li><a href='http://www.mediafire.com/?wy3ax75y0z7yqvo'>Signed Ftp Server 1.2</a>
</li><li><a href='http://www.mediafire.com/?ad1zt7h7szg6jd9'>GT5 Cheated Save Data</a>
</li><li><a href='http://www.mediafire.com/?1dehmdy6z75t90m'>OFW 3.55</a>
</li><li><a href='http://filezilla-project.org/'>FileZilla</a> <sup>[6]</sup><br>
</li><li>FileZilla が走る PC<br>
</li><li>スイッチングハブ<br>
</li><li>ネットのケーブル x2</li></ol>

<blockquote><code>[5]</code> なんでもいいと思う、俺は kmeaw 3.55 w/ lv2 patch v9<br>
<code>[6]</code> 他の FTP クライアントだと問題出るかもしれない、いいから FileZilla にしとけ<br></blockquote>

<h2>方法</h2>

<ol><li>PS3(1) で GT5 を起動してセーブしてセーブデータを作る<br>
</li><li>PS3(1) に CFW をインストール<sup>[7]</sup><br>
</li><li>PS3(1) に Signed Ftp Server をインストール<br>
</li><li>PC と PS3(1) をケーブルとハブで接続する<br>
</li><li>PS3(1) の IP アドレスを確認しておく<br>
</li><li>PS3(1) 側で Signed Ftp Server を起動する<br>
</li><li>PC 側で FileZilla を5. のアドレスを使って起動する<sup>[8]</sup><br>
</li><li>PC 側で GT5 Changed Save Data を解凍する<sup>[9]</sup><br>
</li><li>解凍したファイル2つを FileZilla を使って /dev_hdd0/home/oooooxxx/savedata/BCJS30001-GAME に上書き転送する<sup>[10]</sup><br>
</li><li>PS3(1) をリカバリーモードを使用して OFW にもどす<sup>[11]</sup><br>
</li><li>PS3(1) をインターネットに接続して PSN には接続せずに GT5 を起動して、アップデート<sup>[12]</sup>する<br>
</li><li>GT Life に行くとすべてが揃っているｗ<br>
</li><li>GT5 をセーブして終了<br>
</li><li>GT5 のセーブデータを外部記憶装置にコピー<br>
</li><li>PS3(2) で PS3(1) で使用したアカウントと同じ PSN アカウントを持つユーザを作成する<br>
</li><li>作成したアカウントでログインして先の外部記憶装置からセーブデータをコピーしてくる<br>
</li><li>GT5 起動であとは通常通り</li></ol>

<blockquote><code>[7]</code> リカバリーモードでないとダメかもしれない？<br>
<code>[8]</code> ユーザ "FTPD12345"、パスワード空欄、モードはアクティブモード<br>
<code>[9]</code> パスワード "GT5SAVE"<br>
<code>[10]</code> oooooxxx は適当に探してくれ<br>
<code>[11]</code> 必要かどうかわからないけど垢 BAN されるの怖いので <br>
<code>[12]</code> 2011-06-03 現在 Ver. 1.09 <br></blockquote>


<blockquote><code>[a1]</code> 2011-06-04 メールでできるようになったみたい<br>