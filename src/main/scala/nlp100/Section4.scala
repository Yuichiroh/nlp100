package nlp100

import org.sameersingh.scalaplot.Implicits._
import org.sameersingh.scalaplot.Style.Color
import org.sameersingh.scalaplot.gnuplot.GnuplotPlotter
import org.sameersingh.scalaplot.{BarChart, BarData, MemBarSeries, MemXYSeries, XYChart, XYData}

import scala.io.Source

/** Code for 言語処理100本ノック 2015 第4章: 形態素解析
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/02/05.
  */

/** 準備：
  * 夏目漱石の小説『吾輩は猫である』の文章（neko.txt）をMeCabを使って形態素解析し，その結果をneko.txt.mecabというファイルに保存せよ．
  * cat path/to/data/neko.txt | mecab > resources/data/neko.txt.mecab
  */

/** common methods/fields for section 4 */
object Section4 {

  val in = Source.fromURL(getClass.getClassLoader.getResource("data/neko.txt.mecab")).getLines()
  val path = getClass.getClassLoader.getResource(".").getPath

  //  case class Morph(surface: String, pos: String, pos1: String, base: String)
  //
  //  object Morph {
  //    def apply(line: String) = {
  //      val Array(surface, pos, pos1, _, _, _, _, base, _*) = line.split("\t|,")
  //      new Morph(surface, pos, pos1, base)
  //    }
  //  }
}

import nlp100.Section4._

/** 形態素解析結果（neko.txt.mecab）を読み込むプログラムを実装せよ．
  * ただし，各形態素は表層形（surface），基本形（base），品詞（pos），品詞細分類1（pos1）をキーとするマッピング型に格納し，
  * 1文を形態素（マッピング型）のリストとして表現せよ．第4章の残りの問題では，ここで作ったプログラムを活用せよ． */
object P30 {
  val mecabSentences = Iterator.continually {
    in.takeWhile(_ != "EOS")
  }.takeWhile(i => in.hasNext)

  //  val sentences = mecabSentences.map(_.map(Morph(_)).toIndexedSeq).toSeq

  val sentences = mecabSentences.map { sentence =>
    sentence.map { line =>
      val Array(surface, pos, pos1, pos2, pos3, cform, ctype, base, _*) = line.split("\t|,")
      Map('surface -> surface, 'base -> base, 'pos -> pos, 'pos1 -> pos1)
    }.toIndexedSeq
  }.toSeq

  def main(args: Array[String]) = sentences.foreach(println)
}

/** 動詞の表層形をすべて抽出せよ． */
object P31 extends App {
  P30.sentences.flatten.collect { case m if m('pos) == "動詞" => m('surface) }
    .distinct.foreach(println)
}

/** 動詞の原形をすべて抽出せよ． */
object P32 extends App {
  P30.sentences.flatten.collect { case m if m('pos) == "動詞" => m('base) }
    .distinct.foreach(println)
}

/* サ変接続の名詞をすべて抽出せよ． */
object P33 extends App {
  P30.sentences.flatten.collect { case m if m('pos1) == "サ変接続" => if (m('base) == "*") m('surface) else m('base) }
    .distinct.foreach(println)
}

/** ２つの名詞が「の」で連結されている名詞句を抽出せよ． */
object P34 extends App {
  P30.sentences.flatMap { s =>
    s.sliding(3).collect {
      case Seq(a, no, b) if b('pos) == "名詞" && no('surface) == "の" && a('pos) == "名詞" =>
        s"${ a('surface) }の${ b('surface) }"
    }
  }.distinct.foreach(println)
}

/** 名詞の連接（連続して出現する名詞）を最長一致で抽出せよ． */
object P35 extends App {
  P30.sentences.flatMap { s =>
    val iter = s.toIterator
    val nps = Iterator.continually(iter.dropWhile(_ ('pos) != "名詞").takeWhile(_ ('pos) == "名詞")).takeWhile(i => iter.hasNext)

    nps.map { np =>
      np.dropWhile(_ ('pos1) == "非自立")
        .map(n => if (n('pos1) == "副詞可能") n('surface) + "|" else n('surface))
        .mkString.split("\\|")
    }.toSeq.flatten
  }.filterNot(_.matches("\\s*")).foreach(println)
}

/** 文章中に出現する単語とその出現頻度を求め，出現頻度の高い順に並べよ． */
object P36 {
  val words = in.collect { case line if line != "EOS" => line.split(",")(6) }
  val wFreqs = P19.freqs(words)

  def main(args: Array[String]): Unit = wFreqs.toSeq.sortWith(_._2 > _._2).foreach(println)
}

/** 出現頻度が高い10語とその出現頻度をグラフで表示せよ． */
object P37 extends App {
  val xy = P36.wFreqs.toSeq.sortWith(_._2 > _._2).take(10)
  val xs = xy.map(_._1)
  val ys = xy.map(_._2.toDouble)

  val series = new MemBarSeries(ys, "word vs. freq")
  series.color = Some(Color.Cyan)

  val data = new BarData((i: Int) => xs(i), Seq(series))
  val chart = new BarChart("most frequent 10 words", data)
  chart.showLegend = true

  val plotter = new GnuplotPlotter(chart)
  plotter.svg(path + "/", "mostFrequent10words")
}

/** 単語の出現頻度のヒストグラム（横軸に出現頻度，縦軸に出現頻度をとる単語の種類数を棒グラフで表したもの）を描け． */
object P38 extends App {
  val xy = P19.freqs(P36.wFreqs.values).toSeq.sorted
  val (xs, ys) = xy.unzip

  val series = new MemXYSeries(xs.map(_.toDouble), ys.map(_.toDouble), "freq vs. #word")
  series.color = Some(Color.Red)

  val data = new XYData(series)
  val chart = new XYChart("frequency distribution", data, x = Axis(label = "#word"), y = Axis(label = "frequency"))
  chart.showLegend = true

  val plotter = new GnuplotPlotter(chart)
  plotter.png(path + "/", "wordFreqDistribution")
  //    val plotter = new JFGraphPlotter(chart)
  //    plotter.gui()
}

/** 単語の出現頻度順位を横軸，その出現頻度を縦軸として，両対数グラフをプロットせよ． */
object P39 extends App {
  val xy = P36.wFreqs.values.toSeq.sortWith(_ > _).zipWithIndex
  val (ys, xs) = xy.unzip

  val series = new MemXYSeries(xs.map(_.toDouble), ys.map(_.toDouble), "freq rank vs. freq")
  series.color = Some(Color.Red)

  val data = new XYData(series)
  val chart = new XYChart("word freq rank vs. freq", data,
                          x = Axis(label = "rank", log = true),
                          y = Axis(label = "frequency", log = true)
  )
  chart.showLegend = true

  val plotter = new GnuplotPlotter(chart)
  plotter.png(path + "/", "wordRankVsFreq")
  //    val plotter = new JFGraphPlotter(chart)
  //    plotter.gui()
}