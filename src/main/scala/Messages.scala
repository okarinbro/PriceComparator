import java.io.File
import java.nio.file.Paths

import akka.actor.ActorRef

case class Query(productName: String)

case class QueryResult(productName: String, price: Double, occurrences: Int)

case class FoundPrice(productName: String, price: Double)

case class QueryOrder(productName: String, sender: ActorRef)

case class FindPrice(productName: String)

case class PriceNotFound(productName: String, occurrences: Int)
