import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, PoisonPill}

import scala.util.Using

class DatabaseUpdater extends Actor with ActorLogging with DatabaseManager {
  override def receive: Receive = {
    case msg: UpdateDatabase =>
      Using(DriverManager.getConnection(dbURL)) {
        conn =>
          msg.occurrencesSoFar match {
            case 1 => insert(conn, msg.productName)
            case _ => update(conn, msg.productName, msg.occurrencesSoFar)
          }
      }
      self ! PoisonPill
  }

  def insert(conn: Connection, productName: String) = {
    val sql = "INSERT INTO Queries(productName, occurrences) VALUES(?,?)"
    val pstmt = conn.prepareStatement(sql)
    pstmt.setString(1, productName);
    pstmt.setInt(2, 1);
    pstmt.execute();
    log.debug(productName + " inserted")
  }

  def update(conn: Connection, productName: String, occurrences: Int) = {
    val sql = "UPDATE Queries SET occurrences = ? WHERE productName = ?";
    val pstmt = conn.prepareStatement(sql)
    pstmt.setInt(1, occurrences)
    pstmt.setString(2, productName)
    pstmt.executeUpdate();
    log.debug(productName + " updated")

  }
}
