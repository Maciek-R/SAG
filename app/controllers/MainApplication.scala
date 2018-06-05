package controllers

import akka.actor.{ActorSystem, Props, _}
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import pl.sag._
import pl.sag.mainActor.MainActor
import pl.sag.product.ProductInfo
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class MainApplication @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)
                               (implicit exec: ExecutionContext, assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  val mainActor: ActorRef = actorSystem.actorOf(Props[MainActor].withDispatcher("control-aware-dispatcher"), "MainActor")

  var currentProduct: ProductInfo = _
  var bestMatches: List[ProductInfo] = List[ProductInfo]()
  var subActors: List[(String, Boolean)] = List[(String, Boolean)]()

  def index = Action {
    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }

  def create = Action {
    mainActor ! CreateSubActor
    retrieveSubActors()

    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }

  def remove(index: String) = Action {
    mainActor ! RemoveSubActor(index.toInt)
    retrieveSubActors()

    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }

  def refresh = Action {
    retrieveSubActors()

    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }

  private def retrieveSubActors(): Unit = {
    implicit val timeout: Timeout = Timeout(5 seconds)
    val futureSubActors: Future[List[(String, Boolean)]] = ask(mainActor, ListSubActors).mapTo[List[(String, Boolean)]]
    subActors = Await.result(futureSubActors, timeout.duration)
  }

  def searchString(query: Option[String]) = Action {
    implicit val timeout: Timeout = Timeout(10 seconds)
    val futureBestMatches: Future[List[CollectBestMatches]] = ask(mainActor, SearchByStringQuery(query.get)).mapTo[List[CollectBestMatches]]
    val currentLink = if (currentProduct == null) "" else currentProduct.linkPage
    bestMatches = Await.result(futureBestMatches, timeout.duration).flatMap(_.bestMatches)
      .sortWith(_._2 > _._2)
      .map(_._1)
      .filterNot(_.linkPage == currentLink)
      .take(5)

    retrieveSubActors()

    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }

  def setProduct(index: String) = Action {
    currentProduct = bestMatches(index.toInt)

    implicit val timeout: Timeout = Timeout(10 seconds)
    val futureBestMatches: Future[List[CollectBestMatches]] = ask(mainActor, SearchByProductInfo(currentProduct)).mapTo[List[CollectBestMatches]]
    bestMatches = Await.result(futureBestMatches, timeout.duration).flatMap(_.bestMatches)
      .sortWith(_._2 > _._2)
      .map(_._1)
      .filterNot(_.linkPage == currentProduct.linkPage)
      .take(5)

    retrieveSubActors()


    Ok(views.html.index(currentProduct, subActors, bestMatches))
  }
}
