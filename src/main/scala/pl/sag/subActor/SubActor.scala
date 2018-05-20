package pl.sag.subActor

import akka.actor.Actor
import pl.sag.{CollectData, SendCollectedProductsInfo}

import scala.util.Random

class SubActor extends Actor {
  private val random = new Random

  override def receive: Receive = {
    case CollectData => collectData()
  }

  def collectData() = {
    val numbers = for{
      i <- 0 until 5
    } yield random.nextInt(20)

    sender ! SendCollectedProductsInfo(numbers.toSeq)
  }
}
