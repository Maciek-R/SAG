package pl.sag.subActor

import pl.sag.Logger._
import akka.actor.Actor
import pl.sag.product.ProductsInfo
import pl.sag.text.{IndexedDocuments, Indexer, TextPreprocessor}
import pl.sag.utils.XKomClient
import pl.sag._

import scala.util.Random

class SubActor extends Actor {
  private val random = new Random
  val xKomClient = new XKomClient(true)

  var model: IndexedDocuments = _

  override def receive: Receive = {
    case CollectData => collectData()
    case CollectDataBl => collectDataBl()
    case GetBestMatches(productUrl: String) => getBestMatches(productUrl)
  }

  def collectData() = {
    log(s"SubActor ${self.path.name} started downloading products.")
    val products = xKomClient.downloadRandomProducts(5)

    log(s"SubActor ${self.path.name} downloaded products.")

    model = Indexer.indexDocuments(products.map(p => (p, TextPreprocessor.preprocess(p.description.get))).toIterator)
    log(s"SubActor ${self.path.name} indexed products.")

    sender ! SendCollectedProductsInfoToMainActor(ProductsInfo(products))
  }

  def collectDataBl() = {
    log(s"SubActor ${self.path.name} started downloading products.")
    val products = xKomClient.downloadRandomProducts(15)

    log(s"SubActor ${self.path.name} downloaded products.")

    model = Indexer.indexDocuments(products.map(p => (p, TextPreprocessor.preprocess(p.description.get))).toIterator)
    log(s"SubActor ${self.path.name} indexed products.")

    sender ! ProductsInfo(products)
  }

  def getBestMatches(productUrl: String) = {
    if (model != null) {
      val product = xKomClient.downloadProduct(productUrl)
      val words = TextPreprocessor.preprocess(product.description.get)
      val topMatches = model.search(words, 50)
      sender ! SendBestMatchesToMainActor(topMatches)
    }
  }
}
