package controllers

import actors.{AllGames, Messages, WebSocketActor}
import play.api.libs.json.JsValue
import play.api.mvc.WebSocket
import play.api.mvc._
import play.api.Play.current

class Application extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out)
  }

}
