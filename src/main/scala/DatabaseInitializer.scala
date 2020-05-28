import java.io.File
import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager}

class DatabaseInitializer extends DatabaseManager{
  def initDbIfNotExists(): Unit = {
    if (!Files.exists(Paths.get(path))) {
      createDatabase()
      createTable()
    }
  }

  private def createDatabase(): Unit = {
    var connection: Connection = null
    try {
      connection = DriverManager.getConnection(dbURL)
      if (connection != null) {
        val meta = connection.getMetaData
        println("The driver name is " + meta.getDriverName)
        println("A new database has been created.")
      }
    } catch {
      case exception: Exception => println("Couldn't create database due to: {}".format(exception.getMessage))
    } finally {
      if (connection != null) connection.close()
    }
  }

  def createTable(): Unit = {
    val sqlDdl: String = "CREATE TABLE IF NOT EXISTS Queries (\n" +
      "	id integer PRIMARY KEY,\n" +
      "	productName text NOT NULL,\n" +
      "	occurrences integer NOT NULL );"
    try {
      val connection = DriverManager.getConnection(dbURL)
      val statement = connection.createStatement
      try
        statement.execute(sqlDdl)
      catch {
        case e: Exception => println(e.getMessage)
      } finally {
        if (connection != null) connection.close()
        if (statement != null) statement.close()
      }
    }
  }
}
