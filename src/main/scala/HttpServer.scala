import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives.{complete, concat, get, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonWriter

import scala.concurrent.{ExecutionContext, Future}

class HttpServer(localServer: ActorRef) {
  private val htmlParser: OpineoHtmlResponseParser = new OpineoHtmlResponseParser
  private implicit val serverResponseWriter: RootJsonWriter[Response] = {
    case queryResult: QueryResult => jsonFormat3(QueryResult).write(queryResult)
    case priceNotFound: PriceNotFound => jsonFormat2(PriceNotFound).write(priceNotFound)
  }

  def handleRequests(implicit executionContext: ExecutionContext, timeout: Timeout, system: ActorSystem): Route = {
    concat(pathPrefix("price" / Remaining) { id =>
      get {
        val eventualResponse = (localServer ? Query(id)).fallbackTo(Future {
          PriceNotFound(id, 0)
        }).mapTo[Response]
        complete(eventualResponse)
      }
    }, pathPrefix("review" / Remaining) { id =>
      get {
        complete(Http.get(system).singleRequest(HttpRequest(uri = getURL(id)))
          .flatMap { response =>
            response.entity.toStrict(timeout.duration)
          }.map { entity =>
          try {
            htmlParser.parseHtml(entity)
          } catch {
            case ex: Exception => "Error occurred: " + ex.getMessage
          }
        })
      }
    }
    )
  }

  private def getURL(id: String) = {
    "https://www.opineo.pl/?szukaj=%s&s=2".format(id)
  }
}
