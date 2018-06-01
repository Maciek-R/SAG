package pl.sag.mainActor

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths}

import akka.actor.{Actor, ActorRef, Props}
import pl.sag.Logger._
import pl.sag._
import pl.sag.product.ProductsInfo
import pl.sag.subActor.SubActor
import pl.sag.utils.XKomClient

import scala.collection.mutable
import scala.io.Source


class MainActor(val numberOfSubActors: Int) extends Actor {

  private var subActors = mutable.Buffer[ActorRef]()
  private val actorsToProducts = mutable.HashMap[ActorRef, Option[ProductsInfo]]()

  override def receive: Receive = {
    case StartCollectingData => startCollectingData()
    case SendCollectedProductsInfoToMainActor(productsInfo) => saveProductsInfo(productsInfo)
    case ShowProductsInfo => showProductsInfo()
    case TerminateChildren => subActors.foreach(context.stop)
    case GotAllMessages => isAllDataDownloaded()
    case ShowCurrentLinksAndImgsOfProducts => showCurrentLinksAndImgsOfProducts()
    case UpdateLocalBaseCategoriesAndProductsLinks => updateLocalBase()
  }

  def createSubActor() = {
    log("Creating subActor" + "subActor" + subActors.length)
    subActors += context.actorOf(Props[SubActor], "subActor" + subActors.length)
  }

  def startCollectingData() = {
    for (_ <- 0 until numberOfSubActors)
      createSubActor()
    log("Starting collecting data for " + subActors.length + " subActors.")
    subActors.foreach(_ ! CollectData)
  }

  def saveProductsInfo(productsInfo: ProductsInfo) = {
    actorsToProducts += (sender -> Some(productsInfo))
  }

  def isAllDataDownloaded() = {
    if (actorsToProducts.size != subActors.size)
      log("MainActor is waiting for data")
    else
    log("MainActor got all data")
  }

  def showProductsInfo() = {
    log("Printing products")
    actorsToProducts.foreach {
      case (actorRef, productsInfo) => log(actorRef.path.name, productsInfo.map(_.productsInfo).toString)
    }

    log("Sorted Data: ", actorsToProducts.flatMap(_._2).flatMap(_.productsInfo).toList.sortBy(_.linkPage).toString)
  }

  def showCurrentLinksAndImgsOfProducts() = {
    actorsToProducts.flatMap(_._2).flatMap(_.productsInfo).map(p => (p.linkPage, p.imageUrl)).foreach(println)
  }

  def updateLocalBase() = {
    createDirectories()

    val writer = new PrintWriter(linksFile)
    val xKomClient = new XKomClient(false)
    val categoryToProducts = xKomClient.categoriesLinks.map(cat => cat -> xKomClient.getProductLinks(cat)).toMap
    categoryToProducts.foreach {case(category, products) => {
        writer.write(category)
        products.foreach(p => writer.write(" " + p))
        writer.println()
      }
    }
    writer.close()
    println(s"Saved ${categoryToProducts.size} links to categories and ${categoryToProducts.values.flatten.size} links to products")
  }

  private def createDirectories() = {
    new File(mainFolder).mkdir()
    new File(productsFolder).mkdir()
  }

  val mainFolder = "XKom/"
  val linksFile = "XKom/Links.txt"
  val productsFolder = "XKom/Products/"
}
