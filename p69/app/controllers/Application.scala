package controllers

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.twirl.api.Html
import reactivemongo.api.Cursor

import scala.concurrent.Future

class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  import scala.xml.PrettyPrinter

  val pp = new PrettyPrinter(80, 2)

  def index = Action {
    val body = views.html.index().body.trim
    val str = pp.format(scala.xml.XML.loadString(body))
    Ok(new Html(str))
  }

  def find(name: String, alias: String, tag: String) = Action.async {
    val cursor: Cursor[JsObject] = collection
      .find(Json.obj("name" -> Json.obj("$regex" -> (".*" + name + ".*")),
                     "aliases.name" -> Json.obj("$regex" -> (".*" + alias + ".*")),
                     "tags.value" -> Json.obj("$regex" -> (".*" + tag + ".*"))))
      .sort(Json.obj("rating.value" -> -1))
      .cursor[JsObject]

    val futureArtistList: Future[List[JsObject]] = cursor.collect[List]()

    futureArtistList.map { artist =>
      Ok(Json.prettyPrint(Json.arr(artist)))
    }
  }

  def collection: JSONCollection = db.collection[JSONCollection]("artist")
}
