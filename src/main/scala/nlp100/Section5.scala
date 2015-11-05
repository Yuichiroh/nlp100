package nlp100

import java.io.{ByteArrayInputStream, File}

import nlp100.Section5._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/** Code for 言語処理100本ノック 2015 第5章: 係り受け解析
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/02/05.
  */

/** 準備：
  * 夏目漱石の小説『吾輩は猫である』の文章（neko.txt）をCaboChaを使って係り受け解析し，その結果をneko.txt.cabochaというファイルに保存せよ．
  * {{{
  * cat path/to/data/neko.txt | cabocha -f1 > resources/data/neko.txt.cabocha
  * }}}
  */

/** common methods/fields for section 5 */
object Section5 {

  import scala.annotation.tailrec

  val path = getClass.getClassLoader.getResource("data/").getPath
  val in = Source.fromURL(getClass.getClassLoader.getResource("data/neko.txt.cabocha")).getLines().buffered

  case class Morph(surface: String, base: String, pos: String, pos1: String)

  case class Chunk(morphs: ArrayBuffer[Morph] = ArrayBuffer(),
                   dst: Int,
                   srcs: ArrayBuffer[Int] = ArrayBuffer(),
                   sentence: ArrayBuffer[Chunk]) {
    def surfaceWithoutSymbols =
      morphs.collect { case m if m.pos != "記号" => m.surface }.mkString

    def surface = morphs.map(_.surface).mkString

    def pathToRoot = linearPathTo(sentence.last)

    @tailrec
    final def linearPathTo(c: Chunk, path: List[Chunk] = Nil): List[Chunk] = {
      if (this eq c) (this :: path).reverse
      else head match {
        case Some(h) => h.linearPathTo(c, this :: path)
        case None => (this :: path).reverse
      }
    }

    def deps = srcs.map(sentence(_))

    def head = dst match {
      case -1 => None
      case _ => Some(sentence(dst))
    }

    override def toString = s"Chunk(${ morphs.map(_.surface).mkString }, $dst, $srcs)"
  }

  object Morph {
    def apply(line: String): Morph = {
      val Array(surface, pos, pos1, _, _, _, _, base, _*) = line.split("\t|,")
      new Morph(surface, base, pos, pos1)
    }
  }

  object Chunk {
    def apply(info: String, s: ArrayBuffer[Chunk]) = {
      val Array(_, _, dep, _*) = info.split(" ")
      new Chunk(dst = dep.dropRight(1).toInt, sentence = s)
    }
  }

}

/** 形態素を表すクラスMorphを実装せよ．
  * このクラスは表層形（surface），基本形（base），品詞（pos），品詞細分類1（pos1）をメンバ変数に持つこととする．
  * さらに，CaboChaの解析結果（neko.txt.cabocha）を読み込み，各文をMorphオブジェクトのリストとして表現し，3文目の形態素列を表示せよ． */
object P40 extends App {
  val sentences = Iterator.continually {
    in.takeWhile(_ != "EOS")
      .filterNot(line => line.startsWith("* ") || line == "EOS")
      .map(Morph(_)).toIndexedSeq
  }.takeWhile(i => in.hasNext)

  sentences.drop(args(0).toInt).next() foreach println
}

/** 文節を表すクラスChunkを実装せよ．
  * このクラスは形態素（Morphオブジェクト）のリスト（morphs），係り先文節インデックス番号（dst），係り元文節インデックス番号のリスト
  * （srcs）をメンバ変数に持つこととする．さらに，入力テキストのCaboChaの解析結果を読み込み，１文をChunkオブジェクトのリストとして表現し，
  * 8文目の文節の文字列と係り先を表示せよ． */
object P41 {
  val sentences = Iterator.continually(parseSentence(in)).takeWhile(i => in.hasNext)

