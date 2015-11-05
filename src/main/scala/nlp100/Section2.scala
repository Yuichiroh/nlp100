package nlp100

import java.io.PrintWriter

import scala.io.Source

/** Code for 言語処理100本ノック 2015 第2章: UNIXコマンドの基礎
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author y-matsu
  *         Created on 2015/02/04.
  */

/** "hightemp.txt"を入力ファイルとして，プログラムを実行せよ． さらに，同様の処理をUNIXコマンドでも実現せよ． */

/** common methods/fields for section 2 */
object Section2 {

  val path = getClass.getClassLoader.getResource("data/").getPath

  implicit class Using[T <: AutoCloseable](resource: T) {
    def foreach[R](op: T => R): R = {
      try op(resource)
      finally resource.close()
    }
  }

}

/** 行数をカウントせよ．確認にはwcコマンドを用いよ． */
object P10 extends App {
  println(Source.stdin.getLines().length)
}

/** タブ１文字につきスペース１文字に置換せよ．確認にはsedコマンド，trコマンド，もしくはexpandコマンドを用いよ． */
object P11 extends App {
  Source.stdin.getLines().map(_.replaceAll("\t", " ")).foreach(println)
}

/** 各行の１列目だけを抜き出したものをcol1.txtに，２列目だけを抜き出したものをcol2.txtとしてファイルに保存せよ．確認にはcutコマンドを用いよ． */
object P12 extends App {

  import nlp100.Section2._

  for {
    w1 <- new PrintWriter(path + "col1.txt")
    w2 <- new PrintWriter(path + "col2.txt")
  } {
    Source.stdin.getLines().map(_.split("\t")).foreach {
      case Array(col1, col2, _*) =>
        w1.println(col1)
        w2.println(col2)
      case Array(col1) => w1.println(col1)
    }
  }
}

/** 12で作ったcol1.txtとcol2.txtを結合し，元のファイルの１列目と２列目をタブ区切りで並べたテキストファイルを作成せよ．
  * 確認にはpasteコマンドを用いよ． */
object P13 extends App {

  import nlp100.Section2._

  val col1 = Source.fromFile(path + "col1.txt").getLines()
  val col2 = Source.fromFile(path + "col2.txt").getLines()

  col1 zip col2 map (c1c2 => s"${c1c2._1}\t${c1c2._2}") foreach println
}

/** 自然数Nをコマンドライン引数などの手段で受け取り，入力のうち先頭のN行だけを表示せよ．確認にはheadコマンドを用いよ． */
object P14 extends App {
  Source.stdin.getLines().take(args(0).toInt).foreach(println)
}

/** 自然数Nをコマンドライン引数などの手段で受け取り，入力のうち末尾のN行だけを表示せよ．確認にはtailコマンドを用いよ． */
object P15 extends App {
  //  Source.stdin.getLines().toStream.takeRight(args(0).toInt).foreach(println)
  val size = args(0).toInt

  val (start, lines) =
    ((0, new Array[String](size)) /: Source.stdin.getLines()) { (q, line) =>
      val (i, arr) = q
      arr(i) = line
      ((i + 1) % size, arr)
    }

  lines.slice(start, lines.length) ++ lines.slice(0, start) foreach println
}

/** 自然数Nをコマンドライン引数などの手段で受け取り，入力のファイルを行単位でN分割せよ．同様の処理をsplitコマンドで実現せよ． */
object P16 extends App {
  val writers = (1 to args(0).toInt).map(i => new PrintWriter(Section2.path + s"/hightemp-$i.txt"))
  val fid = Iterator.iterate(0)(i => (i + 1) % args(0).toInt)

  Source.stdin.getLines().foreach(line => writers(fid.next()).println(line))
  writers.foreach(_.close)
}

object P16_2 extends App {
  val n = args(0).toInt
  val writers = (1 to n).map(i => new PrintWriter(Section2.path + s"hightemp-$i.txt"))
  val (lines, lines2) = Source.stdin.getLines().duplicate
  val size = lines2.size / n

  (0 until n).foreach(i => lines.take(size).foreach(writers(i).println))
  writers.foreach(_.close)
}

/** １列目の文字列の種類（異なる文字列の集合）を求めよ．確認にはsort, uniqコマンドを用いよ． */
object P17 extends App {
  println(Source.stdin.getLines().map(_.split("\t")(0)).toSet)
}

/** 各行を３コラム目の数値の逆順で整列せよ（注意: 各行の内容は変更せずに並び替えよ）．
  * 確認にはsortコマンドを用いよ（この問題はコマンドで実行した時の結果と合わなくてもよい）． */
object P18 extends App {
  Source.stdin.getLines().toIndexedSeq.sortBy(_.split("\t")(2).toDouble).reverse foreach println
}

/** 各行の１列目の文字列の出現頻度を求め，その高い順に並べて表示せよ．確認にはcut, uniq, sortコマンドを用いよ． */
object P19 {
  def main(args: Array[String]): Unit =
    freqs(Source.stdin.getLines().map(_.split("\t")(0)))
      .toSeq.sortBy(_._2).reverse foreach println

  def freqs[A](items: TraversableOnce[A]) =
    (Map[A, Int]() /: items) { (map, item) => map + (item -> (map.getOrElse(item, 0) + 1)) }
}

/** 各行の１列目の文字列の出現頻度を求め，その高い順に並べて表示せよ．確認にはcut, uniq, sortコマンドを用いよ． */
object P19_2 extends App {
  val freqs = collection.mutable.Map[String, Int]()

  Source.stdin.getLines().foreach { line => freqs(line.split("\t")(0)) += 1 }
  freqs.toSeq.sortBy(_._2).reverse foreach println
}
