package controllers

import akka.actor.{ActorSystem, Props}
import javax.inject.{Inject, Singleton}
import pl.sag._
import pl.sag.mainActor.MainActor
import play.api.mvc.{AbstractController, ControllerComponents}
import akka.actor._
import akka.pattern.ask

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import pl.sag.product.{ProductInfo, ProductsInfo}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.Implicits.global._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.language.postfixOps

@Singleton
class MainApplication @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val mainActor = actorSystem.actorOf(Props(new MainActor(4)), "MainActor")

  def hello = Action {Ok("hello")}

  def start = Action {
    implicit val timeout = Timeout(50 seconds)
    val futureProductsInfos: Future[List[ProductsInfo]] = ask(mainActor, StartCollectingDataBlock).mapTo[List[ProductsInfo]]
    val productsInfos = Await.result(futureProductsInfos, timeout.duration)
    Ok("Pobrano: \n" + productsInfos.map(_.toString+"\n").toString)
  }

  def getBestMatches() = Action {
    val tmpUrl = "https://www.x-kom.pl/p/397094-all-in-one-hp-all-in-one-i5-7200u-8gb-240ssd-win10-920mx-fhd.html"
    implicit val timeout = Timeout(50 seconds)
    val futureBestMatches: Future[List[SendBestMatchesToMainActor]] = ask(mainActor, GetBestMatchesBl(tmpUrl)).mapTo[List[SendBestMatchesToMainActor]]
    val bestMatches = Await.result(futureBestMatches, timeout.duration)
    Ok(bestMatches.take(3).flatMap(x=>x.topMatches).map(_._1).toString())
  }

  def check = Action {
    mainActor ! CheckIfGotAllMessages
    Ok("sprawdzanie")
  }

  def updateLocalBase = Action {
    mainActor ! UpdateLocalBaseCategoriesAndProductsLinks
    Ok("Updating...")
  }

  def getProductsInfo = Action {
    implicit val timeout = Timeout(5 seconds)
    val futureProductsInfos: Future[List[ProductInfo]] = ask(mainActor, GetProductsInfo).mapTo[List[ProductInfo]]
    val productsInfos = Await.result(futureProductsInfos, timeout.duration)
    Ok(productsInfos.map(_.toString+"\n").toString)
  }
}
