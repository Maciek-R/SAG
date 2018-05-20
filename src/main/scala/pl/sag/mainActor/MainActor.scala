package pl.sag.mainActor

import akka.actor.{Actor, ActorRef, Props}
import pl.sag.Logger._
import pl.sag._
import pl.sag.product.ProductsInfo
import pl.sag.subActor.SubActor

import scala.collection.mutable


class MainActor extends Actor {
  private var subActors = mutable.Buffer[ActorRef]()
  private val actorsToProducts = mutable.HashMap[ActorRef, ProductsInfo]()

  override def receive: Receive = {

    case CreateSubActor => createSubActor()
    case StartCollectingData => startCollectingData()
    case SendCollectedProductsInfo(productsInfo) => saveProductsInfo(productsInfo)
    case ShowProductsInfo => showProductsInfo()
    case s: String => println("String: " + s)
    case n: Int => println("Int: " + n)
  }

  def createSubActor() = {
    println ("Creating subActor: " + "subActor"+subActors.length)
    subActors += context.actorOf(Props[SubActor], "subActor"+subActors.length)
  }

  def startCollectingData() = {
    println("Starting collecting data for " + subActors.length + " subActors.")
    subActors.foreach(_ ! CollectData)
  }

  def saveProductsInfo(productsInfo: Seq[Int]) = {
    actorsToProducts(sender) = ProductsInfo(productsInfo)
  }

  def showProductsInfo() = {
    log("Printing products")
    actorsToProducts.foreach {
      case (actorRef, actorInfo) => log(actorRef.path.name, actorInfo.info.toString)
    }

    log("Sorted Data: ", actorsToProducts.flatMap(_._2.info).toList.sorted.toString)
  }
}
