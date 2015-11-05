package nlp100

import java.io.{File, PrintWriter}

import de.bwaldvogel.liblinear.{Feature, FeatureNode, Linear, Parameter, Problem, SolverType}
import nlp100.Section8._
import org.sameersingh.scalaplot.Implicits._
import org.sameersingh.scalaplot.Style.Color
import org.sameersingh.scalaplot.gnuplot.GnuplotPlotter
import org.sameersingh.scalaplot.{MemXYSeries, XYChart, XYData}

import scala.io.{BufferedSource, Source}
import scala.util.Random

/** Code for 言語処理100本ノック 2015 第8章: 機械学習
  * http://www.cl.ecei.tohoku.ac.jp/nlp100/
  * @author Yuichiroh Matsubayashi
  *         Created on 15/03/18.
  */

/** common methods/fields for section 8 */
object Section8 {

  val path = getClass.getClassLoader.getResource("data/rt-polaritydata/").getPath
  val modelFile = path + "model.txt"
  val trainFile = path + "train.txt"
  val featureIndexFile = path + "ft-index.txt"

  implicit class Using[T <: AutoCloseable](resource: T) {
    def foreach[R](op: T => R): R = {
      try op(resource)
      finally resource.close()
    }
  }

  object FeatureIndexer {
    val name2index = collection.mutable.Map[String,Int]()
    val index2name = collection.mutable.Map[Int, String]()
    var count = 0

    def read(src: BufferedSource) = src.getLines().foreach { line =>
      val Array(feature, i) = line.split("\t")
      val index = i.toInt
      name2index.put(feature, index)
      index2name.put(index, feature)
      if (count < index) count = index
    }

    def write(pw: PrintWriter) = for (w <- pw) {
      name2index.foreach { case (feature, index) => w.println(s"$feature\t$index") }
    }

    def getIndex(name: String) = name2index.getOrElseUpdate(name, {
      count += 1
      index2name.put(count, name)
      count
    })

    def getName(index: Int) = index2name.getOrElse(index, "NONE")
  }

}

/** データの入手・整形
  * 文に関する極性分析の正解データを用い，以下の要領で正解データ（sentiment.txt）を作成せよ
  * ．
  * 1. rt-polarity.posの各行の先頭に"+1 "という文字列を追加する（極性ラベル"+1"とスペースに続けて肯定的な文の内容が続く）
  * 2. rt-polarity.negの各行の先頭に"-1 "という文字列を追加する（極性ラベル"-1"とスペースに続けて否定的な文の内容が続く）
  * 3. 上述1と2の内容を結合（concatenate）し，行をランダムに並び替える
  *
  * sentiment.txtを作成したら，正例（肯定的な文）の数と負例（否定的な文）の数を確認せよ． */
object P70 {

  /** preprocessing:
    * nkf --in-place -w resources/data/rt-polaritydata/rt-polarity.pos
    * nkf --in-place -w resources/data/rt-polaritydata/rt-polarity.neg
    */

  def main(args: Array[String]): Unit = {
    val pos = Source.fromFile(path + "rt-polarity.pos").getLines().map("+1 " + _)
    val neg = Source.fromFile(path + "rt-polarity.neg").getLines().map("-1 " + _)

    for (w <- new PrintWriter(path + "sentiment.txt")) {
      Random.shuffle(pos ++ neg) foreach w.println
    }
  }
}

/** ストップワード
  * 英語のストップワードのリスト（ストップリスト）を適当に作成せよ．
  * さらに，引数に与えられた単語（文字列）がストップリストに含まれている場合は真，それ以外は偽を返す関数を実装せよ．
  * さらに，その関数に対するテストを記述せよ． */
object P71 {
  val stopWords = Set("a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the")

  def isStopWord(word: String) = stopWords.contains(word.toLowerCase)
}

/** 素性抽出
  * 極性分析に有用そうな素性を各自で設計し，学習データから素性を抽出せよ．
  * 素性としては，レビューからストップワードを除去し，各単語をステミング処理したものが最低限のベースラインとなるであろう． */
object P72 {
  def main(args: Array[String]): Unit = {
    Source.stdin.getLines().map(line => sentence2features(line.split(" ").drop(1)).mkString(" ")) foreach println
  }

  def sentence2features(sentence: Array[String]) = sentence.filterNot(P71.isStopWord).map(P52.stem).distinct
}

/** 学習
  * 72で抽出した素性を用いて，ロジスティック回帰モデルを学習せよ． */
object P73 {

  /** sbt -mem1024 --error 'set showSuccess := false'  'set outputStrategy := Some(StdoutOutput)' 'runMain nlp100.P73' < resources/data/rt-polaritydata/sentiment.txt > resources/data/rt-polaritydata/model.txt */
  def main(args: Array[String]): Unit = {
    val instances = Source.stdin.getLines().map(instance).toSeq
    val (ys, xs) = instances.unzip
    Console.err.println(instances.size, ys.size, xs.size, FeatureIndexer.count)

    for (w <- new PrintWriter(trainFile)) {
      instances.foreach { l_fs =>
        w.println(s"${ l_fs._1.toInt } ${ l_fs._2.map(f => s"${ f.getIndex }:${ f.getValue }").mkString(" ") }")
      }
    }
    for (w <- new PrintWriter(featureIndexFile)) {
      FeatureIndexer.write(w)
    }

    Linear.disableDebugOutput()
    val problem = Problem.readFromFile(new File(trainFile), 1.0)
    val parameters = new Parameter(SolverType.L2R_LR, 1, 0.01, 0.1)
    val model = Linear.train(problem, parameters)

    for (w <- new PrintWriter(Console.out)) {
      model.save(w)
    }
  }

