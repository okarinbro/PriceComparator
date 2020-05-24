import akka.actor.ActorRef


sealed trait Response

case class Query(productName: String)

case class QueryResult(productName: String, price: Double, occurrences: Int) extends Response

case class PriceNotFound(productName: String, occurrences: Int) extends Response

case class FoundPrice(productName: String, price: Double)

case class QueryOrder(productName: String, sender: ActorRef)

case class FindPrice(productName: String)