  def parseSentence(cabocha: Iterator[String]) = {
    val sentence = ArrayBuffer[Chunk]()
    var chunk: Chunk = null

    cabocha.takeWhile(_ != "EOS").foreach {
      case chk if chk.startsWith("* ") =>
        chunk = Chunk(chk, sentence)
        sentence += chunk
      case mph if mph.nonEmpty => chunk.morphs += Morph(mph)
    }

    (0 until sentence.size - 1).foreach { i => sentence(sentence(i).dst).srcs += i }
    sentence.toArray
  }

  def main(args: Array[String]): Unit =
    sentences.drop(args(0).toInt).next().zipWithIndex.foreach { ci =>
      println(s"${ ci._2 }: ${ ci._1.surface }\t${ ci._1.dst }")
    }
}

/** 係り元の文節と係り先の文節の表現をタブ区切り形式ですべて抽出せよ．ただし，句読点などの記号は出力しないようにせよ． */
object P42 extends App {
  for {
    sentence <- P41.sentences
    chunk <- sentence
  } println(s"${ chunk.surfaceWithoutSymbols }\t${ headStr(chunk) }")

  def headStr(chunk: Chunk) = chunk.head match {
    case Some(dst) => dst.surfaceWithoutSymbols
    case None => ""
  }
}

/** 名詞を含む文節が，動詞を含む文節に係るとき，これらをタブ区切り形式で抽出せよ． */
object P43 extends App {
  for {
    s <- P41.sentences
    nc <- s if nc.dst != -1 && nc.morphs.exists(_.pos == "名詞")
    vc = s(nc.dst) if vc.morphs.exists(_.pos == "動詞")
  } {
    println(s"${ nc.surfaceWithoutSymbols }\t${ vc.surfaceWithoutSymbols }")
  }
}

/** 与えられた文の係り受け木を有向グラフとして可視化せよ．
  * 可視化には，係り受け木をDOT言語に変換し，Graphvizを用いるとよい．また，Pythonから有向グラフを直接的に可視化するには，pydotを使うとよい． */
object P44 {
  def main(args: Array[String]): Unit = {
    val sentence = P41.sentences.drop(args(0).toInt).next()
    val g = new DirectedDotGraph("dependency")
    sentence.foreach { c =>
      if (c.dst != -1)
        g.addChain(Seq(c.surfaceWithoutSymbols, sentence(c.dst).surfaceWithoutSymbols))
    }

    import sys.process._
    "dot -Tsvg" #< new ByteArrayInputStream(g.toDot.getBytes) #> new File(path + "neko.svg") run ()
    s"open ${path}neko.svg" run ()
  }

  class DirectedDotGraph(val name: String) {
    val chains = ArrayBuffer[String]()

    def toDot = (s"digraph ${ name } {" +: chains :+ "}").mkString("\n")

    def addChain(chain: Seq[String]): Unit = chains += chain.map("\"" + _ + "\"").mkString("", " -> ", ";")

    def addChain(chain: Seq[String], label: String): Unit = chains += chain.map("\"" + _ + "\"").mkString("", " -> ", s""" [label="$label"];""")
  }

}

/** 今回用いている文章をコーパスと見なし，日本語の述語が取りうる格を調査したい． 
  * 動詞を述語，動詞に係っている文節の助詞を格と考え，述語と格をタブ区切り形式で出力せよ． ただし，出力は以下の仕様を満たすようにせよ．
  - 動詞を含む文節において，最左の動詞の基本形を述語とする
  - 述語に係る助詞を格とする
  - 述語に係る助詞（文節）が複数あるときは，すべての助詞をスペース区切りで辞書順に並べる
  * 「吾輩はここで始めて人間というものを見た」という例文を考える．この文は「始める」と「見る」の２つの動詞を含み，
  * 「始める」に係る文節は「ここで」，「見る」に係る文節は「吾輩は」と「ものを」と解析された場合は，次のような出力になるはずである．
  * {{{
  * 始める  で
  * 見る    は を
  * }}}
  * このプログラムの出力をファイルに保存し，以下の事項をUNIXコマンドを用いて確認せよ．
  - コーパス中で頻出する述語と格パターンの組み合わせ
  - 「する」「見る」「与える」という動詞の格パターン（コーパス中で出現頻度の高い順）
  */
