package actors

import actors.WebSocketActor.{Value, Coordinate, StateCoordinates}
import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import akka.util.Timeout

import scala.concurrent.duration._
import play.api.libs.json.{JsValue, Json}

class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  import Messages._
  import concurrent.ExecutionContext.Implicits.global
  implicit val timeout : Timeout = 1.second

  implicit val coordinateWrites = Json.writes[Coordinate]
  implicit val stateWrites = Json.writes[StateCoordinates]
  implicit val playersWrites = Json.writes[Players]
  implicit val valueWrites = Json.writes[Value]

  var myUid : String = _
  var snake : ActorRef = _
  Lobby() ! Started

  def receive = LoggingReceive {
    case x : JsValue => (x \ "t").as[String] match {
      case "CreateGame" =>
        val player1 = (x \ "player1").as[String]
        val player2 = (x \ "player2").as[String]
        AllGames() ! Pair(player1, player2)
        Lobby() ! GetCompany(player2)
      case "Direction" =>
        val dir = (x \ "dir").as[String]
        dir match {
          case "Up" => snake ! Up
          case "Down" => snake ! Down
          case "Left" => snake ! Left
          case "Right" => snake ! Right
          case _ =>
        }
      case "FindPlayers" =>
        Lobby() ! GetList
      case "Connect" =>
        val player = (x \ "player").as[String]
        AllGames() ! FindGame(player)
      case _ =>
    }
    case Messages.Room(room) =>
      room ! PlayerConnected(myUid)
    case x : SnakeCreated =>
      snake = x.snake
      context.system.scheduler.schedule(0.second, 100.millis, sender, GetState)
      out ! Json.toJson(Value("Status", "Snake created"))
    case x : Players =>
      myUid = x.me
      out ! Json.toJson(x)
    case Company(company) =>
      company ! Invitation(myUid)
    case Invitation(otherPlayer) =>
      out ! Json.toJson(Value("Invitation", otherPlayer))
    case State(snake1, snake2, food) =>
      val coorSnake1 = snake1.map(pair => new Coordinate(pair._1, pair._2))
      val coorSnake2 = snake2.map(pair => new Coordinate(pair._1, pair._2))
      out ! Json.toJson(StateCoordinates(coorSnake1, coorSnake2, new Coordinate(food._1, food._2)))
    case _ =>
  }
}

object WebSocketActor {
  case class Coordinate(x: Int, y: Int)
  case class StateCoordinates(snake1: List[Coordinate], snake2: List[Coordinate], food: Coordinate)
  case class Value(t: String, value: String)
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}