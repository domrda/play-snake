package actors

import actors.WebSocketActor.{Coordinate, StateCoordinates}
import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import play.api.libs.json.{JsValue, Json}

class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  import Messages._

  implicit val coordinateWrites = Json.writes[Coordinate]
  implicit val stateWrites = Json.writes[StateCoordinates]

  var gameRoom : ActorRef = _
  var uid : String = _
  var snake : ActorRef = _

  def receive = LoggingReceive {
    case x : JsValue => (x \ "t").as[String] match {
      case "FindGame" =>
        uid = (x \ "uid").as[String]
        AllGames() ! FindGame(uid)
      case "Direction" =>
        val dir = (x \ "dir").as[String]
        dir match {
          case "Up" => snake ! Up
          case "Down" => snake ! Down
          case "Left" => snake ! Left
          case "Right" => snake ! Right
          case _ =>
        }
      case _ =>
    }
    case GameFound(gameInfo) =>
      gameRoom = gameInfo._2
      gameRoom ! PlayerConnected(uid)
    case State(snake1, snake2, food) =>
      val coorSnake1 = snake1.map(pair => new Coordinate(pair._1, pair._2))
      val coorSnake2 = snake2.map(pair => new Coordinate(pair._1, pair._2))
      out ! Json.toJson(StateCoordinates(coorSnake1, coorSnake2, new Coordinate(food._1, food._2)))
    case x : SnakeCreated =>
      snake = x.snake
      out ! Json.toJson("{status:\"Ok\"}")
    case _ =>
  }
}

object WebSocketActor {
  case class Coordinate(x: Int, y: Int)
  case class StateCoordinates(snake1: List[Coordinate], snake2: List[Coordinate], food: Coordinate)

  def props(out: ActorRef) = Props(new WebSocketActor(out))
}