  def instance(line: String) = {
    val (labels, sentence) = line.split(" ").splitAt(1)
    val label = labels.head.toDouble
    (label, features(sentence))
  }

  def features(sentence: Array[String]): Array[Feature] =
    P72.sentence2features(sentence).map(FeatureIndexer.getIndex).sorted.map(new FeatureNode(_, 1.0))
}

/** 予測
  * 73で学習したロジスティック回帰モデルを用い，与えられた文の極性ラベル（正例なら"+1"，負例なら"-1"）と，
  * その予測確率を計算するプログラムを実装せよ． */
object P74 {
  /** sbt -mem1024 --error 'set showSuccess := false'  'set outputStrategy := Some(StdoutOutput)' 'runMain nlp100.P74' < resources/data/rt-polaritydata/test.txt */
  def main(args: Array[String]): Unit = {
    FeatureIndexer.read(Source.fromFile(featureIndexFile))
    val model = Linear.loadModel(new File(modelFile))

    Source.stdin.getLines().map { line =>
      val x = P73.features(line.split("\\s"))
      val probs = Array.ofDim[Double](2)
      s"${ Linear.predictProbability(model, x, probs) }\t${ probs.mkString(" ") }\t$line"
    } foreach println
  }
}

/** 素性の重み
  * 73で学習したロジスティック回帰モデルの中で，重みの高い素性トップ10と，重みの低い素性トップ10を確認せよ． */

object P75 {
  val indexer = FeatureIndexer

  def main(args: Array[String]): Unit = {
    indexer.read(Source.fromFile(featureIndexFile))
    val model = Linear.loadModel(new File(modelFile))
    val weightsWithIndexSortedByAbs = model.getFeatureWeights.zipWithIndex.sortBy(_._1.abs)
    val worst10 = weightsWithIndexSortedByAbs.take(10)
    val top10 = weightsWithIndexSortedByAbs.takeRight(10).reverse

    println("top 10:")
    top10.map { case (w, i) => s"$w\t${ weightIndex2featureName(i) }" } foreach println
    println("worst 10:")
    worst10.map { case (w, i) => s"$w\t${ weightIndex2featureName(i) }" } foreach println
  }

  def weightIndex2featureName(i: Int) = indexer.getName(i + 1)
}

/** ラベル付け
  * 学習データに対してロジスティック回帰モデルを適用し，正解のラベル，予測されたラベル，予測確率をタブ区切り形式で出力せよ． */
object P76 {
  /** sbt -mem1024 --error 'set showSuccess := false'  'set outputStrategy := Some(StdoutOutput)' 'runMain nlp100.P76' < resources/data/rt-polaritydata/sentiment.txt > resources/data/rt-polaritydata/prediction.txt */
  def main(args: Array[String]): Unit = {
    Linear.disableDebugOutput()
    FeatureIndexer.read(Source.fromFile(featureIndexFile))
    val model = Linear.loadModel(new File(modelFile))

    Source.stdin.getLines().map(P73.instance).map { case (gold, fs) =>
      val probs = Array.ofDim[Double](2)
      val predicted = Linear.predictProbability(model, fs, probs)

      s"$gold\t$predicted\t${ probs.mkString(" ") }"
    } foreach println
  }
}

/** 正解率の計測
  * 76の出力を受け取り，予測の正解率，正例に関する適合率，再現率，F1スコアを求めるプログラムを作成せよ． */
object P77 {
  val printResults = (pp: Int, pn: Int, np: Int, correct: Int, total: Int) => {
    val precision = pp.toDouble / (np + pp)
    val recall = pp.toDouble / (pn + pp)
    val f1 = 2 * precision * recall / (precision + recall)
    val accuracy = correct.toDouble / total

    println(s"$precision\tprecision")
    println(s"$recall\trecall")
    println(s"$f1\tf1 score")
    println(s"$accuracy\taccuracy")
  }

  def main(args: Array[String]): Unit = {
    val results = Source.stdin.getLines().map { line =>
      val Array(goldStr, sysStr, _*) = line.split("\t")
      val gold = goldStr.toDouble.toInt
      val sys = sysStr.toDouble.toInt
      (gold, sys)
    }

    //    val (pp: Int, pn: Int, np: Int, correct: Int, total: Int) = countResults(results)
    //    printResults(pp, pn, np, correct, total)
    printResults tupled countResults(results)
  }

