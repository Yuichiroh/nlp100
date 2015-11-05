package nlp100

import org.scalatest.{Matchers, WordSpec}

/** @author Yuichiroh Matsubayashi
  *         Created on 15/10/30.
  */
class P71$Test extends WordSpec with Matchers{
  "isStopWord" should {
    "returns true for words: \"about\", \"above\", \"above\", \"across\", \"After\", \"afterwards\"" in {
      P71.isStopWord("about") should be(true)
      P71.isStopWord("above") should be(true)
      P71.isStopWord("across") should be(true)
      P71.isStopWord("After") should be(true)
      P71.isStopWord("afterwards") should be(true)
    }

    """returns false for words: "car", "people", "friend", "Mary", "inn" """ in {
      P71.isStopWord("car") should be(false)
      P71.isStopWord("people") should be(false)
      P71.isStopWord("friend") should be(false)
      P71.isStopWord("Mary") should be(false)
      P71.isStopWord("inn") should be(false)
    }
  }
}
