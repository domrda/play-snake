package controllers

import actors.{AllGames, Messages, WebSocketActor}
import play.api.libs.json.JsValue
import play.api.mvc.WebSocket
import play.api.mvc._
import play.api.Play.current

class Application extends Controller {
  AllGames().tell(Messages.Pair("player1", "player2"), AllGames())

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def generatePair() : String = {
    //    val player1 = UUID.randomUUID.toString
    //    val player2 = UUID.randomUUID.toString
    val player1 = "59b54598-ef38-4995-b659-fbea3ecbe317"
    val player2 = "23523a7b-af9e-4fab-8703-bcbdb03e0071"
    AllGames() ! Messages.Pair(player1, player2)
    "Player1: " + player1 + "\nPlayer2: " + player2
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    WebSocketActor.props(out)
  }

}
