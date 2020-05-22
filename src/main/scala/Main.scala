import akka.actor.{ActorSystem, Props}
import scala.io.StdIn.readLine

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("local_system")

    val server = system.actorOf(Props[Server], "server")
    val client1 = system.actorOf(Client.apply(server), "client1")
    val client2 = system.actorOf(Client.apply(server), "client2")

    while (true) {
      println("Type product name")
      val productName = readLine()
      client1 ! productName
    }
  }
}
