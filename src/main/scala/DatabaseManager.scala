import java.io.File
import java.nio.file.Paths

trait DatabaseManager {
  val path: String = Paths.get(".").toAbsolutePath + File.separator + "Comparator.db"
  val dbURL = "jdbc:sqlite:" + path
}
