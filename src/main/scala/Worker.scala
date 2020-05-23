import java.io.File
import java.nio.file.Paths
import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Using}

class Worker extends Actor {
  implicit val timeout: Timeout = 300.milliseconds
  implicit val executionContext: ExecutionContext = context.dispatcher
  val path: String = Paths.get(".").toAbsolutePath + File.separator + "Comparator.db"
  val dbURL = "jdbc:sqlite:" + path

  def insert(conn: Connection, productName: String) = {
    val sql = "INSERT INTO Queries(productName, occurrences) VALUES(?,?)"
    val pstmt = conn.prepareStatement(sql)
    pstmt.setString(1, productName);
    pstmt.setInt(2, 1);
    pstmt.execute();
  }

  def update(conn: Connection, productName: String, occurrences: Int) = {
    val sql = "UPDATE Queries SET occurrences = ? WHERE productName = ?";
    val pstmt = conn.prepareStatement(sql)
    pstmt.setInt(1, occurrences + 1)
    pstmt.setString(2, productName)
    pstmt.executeUpdate();
  }

  def getOccurrences(productName: String): Int = {
    val sql: String = "SELECT occurrences FROM Queries WHERE productName = '%s'".format(productName)
    var occurrences = 0
    Using(DriverManager.getConnection(dbURL)) {
      conn =>
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sql)
        if (rs.isClosed) {
          insert(conn, productName)
        } else {
          occurrences = rs.getInt(1)
          update(conn, productName, occurrences)
        }
    }
    occurrences
  }


  override def receive: Receive = {
    case msg: QueryOrder =>
      val firstQuery = (context.actorOf(Props[PriceFinder]) ? FindPrice(msg.productName)).mapTo[FoundPrice]
      val secondQuery = (context.actorOf(Props[PriceFinder]) ? FindPrice(msg.productName)).mapTo[FoundPrice]
      val future: _root_.scala.concurrent.Future[Double] = orderTask(firstQuery, secondQuery)
      val occurrences = getOccurrences(msg.productName)
      sendResult(future, msg, occurrences)
      self ! PoisonPill
  }

  private def orderTask(firstQuery: Future[FoundPrice], secondQuery: Future[FoundPrice]): Future[Double] = {
    firstQuery
      .zipWith(secondQuery)((q1, q2) => Seq(q1.price, q2.price).min)
      .fallbackTo(firstQuery.map(_.price))
      .fallbackTo(secondQuery.map(_.price))
  }

  private def sendResult(future: Future[Double], msg: QueryOrder, occurrences: Int): Unit = {
    future.onComplete {
      case Success(res) => msg.sender ! QueryResult(msg.productName, res, occurrences)
      case _ => msg.sender ! PriceNotFound(msg.productName, occurrences)
    }
  }
}
