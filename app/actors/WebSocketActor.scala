package actors

import actors.WebSocketActor.{Coordinate, StateCoordinates, Value}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.util.Timeout
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  import Messages._

  import concurrent.ExecutionContext.Implicits.global
  implicit val timeout : Timeout = 1.second

  implicit val coordinateWrites = Json.writes[Coordinate]
  implicit val stateWrites = Json.writes[StateCoordinates]
  implicit val playersWrites = Json.writes[Players]
  implicit val valueWrites = Json.writes[Value]

  Lobby() ! Started

  def receive = initial

  def initial = LoggingReceive {
    case x : JsValue => (x \ "t").as[String] match {
      case "FindPlayers" =>
        Lobby() ! GetList
      case _ =>
    }
    case x : Players =>
      val myUid = x.me
      out ! Json.toJson(x)
      context.become(waitingForCommand(myUid))
  }

  def waitingForCommand(myUid: String) : Actor.Receive = LoggingReceive {
    case x : JsValue => (x \ "t").as[String] match {
      case "CreateGame" =>
        val player2 = (x \ "player2").as[String]
        AllGames() ! Pair(myUid, player2)
        Lobby() ! GetCompany(player2)
        context.become(sendingInvitationWaitingRoom(myUid))
      case "FindPlayers" =>
        Lobby() ! GetList
      case "Connect" =>
        val player = (x \ "player").as[String]
        AllGames() ! FindGame(player)
        context.become(waitingRoom(myUid))
      case _ =>
    }
    case x : Players =>
      out ! Json.toJson(x)
    case Invitation(otherPlayer) =>
      out ! Json.toJson(Value("Invitation", otherPlayer))
  }

  def sendingInvitationWaitingRoom(myUid: String) = LoggingReceive {
    case Messages.Room(room) =>
      context.become(sendingInvitation(myUid, room))
    case Company(company) =>
      company ! Invitation(myUid)
      context.become(waitingRoom(myUid))
  }

  def sendingInvitation(myUid: String, room: ActorRef) = LoggingReceive {
    case Company(company) =>
      company ! Invitation(myUid)
      room ! PlayerConnected(myUid)
      context.become(waitingForSnake(myUid))
  }

  def waitingRoom(myUid: String) = LoggingReceive {
    case Messages.Room(room) =>
      room ! PlayerConnected(myUid)
      context.become(waitingForSnake(myUid))
  }

  def waitingForSnake(myUid: String) = LoggingReceive {
    case x : SnakeCreated =>
      val snake = x.snake
      context.system.scheduler.schedule(1.second, 10.seconds, sender, GetState)
      out ! Json.toJson(Value("Status", "Snake created"))
      println("playing")
      context.become(playing(snake))
  }

  def playing(snake: ActorRef) = LoggingReceive {
    case x : JsValue => (x \ "t").as[String] match {
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
    case State(snake1, snake2, food) =>
      val coorSnake1 = snake1.map(pair => new Coordinate(pair._1, pair._2))
      val coorSnake2 = snake2.map(pair => new Coordinate(pair._1, pair._2))
      out ! Json.toJson(StateCoordinates(coorSnake1, coorSnake2, new Coordinate(food._1, food._2)))
  }
}

object WebSocketActor {
  case class Coordinate(x: Int, y: Int)
  case class StateCoordinates(snake1: List[Coordinate], snake2: List[Coordinate], food: Coordinate)
  case class Value(t: String, value: String)
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}