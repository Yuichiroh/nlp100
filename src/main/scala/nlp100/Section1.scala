package nlp100

import scala.io.StdIn
import scala.util.Random

/** Code for 言語処理100本ノック 2015 第1章: Pythonの基礎
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author y-matsu
  *         Created on 2015/02/04.
  */

/** common methods/fields for section 1 */
object Section1 {}

/** 文字列"stressed"の文字を逆に（末尾から先頭に向かって）並べた文字列を得よ． */
object P0 {
  val str = "stressed".reverse

  def main(args: Array[String]): Unit =
    println(str)
}

/** 「パタトクカシーー」という文字列の1,3,5,7文字目を取り出して連結した文字列を得よ． */
object P1 {
  val str = "パタトクカシーー".zipWithIndex.collect { case (c, i) if i % 2 != 0 => c }.mkString

  def main(args: Array[String]): Unit =
    println(str)
}

/** 「パトカー」＋「タクシー」の文字を先頭から交互に連結して文字列「パタトクカシーー」を得よ． */
object P2 {
  val str = ("パトカー" zip "タクシー").map { case (p, t) => s"$p$t" }.mkString

  def main(args: Array[String]): Unit =
    println(str)
}

/** "Now I need a drink, alcoholic of course, after the heavy lectures involving quantum mechanics." 
  * という文を単語に分解し，各単語の（アルファベットの）文字数を先頭から順に並べたリストを作成せよ． */
object P3 {
  val pi = "Now I need a drink, alcoholic of course, after the heavy lectures involving quantum mechanics."
    .split(" ")
    .map(_.count(_.isLetter))
    .mkString

  def main(args: Array[String]): Unit =
    println(pi)
}

/** "Hi He Lied Because Boron Could Not Oxidize Fluorine. New Nations Might Also Sign Peace Security Clause. Arthur King Can."
  * という文を単語に分解し，1, 5, 6, 7, 8, 9, 15, 16, 19番目の単語は先頭の1文字，それ以外の単語は先頭に2文字を取り出し，
  * 取り出した文字列から単語の位置（先頭から何番目の単語か）への連想配列（辞書型）を作成せよ． */
object P4 {
  val rules = Array.fill(20)(2)
  Seq(1, 5, 6, 7, 8, 9, 15, 16, 19).map(_ - 1).foreach(rules(_) = 1)

  val elements =
    "Hi He Lied Because Boron Could Not Oxidize Fluorine. New Nations Might Also Sign Peace Security Clause. Arthur King Can."
      .split(" ").zipWithIndex
      .map(wi => wi._1.substring(0, rules(wi._2)) -> wi._2)
      .toMap

  def main(args: Array[String]): Unit =
    println(elements)
}

/** 与えられたシーケンス（文字列やリストなど）からn-gramを作る関数を作成せよ．
  * この関数を用い，"I am an NLPer"という文から単語bi-gram，文字bi-gramを得よ． */
object P5 {
  def main(args: Array[String]): Unit = {
    println(ngram(2)("I am an NLPer").toList)
    println(ngram(2)("I am an NLPer".split(" ")).toList)
  }

  def ngram[A](n: Int)(xs: Iterable[A]) = xs.sliding(n)
}

/** "paraparaparadise"と"paragraph"に含まれる文字bi-gramの集合を，それぞれ, XとYとして求め，XとYの和集合，積集合，補集合を求めよ．
  * さらに，'se'というbi-gramがXおよびYに含まれるかどうかを調べよ． */
object P6 {
  val charBigram = P5.ngram[Char](2)
  val x = charBigram("paraparaparadise").toSet
  val y = charBigram("paragraph").toSet

  val intersection = x intersect y
  // or x & y
  val union = x union y
  // or x | y
  val diff = x diff y // or x -- y

  def main(args: Array[String]): Unit = {
    println(s"intersection:\t$intersection")
    println(s"union:\t$union")
    println(s"diff:\t$diff")

    println(s"'se' is in X: ${ x.contains("se") }")
    println(s"'se' is in Y: ${ y.contains("se") }")
  }
}

/** 引数x, y, zを受け取り「x時のyはz」という文字列を返す関数を実装せよ．さらに，x=12, y="気温", z=22.4として，実行結果を確認せよ． */
object P7 {
  def main(args: Array[String]): Unit =
    println(atXoclockYisZ("12")("気温")("22.4"))

  def atXoclockYisZ(x: String)(y: String)(z: String) = s"${ x }時の${ y }は${ z }"
}

/** 与えられた文字列の各文字を，以下の仕様で変換する関数cipherを実装せよ．
  - 英小文字ならば(219 - 文字コード)の文字に置換
  - その他の文字はそのまま出力
  * この関数を用い，英語のメッセージを暗号化・復号化せよ． */
object P8 {
  def main(args: Array[String]): Unit = {
    println(cipher("This is a test message."))
    println(cipher("Tsrh rh z gvhg nvhhztv."))
  }

  def cipher(str: String) = str.map {
    case c if c.isLower => (219 - c).toChar
    case c => c
  }
}

/** スペースで区切られた単語列に対して，各単語の先頭と末尾の文字は残し，それ以外の文字の順序をランダムに並び替えるプログラムを作成せよ．
  * ただし，長さが４以下の単語は並び替えないこととする．適当な英語の文（例えば
  * "I couldn't believe that I could actually understand what I was reading : the phenomenal power of the human mind ."）
  * を与え，その実行結果を確認せよ． */
object P9 {
  val randomized = (str: String) => str.split("\\s+").map {
    case w if w.length > 4 => w.head + Random.shuffle(w.substring(1, w.length - 1).toIterator).mkString + w.last
    case w => w
  }.mkString(" ")

  def main(args: Array[String]): Unit =
    println(randomized(StdIn.readLine()))
}
