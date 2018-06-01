package pl.sag

import akka.actor.{ActorSystem, Props}
import pl.sag.mainActor.MainActor

import scala.io.StdIn


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props(new MainActor(3)), "MainActor")

 // mainActor ! StartCollectingData
  mainActor ! UpdateLocalBaseCategoriesAndProductsLinks

  var line = ""
  do {
    line = StdIn.readLine()
   // mainActor ! GotAllMessages
   // mainActor ! ShowCurrentLinksAndImgsOfProducts
  } while (line != "quit")

  //mainActor ! ShowProductsInfo
  //mainActor ! TerminateChildren
  system.terminate()
  println("System terminated!")
}
