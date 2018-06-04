package controllers

import akka.actor.{ActorSystem, Props}
import javax.inject.{Inject, Singleton}
import pl.sag.{CheckIfGotAllMessages, StartCollectingData, UpdateLocalBaseCategoriesAndProductsLinks}
import pl.sag.mainActor.MainActor
import play.api.mvc.{AbstractController, ControllerComponents}

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
}
