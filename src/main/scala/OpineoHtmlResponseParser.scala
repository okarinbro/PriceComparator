import akka.http.scaladsl.model.HttpEntity
import org.jsoup.Jsoup

class OpineoHtmlResponseParser {
  def parseHtml(htmlEntity: HttpEntity.Strict): String = {
    Jsoup
      .parse(htmlEntity.data.utf8String)
      .body()
      .getElementById("page")
      .getElementById("content")
      .getElementById("screen")
      .getElementsByClass("pls").get(0)
      .getElementsByClass("shl_i pl_i").get(0)
      .getElementsByClass("pl_attr").get(0)
      .getElementsByTag("li")
      .eachText()
      .toArray
      .map(el => el.toString)
      .mkString(" || ")
  }
}
