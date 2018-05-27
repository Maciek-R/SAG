package pl.sag.subActor

import akka.actor.Actor
import pl.sag.product.ProductsInfo
import pl.sag.utils.XKomClient
import pl.sag.{CollectData, SendCollectedProductsInfoToMainActor}

import scala.util.Random

class SubActor extends Actor {
  private val random = new Random
  val xKomClient = new XKomClient

  override def receive: Receive = {
    case CollectData => collectData()
  }

  def collectData() = {
    println(s"SubActor ${self.path.name} started downloading products.")
    val products = xKomClient.downloadRandomProducts(2)
    //    .map(_.copy(description = None))
    println(s"SubActor ${self.path.name} downloaded products.")
    sender ! SendCollectedProductsInfoToMainActor(ProductsInfo(products))
  }
}
