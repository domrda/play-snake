package actors

import akka.actor.ActorRef

object Games {
  //H, V
  val fieldSize = (16, 16)
}

object Messages {
  //Web
  case class Pair(player1: String, player2: String) extends Serializable
  case class Invitation(other: String) extends Serializable
  case class FindGame(uid: String) extends Serializable
  //Games
  case class PlayerConnected(uid: String) extends Serializable
  case class SnakeCreated(snake: ActorRef) extends Serializable
  case object GetState extends Serializable
  case class State(snakes: Iterable[List[(Int, Int)]], food: (Int, Int)) extends Serializable
  case class Room(room: ActorRef) extends Serializable
  case class StopGame(uid: String) extends Serializable
  //Snake
  trait Direction extends Serializable
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction
  //Lobby
  case object Started extends Serializable
  case object GetList extends Serializable
  case class Players(me: String, everybody: Iterable[String], t: String = "Players") extends Serializable
  case class GetCompany(other: String) extends Serializable
  case class Company(other: ActorRef) extends Serializable
}
