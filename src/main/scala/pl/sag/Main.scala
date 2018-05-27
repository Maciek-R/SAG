package pl.sag

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import pl.sag.mainActor.MainActor

import scala.concurrent.duration._
import scala.io.StdIn


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props[MainActor], "MainActor")

  //implicit val timeout = Timeout(20.seconds)

  mainActor ! "Elo"
  mainActor ! 22
  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! CreateSubActor
  mainActor ! StartCollectingData

  //Thread.sleep(60.seconds.toMillis)
  //println("Time's up")
  var line = ""
  do {
    line = StdIn.readLine()
    mainActor ! GotAllMessages
  } while (line != "quit")

  mainActor ! ShowProductsInfo
  mainActor ! TerminateChildren
  system.terminate()
  println("System terminated!")
}