object P45 {
  def main(args: Array[String]): Unit = {
    verbClauses.foreach { case (c, v) =>
      val caseMarkers = c.deps.map(casemarker).filterNot(_ == "").sorted.mkString(" ")
      println(s"${ v.base }\t$caseMarkers")
    }
  }

  def verbClauses = for {
    s <- P41.sentences
    c <- s
    v <- c.morphs.reverseIterator.find(_.pos == "動詞")
  } yield (c, v)

  def casemarker(c: Chunk) = c.morphs.reverse.takeWhile(_.pos == "助詞").map(_.surface).reverse.mkString
}

/** 45のプログラムを改変し，述語と格パターンに続けて項（述語に係っている文節そのもの）をタブ区切り形式で出力せよ．
  * 45の仕様に加えて，以下の仕様を満たすようにせよ．項は述語に係っている文節の単語列とする（末尾の助詞を取り除く必要はない）
  - 述語に係る文節が複数あるときは，助詞と同一の基準・順序でスペース区切りで並べる
  * 「吾輩はここで始めて人間というものを見た」という例文を考える． この文は「始める」と「見る」の２つの動詞を含み，
  * 「始める」に係る文節は「ここで」，「見る」に係る文節は「吾輩は」と「ものを」と解析された場合は，次のような出力になるはずである．
  * {{{
  * 始める  で      ここで
  * 見る    は を   吾輩は ものを 
  * }}} */
object P46 extends App {
  P45.verbClauses.foreach { case (c, v) =>
    val args = c.deps.filter(_.morphs.last.pos == "助詞")
    val caseMarkers = args.map(P45.casemarker)
    val (sortedArgs, sortedCaseMarkers) = (args zip caseMarkers).sortBy(_._2).unzip
    val argsStr = sortedArgs.map(_.surfaceWithoutSymbols).mkString(" ")

    println(s"${ v.base }\t${ sortedCaseMarkers.mkString(" ") }\t${ argsStr }")
  }
}

/** 動詞のヲ格にサ変接続名詞が入っている場合のみに着目したい．46のプログラムを以下の仕様を満たすように改変せよ．
  - 「サ変接続名詞+を（助詞）」で構成される文節が動詞に係る場合のみを対象とする
  - 述語は「サ変接続名詞+を+動詞の基本形」とし，文節中に複数の動詞があるときは，最左の動詞を用いる
  - 述語に係る助詞（文節）が複数あるときは，すべての助詞をスペース区切りで辞書順に並べる
  - 述語に係る文節が複数ある場合は，すべての項をスペース区切りで並べる（助詞の並び順と揃えよ）
  * 例えば「別段くるにも及ばんさと、主人は手紙に返事をする。」という文から，以下の出力が得られるはずである．
  * {{{
  * 返事をする      と に は        及ばんさと 手紙に 主人は
  * }}}
  * このプログラムの出力をファイルに保存し，以下の事項をUNIXコマンドを用いて確認せよ．
  - コーパス中で頻出する述語（サ変接続名詞+を+動詞）
  - コーパス中で頻出する述語と助詞パターン
  */
object P47 extends App {
  for {
    (vc, v) <- P45.verbClauses
    args = vc.deps.filter(_.morphs.last.pos == "助詞")
    sahenWo <- args.filter(c => c.morphs.last.base == "を" && c.morphs.reverse.tail.forall(_.pos1 == "サ変接続"))
  } {
    val argsExcl = args.filterNot(_ eq sahenWo)
    val caseMarkers = argsExcl.map(P45.casemarker)
    val (sortedArgsExcl, sortedCaseMarkers) = (argsExcl zip caseMarkers).sortBy(_._2).unzip
    val argsExclStr = sortedArgsExcl.map(_.surfaceWithoutSymbols).mkString(" ")

    println(s"${ sahenWo.surfaceWithoutSymbols + v.base }\t${ sortedCaseMarkers.mkString(" ") }\t${ argsExclStr }")
  }
}

