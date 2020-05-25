import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class Worker extends Actor {
  implicit val executionContext: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = 300.millisecond

  override def receive: Receive = {
    case msg: QueryOrder =>
      val future: Future[Double] = askForLowerPrice(msg)
      val occurrencesFuture = (context.actorOf(Props[QueryCounter]) ? CountQueryOccurrences(msg.productName))
        .mapTo[QueryOccurrencesResult]
        .map(_.occurrences)

      sendResult(future.zip(occurrencesFuture), msg)
      self ! PoisonPill
  }

  private def sendResult(future: Future[(Double, Int)], msg: QueryOrder): Unit = {
    future.onComplete {
      case Success((res, o)) => msg.sender ! QueryResult(msg.productName, res, o)
      case _ => msg.sender ! PriceNotFound(msg.productName)
    }
  }

  private def askForLowerPrice(msg: QueryOrder): Future[Double] = {
    val firstQuery = askForThePrice(msg)
    val secondQuery = askForThePrice(msg)
    val future: Future[Double] = orderTask(firstQuery, secondQuery)
    future
  }

  private def askForThePrice(msg: QueryOrder): Future[FoundPrice] = {
    (context.actorOf(Props[PriceFinder]) ? FindPrice(msg.productName)).mapTo[FoundPrice]
  }

  private def orderTask(firstQuery: Future[FoundPrice], secondQuery: Future[FoundPrice]): Future[Double] = {
    firstQuery
      .zipWith(secondQuery)((q1, q2) => Seq(q1.price, q2.price).min)
      .fallbackTo(firstQuery.map(_.price))
      .fallbackTo(secondQuery.map(_.price))
  }

}
