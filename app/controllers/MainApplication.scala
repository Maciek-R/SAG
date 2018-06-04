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
import pl.sag.product.ProductInfo

import scala.language.postfixOps

@Singleton
class MainApplication @Inject()(cc: ControllerComponents, actorSystem: ActorSystem) extends AbstractController(cc) {

  val mainActor = actorSystem.actorOf(Props(new MainActor(3)), "MainActor")

  def hello = Action {Ok("hello")}

  def start = Action {
    mainActor ! StartCollectingData
    Ok("Zbieranie danych")
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
