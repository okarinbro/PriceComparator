package database

import java.io.File
import java.nio.file.Paths
import java.sql.DriverManager

class DatabaseInitializer {
  def createNewDatabase(): Unit = {
    try {
      val dbURL = "jdbc:sqlite:" + Paths.get(".").toAbsolutePath + File.separator + "Comparator.db"
      val connection = DriverManager.getConnection(dbURL)
      if (connection != null) {
        val meta = connection.getMetaData
        println("The driver name is " + meta.getDriverName)
        println("A new database has been created.")
      }
    } catch {
      case exception: Exception => println("Couldn't create database due to: {}".format(exception.getMessage))
    }
  }
}
