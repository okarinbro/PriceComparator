import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Success}
import scala.concurrent.duration._

class Worker extends Actor {
  implicit val timeout: Timeout = 300.milliseconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case msg: QueryOrder =>
      val firstQuery = (context.actorOf(Props[PriceFinder]) ? FindPrice(msg.productName)).mapTo[QueryResult]
      val secondQuery = (context.actorOf(Props[PriceFinder]) ? FindPrice(msg.productName)).mapTo[QueryResult]
      val future: _root_.scala.concurrent.Future[Double] = orderTask(firstQuery, secondQuery)
      sendResult(future, msg)
      self ! PoisonPill
  }

  private def orderTask(firstQuery: Future[QueryResult], secondQuery: Future[QueryResult]): Future[Double] = {
    firstQuery
      .zipWith(secondQuery)((q1, q2) => Seq(q1.price, q2.price).min)
      .fallbackTo(firstQuery.map(_.price))
      .fallbackTo(secondQuery.map(_.price))
  }

  private def sendResult(future: Future[Double], msg: QueryOrder): Unit = {
    future.onComplete {
      case Success(res) => msg.sender ! QueryResult(msg.productName, res)
      case _ => msg.sender ! PriceNotFound(msg.productName)
    }
  }
}
