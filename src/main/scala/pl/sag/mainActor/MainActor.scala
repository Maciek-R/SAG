package pl.sag.mainActor

import java.io.{File, PrintWriter}

import akka.actor.{Actor, ActorRef, Props}
import pl.sag.Logger._
import pl.sag._
import pl.sag.product.ProductInfo
import pl.sag.subActor.SubActor
import pl.sag.utils.{FileManager, XKomClient}

import scala.collection.{immutable, mutable}


class MainActor extends Actor {

  private var subActors = mutable.Buffer[ActorRef]()
  private val readySubActors = mutable.HashMap[ActorRef, Boolean]()
  private var subActorsCount: Int = 0

  private val bestMatchesFromSubActors = mutable.HashMap[ActorRef, Seq[(ProductInfo, Double)]]()


  override def receive: Receive = {

    case CreateSubActor => createSubActor()
    case RemoveSubActor(index) => removeSubActor(index)
    case CountReadySubActors => countReadyActors()
    case ListSubActors => listSubActors()

    case SubActorIsReady => setSubActorReady()

    case SearchByProductInfo(product: ProductInfo) => searchByProductInfo(product)
    case SearchByStringQuery(text: String) => searchByStringQuery(text)

    case CollectBestMatches(bestMatches) => collectBestMatches(bestMatches)

    case TerminateChildren => subActors.foreach(context.stop)
    case UpdateLocalBaseCategoriesAndProductsLinks => updateLocalBase()
  }

  def createSubActor(): Unit = {
    log(s"Creating subActor$subActorsCount")

    val subActor = context.actorOf(Props[SubActor], "subActor" + subActorsCount)
    subActorsCount += 1

    subActors += subActor
    readySubActors.update(subActor, false)

    subActor ! BuildModel
  }

  def removeSubActor(index: Int): Unit = {
    if (index >= subActors.length || index < 0)
      log(s"Wrong index $index")
    else {
      val subActor = subActors(index)

      readySubActors.remove(subActor)
      subActors.remove(index)

      context.stop(subActor)

      log(s"SubActor ${subActor.path.name} removed")
    }
  }

  def setSubActorReady(): Unit = {
    readySubActors.update(sender, true)

    log(s"SubActor ${sender.path.name} is ready")
  }

  def countReadyActors(): Unit = {
    val readySubActors = getReadySubActors.size

    log(s"Ready $readySubActors of ${subActors.size} actors")
  }

  def listSubActors(): Unit = {
    readySubActors.foreach(p => log(s"${p._1.path.name} is ready: ${p._2}"))
  }

  def searchByProductInfo(product: ProductInfo): Unit = {
    bestMatchesFromSubActors.clear()
    getReadySubActors.foreach(_ ! SearchByProductInfo(product))
  }

  def searchByStringQuery(text: String): Unit = {
    bestMatchesFromSubActors.clear()
    getReadySubActors.foreach(_ ! SearchByStringQuery(text))
  }

  private def getReadySubActors: collection.Set[ActorRef] = {
    readySubActors.filter(_._2).keySet
  }


  def collectBestMatches(bestMatches: Seq[(ProductInfo, Double)]): Unit = {
    bestMatchesFromSubActors += (sender -> bestMatches)
    if (getReadySubActors.size == bestMatchesFromSubActors.size)
      displayBestMatches()
  }

  private def displayBestMatches(): Unit = {
    val bestMatches: immutable.Seq[(ProductInfo, Double)] = bestMatchesFromSubActors.flatMap(_._2).toList
      .sortWith(_._2 > _._2)
      .take(5)

    if (bestMatches.isEmpty)
      log(s"No results found!")
    else
      bestMatches.foreach(p => log(p._1.toString))
  }

  def updateLocalBase(): Unit = {
    val xKomClient = new XKomClient(false)
    createDirectories()
    val writer = new PrintWriter(FileManager.linksFile)
    val categoryToProducts = xKomClient.categoriesLinks.par
      .map(cat => {
        val products = xKomClient.getProductLinks(cat)
        writer.write(cat)
        products.foreach(p => writer.write(" " + p))
        writer.println()
        cat -> products
      })
      .toList.toMap
    writer.close()
    log(s"Saved ${categoryToProducts.size} links to categories and ${categoryToProducts.values.flatten.size} links to products")
  }

  private def createDirectories(): Boolean = {
    new File(FileManager.mainFolder).mkdir()
    new File(FileManager.productsFolder).mkdir()
    new File(FileManager.linksFile).createNewFile()
  }
}
