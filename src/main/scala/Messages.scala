import akka.actor.ActorRef

case class Query(productName: String)

case class QueryResult(productName: String, price: Double)

case class QueryOrder(productName: String, sender: ActorRef)

