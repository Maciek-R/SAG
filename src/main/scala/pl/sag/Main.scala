package pl.sag

import akka.actor.{ActorSystem, Props}
import pl.sag.mainActor.MainActor

import scala.io.StdIn


object Main extends App {

  val system = ActorSystem("MainSystem")

  val mainActor = system.actorOf(Props(new MainActor(5)), "MainActor")

  var line = ""
  do {
    println("1. Rozpocznij zbieranie danych")
    println("2. Sprawdz czy odebrano wszystkie dane od podaktorów")
    println("3. Pokaż aktualnie otrzymane dane o produktach")
    println("5. Zaktualizuj lokalną bazę linków")
    println("6. Pokaż najlepsze dopasowania(podaj link)")
    println("0. Wyjscie")
    print(">>")
    line = StdIn.readLine()
    line match {
      case "1" => mainActor ! StartCollectingData
      case "2" => mainActor ! CheckIfGotAllMessages
      case "3" => mainActor ! ShowCurrentLinksAndImgsOfProducts
      case "5" => mainActor ! UpdateLocalBaseCategoriesAndProductsLinks
      case "6" => mainActor ! GetBestMatches(StdIn.readLine())
      case _ =>
    }
  } while (line != "quit" && line != "0")

  mainActor ! TerminateChildren
  system.terminate()
  println("System terminated!")
}
