nlp100
======

[言語処理100本ノック 2015](http://www.cl.ecei.tohoku.ac.jp/nlp100/) in Scala

第８セットまで。第９セット以降の更新予定はありません。

想定する閲覧者
-----
* 「Python で一周した。Scalaではどう書くのか知りたい」
* 「自然言語処理については大体分かっている。Scalaを学びたい」
* 「[コップ本](http://www.amazon.co.jp/gp/product/4844330845/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=247&creative=1211&creativeASIN=4844330845&linkCode=as2&tag=ycr-22)を読んでいる（あるいはもう読んだ）。実践的なコードを見ながら学習したい」

言語処理100本ノック 2015 が初めての人は、**まずは自分の得意な言語で一度解いてみる** ことをおすすめします。特にこれから自然言語処理を学びたいという人にとっては、試行錯誤の過程で色々なことが身につく大変良い教材になっているので、自分でやってみることに大きな意義があると思います。

Scalaではこう書くだろう、を密に詰め込んでいます。コードに解説はありません。Scalaをこれから学ぶ、という人は[コップ本](http://www.amazon.co.jp/gp/product/4844330845/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=247&creative=1211&creativeASIN=4844330845&linkCode=as2&tag=ycr-22)を片手に読むと文法と実践的なコードが結びついて理解が深まるのではないかと思います。

How to run codes
----------------

### Requirements 
* [sbt](http://www.scala-sbt.org/) 
* [Java Runtime Environment](http://www.java.com/ja/)
* [MeCab](http://taku910.github.io/mecab/) (for Section 4)
* [Gnuplot](http://www.gnuplot.info/) (for Section 4)
* [CaboCha](http://taku910.github.io/cabocha/) (for Section 5)
* [Graphviz](http://www.graphviz.org/) (for Section 5 and 6)
* [Redis](http://redis.io/) (for Section 7)
* [MongoDB](https://www.mongodb.org/) (for Section 7)
* [Activator](https://www.typesafe.com/community/core-tools/activator-and-sbt) (for Section 7)
* [Play 2](https://www.playframework.com/) (for Section 7)  

```
cd nlp100

mkdir resources

### put data into the resources dir ###

# for programs depending on external libraries
sbt -mem 2048 'runMain  nlp100.P1'

# for programs not depending on external libraries
sbt compile
scala -cp .:resources:target/scala-2.11/classes nlp100.P1  

# for problem 69
cd p69
activator run

# for test
sbt test
```
