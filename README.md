nlp100
======

[言語処理100本ノック 2015](http://www.cl.ecei.tohoku.ac.jp/nlp100/) in Scala

第８セットまで。

想定する閲覧者
-----
* 「Python で一周した。Scalaではどう書くのか知りたい」
* 「自然言語処理については大体分かっている。Scalaを学びたい」
* 「[コップ本](http://www.amazon.co.jp/gp/product/4844330845/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=247&creative=1211&creativeASIN=4844330845&linkCode=as2&tag=ycr-22)を読んでいる（あるいはもう読んだ）。実践的なコードを見ながら学習したい」

言語処理100本ノック 2015 が初めての人は、**まずは自分の得意な言語で一度解いてみる** ことをおすすめします。特にこれから自然言語処理をやりたいという人にとっては、試行錯誤の過程で色々なことが身につく大変良い教材なので、自分でやることに大変な意義があります。

Scalaではこう書くだろう、を密に詰め込んでいます。コードに解説はありません。Scalaをこれから学ぶ、という人は[コップ本](http://www.amazon.co.jp/gp/product/4844330845/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=247&creative=1211&creativeASIN=4844330845&linkCode=as2&tag=ycr-22)を片手に読むと、文法と実践的なコードが結びついて、理解が深まると思います。

How to run codes
----------------

```
cd nlp100

mkdir resources

### put data into the resources dir ###

# for programs depending on external libraries
sbt -mem 2048 'runMain  nlp100.P1'

# for programs not depending on external libraries
sbt compile
scala -cp .:resources:target/scala-2.11/classes nlp100.P1  

# for test
sbt test
```
