import akka.actor.{Actor, ActorRef, Props}

class Client(server: ActorRef) extends Actor {
  override def receive(): Receive = {
    case productName: String => this.server ! Query(productName)
    case result: QueryResult => println("%s: %f $. Query occurred: %d times.".format(result.productName, result.price, result.occurrences))
    case pureResult: PureQueryResult => println("%s: %f $.".format(pureResult.productName, pureResult.price))
    case notFound: PriceNotFound => println("Price for %s not found".format(notFound.productName))
  }
}

object Client {
  def apply(server: ActorRef): Props = {
    Props(new Client(server))
  }
}

