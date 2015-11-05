package nlp100

import org.scalatest.{FlatSpec, Matchers}

/** Section1 Spec
  * Created by y-matsu on 2015/02/09.
  */
class Section1Spec extends FlatSpec with Matchers {
  "P0" should """returns "desserts" """ in {
    P0.str should be("desserts")
  }

  "P1" should """returns "タクシー" """ in {
    P1.str should be("タクシー")
  }

  "P2" should """returns "パタトクカシーー" """ in {
    P2.str should be("パタトクカシーー")
  }

  "P3" should """returns "314159265358979" """ in {
    P3.pi should be("314159265358979")
  }

  "P4.elements" should """maps element symbols to word positions """ in {
    P4.elements.get("H") should be(Some(0))
    P4.elements.get("He") should be(Some(1))
    P4.elements.get("Li") should be(Some(2))
    P4.elements.get("Be") should be(Some(3))
    P4.elements.get("B") should be(Some(4))
    P4.elements.get("C") should be(Some(5))
    P4.elements.get("N") should be(Some(6))
    P4.elements.get("O") should be(Some(7))
    P4.elements.get("F") should be(Some(8))
    P4.elements.get("Ne") should be(Some(9))
    P4.elements.get("Na") should be(Some(10))
    P4.elements.get("Mi") should be(Some(11)) // "Mg" だとうれしい
    P4.elements.get("Al") should be(Some(12))
    P4.elements.get("Si") should be(Some(13))
    P4.elements.get("P") should be(Some(14))
    P4.elements.get("S") should be(Some(15))
    P4.elements.get("Cl") should be(Some(16))
    P4.elements.get("Ar") should be(Some(17))
    P4.elements.get("K") should be(Some(18))
    P4.elements.get("Ca") should be(Some(19))
  }

  "P5" should "create character- and word-bigram for \"I am an NLPer\"" in {
    P5.ngram(2)("I am an NLPer").toList should be(Seq(
      Seq('I', ' '), Seq(' ', 'a'), Seq('a', 'm'), Seq('m', ' '),
      Seq(' ', 'a'), Seq('a', 'n'), Seq('n', ' '), Seq(' ', 'N'),
      Seq('N', 'L'), Seq('L', 'P'), Seq('P', 'e'), Seq('e', 'r')))

    P5.ngram(2)("I am an NLPer".split(" ")).toList should be(Seq(
      Seq("I", "am"),
      Seq("am", "an"),
      Seq("an", "NLPer")))
  }

  "P6" should """finds intersection, union and diff between the character bigrams of "paraparaparadise" and "paragraph" """ in {
    P6.intersection.toSeq.sortWith(_.toString < _.toString) should be(
      Seq(Seq('a', 'p'), Seq('a', 'r'), Seq('p', 'a'), Seq('r', 'a'))
    )
    P6.union.toSeq.sortWith(_.toString < _.toString) should be(
      Seq(
        Seq('a', 'd'), Seq('a', 'g'), Seq('a', 'p'), Seq('a', 'r'),
        Seq('d', 'i'), Seq('g', 'r'), Seq('i', 's'), Seq('p', 'a'),
        Seq('p', 'h'), Seq('r', 'a'), Seq('s', 'e'))
    )
    P6.diff.toSeq.sortWith(_.toString < _.toString) should be(
      Seq(Seq('a', 'd'), Seq('d', 'i'), Seq('i', 's'), Seq('s', 'e'))
    )
  }

  """P7 when x=12, y="気温", z=22.4""" should """generates "12時の気温は22.4" """ in {
    P7.atXoclockYisZ("12")("気温")("22.4") should be("12時の気温は22.4")
  }

  "P8.cipher" should """encrypt "This is a test message." to "Tsrh rh z gvhg nvhhztv." """ in {
    P8.cipher("This is a test message.") should be("Tsrh rh z gvhg nvhhztv.")
  }
  
  it should """decrypt "Tsrh rh z gvhg nvhhztv." to "This is a test message." """ in {
    P8.cipher("Tsrh rh z gvhg nvhhztv.") should be("This is a test message.")
  }

  """P9.randomized("a b  c")""" should """returns "a b c"""" in {
    P9.randomized("a b   c") should be("a b c")
  }

  """P9.randomized("There is a test message")""" should """returns "T...e is a test m.....e"""" in {
    P9.randomized("There is a test message").split("\\s+")(0)(0) should be('T')
    P9.randomized("There is a test message").split("\\s+")(0)(4) should be('e')
    P9.randomized("There is a test message").split("\\s+")(1) should be("is")
    P9.randomized("There is a test message").split("\\s+")(2) should be("a")
    P9.randomized("There is a test message").split("\\s+")(3) should be("test")
    P9.randomized("There is a test message").split("\\s+")(4)(0) should be('m')
    P9.randomized("There is a test message").split("\\s+")(4)(6) should be('e')
    Iterator.continually(P9.randomized("There is a test message").split("\\s+")(0)).take(100).exists(_ startsWith "Tr") should be(true)
    Iterator.continually(P9.randomized("There is a test message").split("\\s+")(4)).take(100).exists(_ startsWith "mg") should be(true)
  }
  
  it should """includes two 'e' and one 'r' in the first token""" in {
    P9.randomized("There is a test message").split("\\s+")(0)(0) should be('T')
    P9.randomized("There is a test message").split("\\s+")(0).filter(_ == 'e').length should be(2)
    P9.randomized("There is a test message").split("\\s+")(0).filter(_ == 'r').length should be(1)
    P9.randomized("There is a test message").split("\\s+")(0)(4) should be('e')
  }
  it should """includes two 'e', two 's', one 'a', and one 'g' in the last token""" in {
    P9.randomized("There is a test message").split("\\s+")(4)(0) should be('m')
    P9.randomized("There is a test message").split("\\s+")(4).filter(_ == 'e').length should be(2)
    P9.randomized("There is a test message").split("\\s+")(4).filter(_ == 's').length should be(2)
    P9.randomized("There is a test message").split("\\s+")(4).filter(_ == 'a').length should be(1)
    P9.randomized("There is a test message").split("\\s+")(4).filter(_ == 'g').length should be(1)
    P9.randomized("There is a test message").split("\\s+")(4)(6) should be('e')
  }
}
