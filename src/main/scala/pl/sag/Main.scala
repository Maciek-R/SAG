package pl.sag

import akka.actor.{ActorSystem, Props}
import pl.sag.mainActor.MainActor

import scala.io.StdIn


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props[MainActor], "MainActor")

  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! StartCollectingData

  var line = ""
  do {
    line = StdIn.readLine()
    mainActor ! GotAllMessages
    mainActor ! ShowCurrentLinksToProducts
  } while (line != "quit")

  mainActor ! ShowProductsInfo
  mainActor ! TerminateChildren
  system.terminate()
  println("System terminated!")
}
