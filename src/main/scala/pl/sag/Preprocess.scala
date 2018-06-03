package pl.sag

import epic.preprocess.MLSentenceSegmenter

case class StopWordFilter(stopwords : List[String]) {
  def apply(doc : Iterable[String]) =
    doc.filter(word => !stopwords.contains(word.toLowerCase))

  def apply(s: String) = !stopwords.contains(s.toLowerCase)
}

object Preprocess extends App{

  val text = "To jest testowe zdanie. A to drugie zdanie, ale ten sam dokumane."

  val stopwords = List("to", "jest", "a", "ale", "ten", "sam")

  val sentenceSplitter = MLSentenceSegmenter.bundled().get
  val tokenizer = new epic.preprocess.TreebankTokenizer()
  private val stopWordsFilter = StopWordFilter(stopwords)

  val sentences: IndexedSeq[IndexedSeq[String]] = sentenceSplitter(text).map(tokenizer).toIndexedSeq
  for(sentence <- sentences) {
    println(sentence)
  }
}
