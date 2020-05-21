import akka.actor.Actor

import scala.util.Random

class Worker extends Actor {
  override def receive: Receive = {
    case msg: QueryOrder => {
      Thread.sleep(300)
      msg.sender ! QueryResult(msg.productName, Math.abs(Random.nextDouble() + 1))
    }
  }
}
