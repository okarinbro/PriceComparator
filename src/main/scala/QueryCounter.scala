import java.io.File
import java.nio.file.Paths
import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext
import scala.util.Using

class QueryCounter extends Actor with ActorLogging with DatabaseManager {
  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case msg: CountQueryOccurrences =>
      sender() ! QueryOccurrencesResult(getOccurrences(msg.productName))
  }

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
    pstmt.setInt(1, occurrences)
    pstmt.setString(2, productName)
    pstmt.executeUpdate();
  }

  def getOccurrences(productName: String): Int = {
    val sql: String = "SELECT occurrences FROM Queries WHERE productName = '%s'".format(productName)
    var occurrences = 1
    Using(DriverManager.getConnection(dbURL)) {
      conn =>
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(sql)
        if (rs.isClosed) {
          insert(conn, productName)
        } else {
          occurrences = rs.getInt(1) + 1
          update(conn, productName, occurrences)
        }
    }
    occurrences
  }
}
