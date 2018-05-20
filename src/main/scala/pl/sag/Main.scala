package pl.sag

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props[MainActor], "MainActor")

  implicit val timeout = Timeout(5.seconds)

  mainActor ! "Elo"
  mainActor ! 22
}
