import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class Client(server: ActorRef) extends Actor {
  override def receive(): Receive = {
    case productName: String => this.server ! Query(productName); println("client")
    case result: QueryResult => println(result.productName, " ", result.price, " ", result.occurrences)
    case notFound: PriceNotFound => println("Price for %s not found. Your query occurrences: %d".format(notFound.productName, notFound.occurrences))
  }
}

object Client {
  def apply(server: ActorRef): Props = {
    Props(new Client(server))
  }
}

