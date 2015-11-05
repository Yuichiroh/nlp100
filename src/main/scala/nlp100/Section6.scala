package nlp100

import java.io.{ByteArrayInputStream, File, PrintWriter}
import java.util.Properties

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.XML

/** Code for 言語処理100本ノック 2015 第6章: 英語テキストの処理
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/02/08.
  */

/** common methods/fields for section 6 */
object Section6 {
  val path = getClass.getClassLoader.getResource("data/").getPath
}

/** (. or ; or : or ? or !) → 空白 → 大文字を文の区切りと見なし，入力された文書を1行1文の形式で出力せよ． */
object P50 {
  def main(args: Array[String]): Unit =
    splitIntoSentences(Source.stdin.getLines()) foreach println

  def splitIntoSentences(lines: TraversableOnce[String]) =
    lines.map(_.replaceAll( """([\.;:\?\!]) ([A-Z])""", "$1\n$2"))
}

/** 空白を単語の区切りとみなし，50の出力を1行1単語の形式で出力せよ．ただし，文の区切りでは空行を出力せよ． */
object P51 extends App {
  Source.stdin.getLines().foreach { line =>
    line.split(" ").foreach(println)
    println()
  }
}

/** 51の出力を入力として受け取り，Porterのステミングアルゴリズムを適用し，単語と語幹をタブ区切り形式で出力せよ．
  * Pythonでは，Porterのステミングアルゴリズムの実装としてstemmingモジュールを利用するとよい．
  */
object P52 {

  import org.tartarus.snowball.ext.englishStemmer

  val stemmer = new englishStemmer

  def main(args: Array[String]): Unit = {
    Source.stdin.getLines().map { word =>
      Seq(word, stem(word)).mkString("\t")
    } foreach println
  }

  def stem(word: String) = {
    stemmer.setCurrent(word)
    stemmer.stem
    stemmer.getCurrent
  }
}

/** Stanford Core NLPを用い，入力テキストの解析結果をXML形式で得よ．また，このXMLファイルを読み込み，入力テキストを1行1単語の形式で出力せよ． */
object P53 extends App {

  import edu.stanford.nlp.pipeline._

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref")
  props.setProperty("parse.maxlen", "60")
  val pipeline = new StanfordCoreNLP(props)

  val text = Source.stdin.getLines().mkString(" ")
  val document = new Annotation(text)
  pipeline.annotate(document)

  pipeline.xmlPrint(document, new PrintWriter(System.out))
}

/** Stanford Core NLPの解析結果XMLを読み込み，単語，レンマ，品詞をタブ区切り形式で出力せよ． */
object P54 extends App {
  val root = XML.load(System.in)
  (root \\ "token").map { t =>
    Seq((t \ "word")(0).text, (t \ "lemma")(0).text, (t \ "POS")(0).text).mkString("\t")
  } foreach println
}

/** 入力文中の人名をすべて抜き出せ． */
object P55 extends App {
  val root = XML.load(System.in)
  (root \\ "token").collect {
    case t if (t \ "NER")(0).text == "PERSON" => (t \ "word")(0).text
  } foreach println
}

/** Stanford Core NLPの共参照解析の結果に基づき，文中の参照表現（mention）を代表参照表現（representative mention）に置換せよ．
  * ただし，置換するときは，「代表参照表現（参照表現）」のように，元の参照表現が分かるように配慮せよ．
  */
object P56 extends App {
  val root = XML.load(System.in)
  val sentences = (root \\ "sentences" \ "sentence").map { s =>
    (s \\ "word").map(_.text).toArray
  }.toArray

  val corefs = root \\ "coreference" \ "coreference"
  corefs.foreach { coref =>
    val representative = ((coref \ "mention").filter(m => (m \ "@representative").nonEmpty)(0) \ "text").text
    val otherMentions = (coref \ "mention").filter(m => (m \ "@representative").isEmpty)
    otherMentions.foreach { m =>
      val sentId = (m \ "sentence").text.toInt - 1
      val start = (m \ "start").text.toInt - 1
      val end = (m \ "end").text.toInt - 1
      sentences(sentId)(start) = s"${ Console.RED }$representative (${ sentences(sentId)(start) }"
      sentences(sentId)(end - 1) += s")${ Console.RESET }"
    }
  }
  sentences.map(_.mkString(" ")) foreach println
}

