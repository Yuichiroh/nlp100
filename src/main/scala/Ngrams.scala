import scala.io.Source

class ScalaNgrams(val chars: Iterator[Char],
                  val n: Int) {
  val history = chars.take(n - 1).toBuffer

  def get = chars.map { c =>
    history.append(c)
    val ngram = history.toList
    history.remove(0)
    ngram
  }
}


object ScalaNgramTest {
  def main(args: Array[String]): Unit = {
    val ngrams = new ScalaNgrams(Source.stdin.iter, args(0).toInt)
    ngrams.get foreach println
  }
}