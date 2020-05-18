//#full-example
import Client.ComparisonResult
import Comparator.AskForComparison
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object Worker {

  final case class Compare(productName: String, replyTo: ActorRef[ComparisonResult])

  def apply(): Behavior[Compare] = Behaviors.receive { (context, message) =>
    context.log.info("Doing job...")
    Thread.sleep(1000)
    message.replyTo ! ComparisonResult(message.productName, 29.99)
    Behaviors.same
  }
}


object Comparator {

  final case class AskForComparison(productName: String, replyTo: ActorRef[ComparisonResult])

  def apply(): Behavior[AskForComparison] =
    Behaviors.setup { context =>
      val greeter = context.spawn(Worker(), "worker")
      Behaviors.receiveMessage { message =>
        greeter ! Worker.Compare(message.productName, message.replyTo)
        Behaviors.same
      }
    }
}


object Client {

  final case class ComparisonResult(name: String, value: Double)

  def apply(): Behavior[ComparisonResult] =
    Behaviors.setup {
      _ =>
        Behaviors.receiveMessage { message =>
          println("Cheaper is: ", message.name, message.value)
          Behaviors.same
        }
    }

}

object AkkaQuickstart extends App {
  val main: ActorSystem[Comparator.AskForComparison] = ActorSystem(Comparator(), "Main")
  val client: ActorRef[ComparisonResult] = ActorSystem(Client(), "Client")
  main ! AskForComparison("randomProduct", client)
}