/** Stanford Core NLPの係り受け解析の結果（collapsed-dependencies）を有向グラフとして可視化せよ．
  * 可視化には，係り受け木をDOT言語に変換し，Graphvizを用いるとよい．また，Pythonから有向グラフを直接的に可視化するには，pydotを使うとよい． */
object P57 extends App {
  val deps = (XML.load(System.in) \\ "dependencies")(args(0).toInt) \ "dep"
  val g = new P44.DirectedDotGraph("dependency")

  for (d <- deps) {
    val depType = (d \ "@type")(0).text
    val dependent = (d \ "dependent")(0)
    val governor = (d \ "governor")(0)

    g.addChain(
      Seq(
        s"${ (governor \ "@idx")(0).text }: ${ governor.text }",
        s"${ (dependent \ "@idx")(0).text }: ${ dependent.text }"),
      depType)
  }

  import sys.process._

  "dot -Tsvg" #< new ByteArrayInputStream(g.toDot.getBytes) #> new File(Section6.path + "dep.svg") run ()
  s"open ${ Section6.path }dep.svg" run ()
}

/** Stanford Core NLPの係り受け解析の結果（collapsed-dependencies）に基づき，「主語 述語 目的語」の組をタブ区切り形式で出力せよ．
  * ただし，主語，述語，目的語の定義は以下を参考にせよ．
  * - 述語: nsubj関係とdobj関係の子（dependant）を持つ単語
  * - 主語: 述語からnsubj関係にある子（dependent）
  * - 目的語: 述語からdobj関係にある子（dependent） */
object P58 extends App {
  val sentences = XML.load(System.in) \\ "dependencies"

  for (s <- sentences) {
    val governors = scala.collection.mutable.Map[Int, ArrayBuffer[(String, String, String)]]()

    for (d <- s \ "dep") {
      val depType = (d \ "@type")(0).text
      val governor = (d \ "governor")(0)
      val govId = (governor \ "@idx")(0).text.toInt
      val dependent = (d \ "dependent")(0)

      governors.getOrElseUpdate(govId, ArrayBuffer[(String, String, String)]())
        .append((depType, governor.text, dependent.text))
    }
    for {
      (vid, ds) <- governors
      sbj <- ds.filter(d => d._1 == "nsubj")
      obj <- ds.filter(d => d._1 == "dobj")
    } println(s"${ sbj._3 }\t${ sbj._2 }\t${ obj._3 }")
  }
}

/** Stanford Core NLPの句構造解析の結果（S式）を読み込み，文中のすべての名詞句（NP）を表示せよ．入れ子になっている名詞句もすべて表示すること． */
object P59 {

  import scala.util.parsing.combinator._

  def main(args: Array[String]): Unit = {
    val sentence = (XML.load(System.in) \\ "parse")(args(0).toInt)
    Console.err.println(sentence.text)

    SExpr(sentence.text) match {
      case Some(root) => collectNPs(root).map(_.text) foreach println
      case _ => println("parsing failed")
    }
  }

  def collectNPs(node: Node): List[Nonterminal] = node match {
    case t: Terminal => Nil
    case nt: Nonterminal =>
      if (nt.label == "NP") nt :: nt.children.flatMap(collectNPs)
      else nt.children.flatMap(collectNPs)
  }

  abstract case class Node(label: String) {
    def text: String
  }

  class Nonterminal(label: String, val children: List[Node]) extends Node(label) {
    def text = children.map(_.text).mkString(" ")
  }

  class Terminal(label: String, val word: String) extends Node(label) {
    def text = word
  }

  object SExpr extends RegexParsers {
    def apply(str: String): Option[Nonterminal] = SExpr.parseAll(s, str) match {
      case Success(root, _) => Some(root)
      case _ => None
    }

    def s: Parser[Nonterminal] = nonterminal

    def node: Parser[Node] = terminal | nonterminal

    def nonterminal: Parser[Nonterminal] = "(" ~> label ~ rep(node) <~ ")" map {
      case label ~ children => new Nonterminal(label, children)
    }

    def terminal: Parser[Terminal] = "(" ~> label ~ word <~ ")" map {
      case label ~ word => new Terminal(label, word)
    }

    def label: Parser[String] = """[^\(\) ]+""".r

    def word: Parser[String] = """[^\(\) ]+""".r
  }

}
