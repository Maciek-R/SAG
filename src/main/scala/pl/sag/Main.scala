package pl.sag

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import pl.sag.mainActor.MainActor

import scala.concurrent.duration._


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props[MainActor], "MainActor")

  implicit val timeout = Timeout(5.seconds)

  mainActor ! "Elo"
  mainActor ! 22
  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! StartCollectingData
  Thread.sleep(3000)
  mainActor ! ShowProductsInfo
  system.terminate()
}
