package pl.sag.mainActor

import akka.actor.{Actor, ActorRef, Props}
import pl.sag.Logger._
import pl.sag._
import pl.sag.product.ProductsInfo
import pl.sag.subActor.SubActor

import scala.collection.mutable


class MainActor extends Actor {
  private var subActors = mutable.Buffer[ActorRef]()
  private val actorsToProducts = mutable.HashMap[ActorRef, Option[ProductsInfo]]()

  override def receive: Receive = {

    case CreateSubActor => createSubActor()
    case StartCollectingData => startCollectingData()
    case SendCollectedProductsInfoToMainActor(productsInfo) => saveProductsInfo(productsInfo)
    case ShowProductsInfo => showProductsInfo()
    case TerminateChildren => subActors.foreach(context.stop(_))
    case GotAllMessages => isAllDataDownloaded()
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

  def saveProductsInfo(productsInfo: ProductsInfo) = {
    actorsToProducts += (sender -> Some(productsInfo))
  }

  def isAllDataDownloaded() = {
    if (actorsToProducts.size != subActors.size)
      println("MainActor is waiting for data")
    else
      println("MainActor got all data")
  }

  def showProductsInfo() = {
    log("Printing products")
    actorsToProducts.foreach {
      case (actorRef, productsInfo) => log(actorRef.path.name, productsInfo.map(_.productsInfo).toString)
    }

    log("Sorted Data: ", actorsToProducts.flatMap(_._2).flatMap(_.productsInfo).toList.sortBy(_.linkPage).toString)
  }
}