/** 文中のすべての名詞を含む文節に対し，その文節から構文木の根に至るパスを抽出せよ． ただし，構文木上のパスは以下の仕様を満たすものとする．
  * {{{
  * - 各文節は（表層形の）形態素列で表現する
  * - パスの開始文節から終了文節に至るまで，各文節の表現を"->"で連結する
  * }}} */
object P48 extends App {
  for {
    s <- P41.sentences
    c <- s if c.morphs.exists(_.pos == "名詞")
  } {
    Console.err.println(c.surface, s.map(_.surface).mkString)
    println(c.pathToRoot.map(_.surfaceWithoutSymbols).mkString("->"))
  }
}

/** 文中のすべての名詞句のペアを結ぶ最短係り受けパスを抽出せよ．
  * ただし，名詞句ペアの文節番号がiとj（i<j）のとき，係り受けパスは以下の仕様を満たすものとする．
  - 問題48と同様に，パスは開始文節から終了文節に至るまでの各文節の表現（表層形の形態素列）を"->"で連結して表現する
  - 文節iとjに含まれる名詞句はそれぞれ，XとYに置換する
  *
  * また，係り受けパスの形状は，以下の2通りが考えられる．
  - 文節iから構文木の根に至る経路上に文節jが存在する場合: 文節iから文節jのパスを表示
  - 上記以外で，文節iと文節jから構文木の根に至る経路上で共通の文節kで交わる場合:
    文節iから文節kに至る直前のパスと文節jから文節kに至る直前までのパス，文節kの内容を"|"で連結して表示
  *
  * 例えば，「吾輩はここで始めて人間というものを見た。」という文（neko.txt.cabochaの8文目）から，次のような出力が得られるはずである．
  * {{{
  * Xは | Yで -> 始めて -> 人間という -> ものを | 見た
  * Xは | Yという -> ものを | 見た
  * Xは | Yを | 見た
  * Xで -> 始めて -> Y
  * Xで -> 始めて -> 人間という -> Y
  * Xという -> Y
  * }}} */
object P49 extends App {

  import scala.annotation.tailrec

  @tailrec
  def path(from: Int)(to: Int)(sentence: Array[Chunk], pathFrom: List[Chunk] = Nil, pathTo: List[Chunk] = Nil)
  : Path = {
    if (from < to) path(sentence(from).dst)(to)(sentence, sentence(from) :: pathFrom, pathTo)
    else if (to < from) path(from)(sentence(to).dst)(sentence, pathFrom, sentence(to) :: pathTo)
    else Path(pathFrom.reverse, sentence(to), pathTo.reverse)
  }

  case class Path(pathFrom: List[Chunk], nca: Chunk, pathTo: List[Chunk]) {
    override def toString = {
      val from =
        if (pathFrom.isEmpty) ""
        else pathFrom.map(_.surfaceWithoutSymbols).mkString("->") + " -> "
      val to =
        if (pathTo.isEmpty) ""
        else " | " + pathTo.map(_.surfaceWithoutSymbols).mkString(" -> ") + " | "
      from + to + nca.surface
    }
  }

  for {
    s <- P41.sentences
    ncIds = s.indices.filter(i => s(i).morphs.exists(_.pos == "名詞"))
    Seq(nc1, nc2) <- ncIds.combinations(2)
  } {
    Console.err.println(s(nc1).surfaceWithoutSymbols, s(nc2).surfaceWithoutSymbols, s.map(_.surface).mkString)
    println(path(nc1)(nc2)(s))
  }
}