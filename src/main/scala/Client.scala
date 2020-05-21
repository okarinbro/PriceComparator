import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class Client(server: ActorRef) extends Actor {
  override def receive(): Receive = {
    case productName: String => this.server ! Query(productName)
    case result: QueryResult => println(result.productName, " ", result.price)
  }
}

object Client {
  def apply(server: ActorRef): Props = {
    Props(new Client(server))
  }
}

