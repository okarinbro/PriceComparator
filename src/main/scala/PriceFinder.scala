import akka.actor.{Actor, PoisonPill}
import akka.event.{Logging, LoggingAdapter}

import scala.util.Random

class PriceFinder extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case message: FindPrice =>
      Thread.sleep(new Random().between(100, 500))
      val price = new Random().between(1.0, 10.0)
      sender ! FoundPrice(message.productName, price)
      log.debug("Price of {} : {}. Message has been sent.", message.productName, price.toString)
      self ! PoisonPill
    case _ => println("price finder got message")
  }
}
