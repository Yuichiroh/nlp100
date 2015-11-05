package nlp100

import argonaut.Argonaut._
import argonaut.CodecJson

import scala.io.Source
import scala.util.parsing.combinator.RegexParsers

/** Code for 言語処理100本ノック 2015 第3章: 正規表現
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/02/04.
  */

/** Wikipediaの各記事を以下のフォーマットで書き出したファイル（wiki-ja-countries.json.gz）がある．
  - １行に１記事の情報がJSON形式で格納される
  - 各行には記事名が"title"キーに，記事本文が"text"キーの辞書オブジェクトに格納され，そのオブジェクトがJSON形式で書き出される
  - ファイル全体はgzipで圧縮される
  * 以下の処理を行うプログラムを作成せよ． */

/** common methods/fields for section 3 */
object Section3 {}

/** Wikipedia記事のJSONファイルを読み込み，「イギリス」に関する記事本文を表示せよ．以降の処理は，この記事本文に対して処理を実行せよ． */
object P20 extends App {

  val articles = Source.stdin.getLines().map(_.decodeOption[Article].orNull)

  case class Article(title: String, text: String)

  object Article {
    implicit def ArticleCodecJson: CodecJson[Article] =
      casecodec2(Article.apply, Article.unapply)("title", "text")
  }

  /** gzcat resources/data/wiki-ja-countries.json.gz | sbt -mem 2048  --error 'set showSuccess := false'  'set outputStrategy := Some(StdoutOutput)' 'runMain  nlp100.P20' > resources/data/uk.txt */
  articles.collect { case article if article.title == "イギリス" => article.text }.foreach(println)
}

/** 記事中でカテゴリ名を宣言している行を抽出せよ． */
object P21 extends App {
  Source.stdin.getLines().filter(_.startsWith("[[Category:")).foreach(println)
}

/** 記事のカテゴリ名を（行単位ではなく名前で）抽出せよ． */
object P22 extends App {
  val pattern = """\[\[Category:(.+)\]\]""".r
  Source.stdin.getLines().collect { case pattern(category) => category }.foreach(println)
}

/** 記事中に含まれるセクション名とそのレベル（例えば"== セクション名 =="ならレベル1）を表示せよ． */
object P23 extends App {
  val pattern = """(.*)==([^=]+)=(.+)""".r
  Source.stdin.getLines().collect { case pattern(head, title, level) => (title, level.length) }.foreach(println)
}

/** 記事から参照されているメディアファイルをすべて抜き出せ． */
object P24 extends App {
  val pattern = """.*\[\[(File|ファイル):([^|]+)\|?.*\]\]""".r
  Source.stdin.getLines().collect { case pattern(tag, file) => file }.foreach(println)
}

/** 記事中に含まれる「基礎情報」テンプレートのフィールド名と値を抽出し，辞書オブジェクトとして格納せよ． */
object P25 {
  val start = """{{基礎情報"""
  val end = """}}"""
  val kv = """(.+)\s+=\s+(.+)""".r
  val info = Source.stdin.getLines()
    .dropWhile(!_.startsWith(start)).takeWhile(_ != end)
    .map(line => if (line.startsWith("|")) "__BOL__" + line else line)
    .mkString.split( """__BOL__\|""")
  val dict = info.collect { case kv(key, value) => (key, value) }.toMap

  def main(args: Array[String]): Unit = dict.foreach(println)

  object SExpr extends RegexParsers {
    def apply(str: String): Option[String] = SExpr.parseAll(node, str) match {
      case Success(root, _) => Some(root)
      case _ => None
    }

    def root: Parser[Seq[String]] = "{{" ~> repsep(node, "|") <~ "}}"

    def node: Parser[String] = "[]".r | "{{.+?}}"
  }

}

/** 25の処理時に，テンプレートの値からMediaWikiの強調マークアップ（弱い強調，強調，強い強調のすべて）を除去してテキストに変換せよ
  * （参考: http://ja.wikipedia.org/wiki/Help:%E6%97%A9%E8%A6%8B%E8%A1%A8 ）． */
object P26 {
  val deleteEm = (line: String) =>
    line.replaceAll( """'''''([^']*)'''''""", "$1")
      .replaceAll( """'''([^']*)'''""", "$1")
      .replaceAll( """''([^']*)''""", "$1")

  val dict = P25.info.collect { case P25.kv(key, value) => (key, deleteEm(value)) }.toMap

  def main(args: Array[String]): Unit = dict.foreach(println)
}

/** 26の処理に加えて，テンプレートの値からMediaWikiの内部リンクマークアップを除去し，テキストに変換せよ
  * （参考: http://ja.wikipedia.org/wiki/Help:%E6%97%A9%E8%A6%8B%E8%A1%A8 ）． */
object P27 {
  val deleteInnerLink = (line: String) =>
    line.replaceAll( """\[\[(?!ファイル:)[^\]]+?#[^\]]+?\|([^\]]*?)\]\]""", "$1")
      .replaceAll( """\[\[(?!ファイル:)[^\]]+?\|([^\]]*?)\]\]""", "$1")
      .replaceAll( """\[\[(?!ファイル:)([^\]]*?)\]\]""", "$1")

  val dict = P25.info.collect { case P25.kv(key, value) => (key, (P26.deleteEm andThen deleteInnerLink) (value)) }.toMap

  def main(args: Array[String]): Unit = dict.foreach(println)
}

/** 27の処理に加えて，テンプレートの値からMediaWikiマークアップを可能な限り除去し，国の基本情報を整形せよ． */
object P28 {
  val deleteLangInfo = (line: String) => line.replaceAll( """\{\{lang\|.+?\|(.+?)\}\}""", "$1")
  val deleteRef = (line: String) => line.replaceAll( """<ref.*?>.+?</ref>""", "")
  val deleteRef2 = (line: String) => line.replaceAll( """<ref.+?/>""", "")
  val deleteBR = (line: String) => line.replaceAll( """<br/>""", "")
  val deleteFile = (line: String) => line.replaceAll( """\[\[ファイル:(.+?)\|.*?\|.*?\]\]""", "$1")

  val dict = P25.info.collect { case P25.kv(key, value) =>
    (key, (P26.deleteEm andThen P27.deleteInnerLink
           andThen deleteLangInfo andThen deleteRef andThen deleteRef2 andThen deleteBR andThen deleteFile) (value))
  }.toMap

  def main(args: Array[String]): Unit = dict.foreach(println)
}

/** テンプレートの内容を利用し，国旗画像のURLを取得せよ． */
object P29 extends App {
  val flag = {
    val txt = P28.dict.getOrElse("国旗画像", "")
    if (txt.contains(":")) txt.split("\\|")(0).split(":")(1).replaceAll(" ", "_")
    else txt.replaceAll(" ", "_")
  }

  val url = "http://ja.wikipedia.org/wiki/File:" + flag

  import scala.sys.process._

  println(url)
  s"open $url".run()
}
