package pl.sag

import akka.actor.{ActorSystem, Props}
import pl.sag.mainActor.MainActor

import scala.io.StdIn


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props(new MainActor()), "MainActor")

  var line = ""
  do {
    println("1. Stwórz aktora")
    println("2. Usuń aktora(index)")
    println("3. Pokaż ile aktorów jest gotowych")
    println("4. Wylistuj podaktorów")
    println("5. Wyszukaj produkty(wpisz frazę)")
    println("6. Zaktualizuj lokalną bazę linków")
    println("0. Wyjscie")
    print(">>")
    line = StdIn.readLine()
    line match {
      case "1" => mainActor ! CreateSubActor
      case "2" => mainActor ! RemoveSubActor(StdIn.readLine().toInt)
      case "3" => mainActor ! CountReadySubActors
      case "4" => mainActor ! ListSubActors
      case "5" => mainActor ! SearchByStringQuery(StdIn.readLine())
      case "6" => mainActor ! UpdateLocalBaseCategoriesAndProductsLinks
      case _ =>
    }
  } while (line != "quit" && line != "0")

  mainActor ! TerminateChildren
  system.terminate()
  println("System terminated!")
}
