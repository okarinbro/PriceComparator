import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.io.StdIn.readLine
import akka.stream.Materializer

object Main {

  def main(args: Array[String]): Unit = {
    new DatabaseInitializer().initDbIfNotExists()
    implicit val system: ActorSystem = ActorSystem("local")
    implicit val materialize: Materializer = Materializer.createMaterializer(system)
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = 1.seconds

    val server = system.actorOf(Props[Server], "server")
    val httpServer = new HttpServer(server)
    val bindingFuture = Http().bindAndHandle(httpServer.handleRequests, "localhost", 8080)
    val clients = new mutable.HashMap[String, ActorRef]()
    for (i <- 1 to 10) {
      clients += ("" + i -> system.actorOf(Client.apply(server), "client" + i))
    }

    while (true) {
      println("Type [client number from 1 to 10]:[product name]. To exit: 'q'")
      val command = readLine()
      if (command.equals("q")) {
        bindingFuture
          .flatMap(_.unbind())
          .onComplete(_ => {
            system.terminate()
            System.exit(0)
          })
      }
      if (!"^([1-9]|10):.+".r.matches(command)) {
        println("Invalid input")
      } else {
        val params = command.split(":")
        val actorNumber = params.head
        val productName = params.tail.mkString(" ")
        clients(actorNumber) ! productName
      }
    }
  }
}
