package pl.sag.subActor

import akka.actor.Actor
import pl.sag.Logger._
import pl.sag._
import pl.sag.product.ProductInfo
import pl.sag.text.{IndexedDocuments, Indexer, TextPreprocessor}
import pl.sag.utils.XKomClient

import scala.util.Random

class SubActor extends Actor {
  private val random = new Random
  val xKomClient = new XKomClient(true)

  val numberOfProducts = 50
  val numberOfBestMatches = 5

  var model: IndexedDocuments = _
  var isReady: Boolean = false

  override def receive: Receive = {
    case BuildModel => buildModel()

    case SearchByProductInfo(product: ProductInfo) => searchByProductInfo(product)
    case SearchByStringQuery(text: String) => searchByStringQuery(text)
  }

  def buildModel(): Unit = {
    log(s"SubActor ${self.path.name} started downloading products.")
    val products = xKomClient.downloadRandomProducts(numberOfProducts)

    log(s"SubActor ${self.path.name} downloaded products.")

    model = Indexer.indexDocuments(products.map(p => (p, TextPreprocessor.preprocess(p.description.get))).toIterator)
    log(s"SubActor ${self.path.name} indexed products.")

    isReady = true

    sender ! SubActorIsReady
  }

  private def searchBestMatches(words: List[String]): Unit = {
    if (isReady)
      sender ! CollectBestMatches(model.search(words, numberOfBestMatches))
  }

  def searchByStringQuery(text: String): Unit = {
    searchBestMatches(TextPreprocessor.preprocess(text))
  }

  def searchByProductInfo(product: ProductInfo): Unit = {
    searchBestMatches(TextPreprocessor.preprocess(product.description.get))
  }
}