  def countResults(results: TraversableOnce[(Int, Int)]): (Int, Int, Int, Int, Int) = {
    ((0,0,0,0,0) /: results){ case ( (pp, pn, np, c, t), (gold, predict)) =>
      if (gold == 1) {
        if (predict == 1)  (pp + 1, pn, np, c + 1, t + 1)
        else  (pp, pn + 1, np, c, t + 1)
      }
      else if (predict == 1) (pp, pn, np + 1, c, t + 1)
      else (pp, pn, np, c + 1, t + 1)
    }
  }
}

/** 5分割交差検定
  * 76-77の実験では，学習に用いた事例を評価にも用いたため，正当な評価とは言えない．
  * すなわち，分類器が訓練事例を丸暗記する際の性能を評価しており，モデルの汎化性能を測定していない．
  * そこで，5分割交差検定により，極性分類の正解率，適合率，再現率，F1スコアを求めよ． */
object P78 {
  /** sbt -mem 2048 'runMain nlp100.P78' */
  def main(args: Array[String]): Unit = {
    val nFold = if (args.length > 0) args(0).toInt else 5
    val problem = Problem.readFromFile(new File(trainFile), 1.0)
    val parameters = new Parameter(SolverType.L2R_LR, 1, 0.01, 0.1)
    val predicted = Array.ofDim[Double](problem.y.length)

    Linear.disableDebugOutput()
    Linear.crossValidation(problem, parameters, nFold, predicted)

    P77.printResults tupled P77.countResults(problem.y.map(_.toInt) zip predicted.map(_.toInt))
  }
}

/** 適合率-再現率グラフの描画
  * ロジスティック回帰モデルの分類の閾値を変化させることで，適合率-再現率グラフを描画せよ． */
object P79 {
  val calcResults = (pp: Int, pn: Int, np: Int) => {
    val precision = pp.toDouble / (np + pp)
    val recall = pp.toDouble / (pn + pp)

    (precision, recall)
  }

  /** sbt -mem 2048 'runMain nlp100.P79' */
  def main(args: Array[String]): Unit = {
    val nFold = if (args.length > 0) args(0).toInt else 5
    val problem = Problem.readFromFile(new File(trainFile), 1.0)
    val parameters = new Parameter(SolverType.L2R_LR, 1, 0.01, 0.1)

    Linear.disableDebugOutput()
    val predicted = crossValidation(problem, parameters, nFold)

    val (precisions, recalls) = (0.1 to 0.9 by 0.05).map { thres =>
      calcResults tupled countResults(problem.y.map(_.toInt) zip predicted.map(predictWithThres(_, thres)))
    }.unzip

    val series = new MemXYSeries(precisions, recalls, "precision vs. recall")
    series.color = Some(Color.Red)

    val data = new XYData(series)
    val chart = new XYChart("precision-recall curve", data,
                            x = Axis(label = "precision", log = true),
                            y = Axis(label = "recall", log = true)
    )
    chart.showLegend = true

    val plotter = new GnuplotPlotter(chart)
    plotter.png(path, "pr-curve")

    import sys.process._
    s"open ${path}pr-curve.png" run ()
  }

  def predictWithThres(probs: Array[Double], thres: Double) = if (probs.head >= thres) +1 else -1

  def crossValidation(prob: Problem, param: Parameter, nr_fold: Int): Array[Array[Double]] = {
    val l: Int = prob.l
    val target = Array.fill(prob.y.length) { Array.ofDim[Double](l) }
    val nFold = if (nr_fold > l) l else nr_fold

    val perm = Array.range(0, l)
    perm.foreach { i =>
      val j = i + Random.nextInt(l - i)
      swap(perm, i, j)
    }

    val fold_start = Array.ofDim[Int](nFold + 1)
    (0 to nFold).foreach { i =>
      fold_start(i) = i * l / nFold
    }

    (0 until nFold).foreach { i =>
      val begin: Int = fold_start(i)
      val end: Int = fold_start(i + 1)

      val subprob = new Problem
      subprob.bias = prob.bias
      subprob.n = prob.n
      subprob.l = l - (end - begin)
      subprob.x = new Array[Array[Feature]](subprob.l)
      subprob.y = new Array[Double](subprob.l)

      var k = 0
      (0 until begin).foreach { j =>
        subprob.x(k) = prob.x(perm(j))
        subprob.y(k) = prob.y(perm(j))
        k += 1
      }
      (end until l).foreach { j =>
        subprob.x(k) = prob.x(perm(j))
        subprob.y(k) = prob.y(perm(j))
        k += 1
      }

      val submodel = Linear.train(subprob, param)
      (begin until end).foreach { j =>
        Linear.predictProbability(submodel, prob.x(perm(j)), target(perm(j)))
      }
    }
    target
  }

  def swap(array: Array[Int], idxA: Int, idxB: Int) {
    val temp: Int = array(idxA)
    array(idxA) = array(idxB)
    array(idxB) = temp
  }

  def countResults(results: TraversableOnce[(Int, Int)]): (Int, Int, Int) = {
    ((0,0,0) /: results){ case ( (pp, pn, np), (gold, predict)) =>
      if (gold == 1) {
        if (predict == 1)  (pp + 1, pn, np)
        else  (pp, pn + 1, np)
      }
      else if (predict == 1) (pp, pn, np + 1)
      else (pp, pn, np)
    }
  }
}

