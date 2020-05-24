import akka.actor.{ActorRef, ActorSystem, Props}
import database.DatabaseInitializer

import scala.collection.mutable
import scala.io.StdIn.readLine

object Main {
  def main(args: Array[String]): Unit = {
    new DatabaseInitializer().initDbIfNotExists()
    implicit val system: ActorSystem = ActorSystem("local_system")

    val server = system.actorOf(Props[Server], "server")
    val clients = new mutable.HashMap[String, ActorRef]()
    for (i <- 1 to 10) {
      clients += ("" + i -> system.actorOf(Client.apply(server), "client" + i))
    }

    while (true) {
      println("Type [client number from 1 to 10]:[product name]. To exit: 'q'")
      val command = readLine()
      if (command.equals("q")) {
        System.exit(0)
      }
      if (!"[1-10]:.+".r.matches(command)) {
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
