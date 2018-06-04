package pl.sag.text

import pl.sag.product.ProductInfo

import scala.collection.mutable.ListBuffer

case class WordVector(private val sentence: Traversable[String], product: Option[ProductInfo]) extends Traversable[(String, Int)] {
  private val wordCountMap: Map[String, Int] = sentence.groupBy(identity).mapValues(_.size)

  val totalWordCount: Int = wordCountMap.values.sum

  def countOccurrencesOf(word: String): Int = wordCountMap.getOrElse(word, 0)

  override def foreach[U](f: ((String, Int)) => U): Unit = wordCountMap.foreach(f)
}

case class TfidfVector(private val data: Map[String, Double], product: Option[ProductInfo]) {
  val length: Double = Math.sqrt(data.values.map(a => a * a).sum)

  def apply(word: String): Double = data.getOrElse(word, 0)

  def commonWordsWith(other: TfidfVector): Set[String] = data.keySet.intersect(other.data.keySet)
}

class IndexedDocuments(documentsSpace: Seq[WordVector], docsContainingWord: Map[String, Set[ProductInfo]]) {
  val docCount: Int = documentsSpace.size
  val documentsAsTfidfSpace: Seq[TfidfVector] = documentsSpace.map(wordVectorToTfIdfVector)

  def wordVectorToTfIdfVector(wordVector: WordVector): TfidfVector = {
    val data: Map[String, Double] = wordVector.map({
      case (word, _) => (word, tfidf(word, wordVector))
    }).toMap

    TfidfVector(data, wordVector.product)
  }

  def tfidf(word: String, wordVector: WordVector): Double = {
    if (documentsSpace.isEmpty) {
      0
    }
    else {
      val occurrencesInDoc: Double = wordVector.countOccurrencesOf(word).toDouble
      val tf = occurrencesInDoc / wordVector.totalWordCount
      val numDocsContainingWord = docsContainingWord.getOrElse(word, Seq.empty).size
      if (numDocsContainingWord == 0) {
        0
      } else {
        val idf = docCount / numDocsContainingWord.toDouble
        val tfidf = tf * Math.log(idf)
        tfidf
      }
    }
  }

  def compareWithQuery(vectorFromUser: TfidfVector)(vectorFromCorpus: TfidfVector): (ProductInfo, Double) = {
    val vec = vectorFromCorpus

    val commonWords = vectorFromUser.commonWordsWith(vec)
    val numerator = commonWords.map(word => vectorFromUser(word) * vec(word)).sum
    val denominator = vectorFromUser.length * vec.length
    (vec.product.get, numerator / denominator)
  }

  def search(sentence: Traversable[String], topN: Int): Seq[(ProductInfo, Double)] = {
    val queryTfidfVector = wordVectorToTfIdfVector(WordVector(sentence, None))
    val scoredDocuments: Seq[(ProductInfo, Double)] = documentsAsTfidfSpace.map(compareWithQuery(queryTfidfVector))
    scoredDocuments
      .sortWith(_._2 > _._2)
      .filterNot(_._2.isNaN)
      .filterNot(_._2 < 0.000001)
      .take(topN)
  }

}

object Indexer {

  def indexDocuments(documents: Iterator[(ProductInfo, List[String])]): IndexedDocuments = {
    import scala.collection.mutable

    val documentsSpace = new ListBuffer[WordVector]
    val docsContainingWord = new mutable.HashMap[String, Set[ProductInfo]]

    documents.foreach {
      case (title, words) =>
        documentsSpace += WordVector(words, Some(title))
        words.foreach { word =>
          val currentDocsWithThisWord = docsContainingWord.getOrElse(word, Set.empty)
          docsContainingWord += word -> (currentDocsWithThisWord + title)
        }
    }

    new IndexedDocuments(documentsSpace.toVector, docsContainingWord.toMap)
  }

}
