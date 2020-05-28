import java.io.File
import java.nio.file.Paths
import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}

import scala.concurrent.ExecutionContext
import scala.util.Using

class QueryCounter extends Actor with ActorLogging with DatabaseManager {
  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case msg: CountQueryOccurrences =>
      sender() ! QueryOccurrencesResult(getOccurrences(msg.productName))
      self ! PoisonPill
  }


  def getOccurrences(productName: String): Int = {
    val sql: String = "SELECT occurrences FROM Queries WHERE productName = '%s'".format(productName)
    var occurrences = 1
    Using(DriverManager.getConnection(dbURL)) {
      conn =>
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sql)
        if (!rs.isClosed)
          occurrences = rs.getInt(1) + 1
    }
    context.actorOf(Props[DatabaseUpdater]) ! UpdateDatabase(productName, occurrences)
    occurrences
  }
}
