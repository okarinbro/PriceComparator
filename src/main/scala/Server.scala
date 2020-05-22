import akka.actor.{Actor, ActorSystem, Props}

class Server extends Actor {
  override def receive: Receive = {
    case message: Query =>
      context.actorOf(Props[Worker]) ! QueryOrder(message.productName, sender)
  }
}