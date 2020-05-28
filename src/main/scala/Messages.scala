import akka.actor.ActorRef


sealed trait Response

case class Query(productName: String)

case class QueryResult(productName: String, price: Double, occurrences: Int) extends Response

case class PureQueryResult(productName: String, price: Double) extends Response

case class PriceNotFound(productName: String) extends Response

case class QueryOrder(productName: String, sender: ActorRef)

case class FoundPrice(productName: String, price: Double)

case class CountQueryOccurrences(productName: String)

case class QueryOccurrencesResult(occurrences: Int)

case class FindPrice(productName: String)

case class UpdateDatabase(productName: String, occurrencesSoFar: Int)


