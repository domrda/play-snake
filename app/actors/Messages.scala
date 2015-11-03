package actors

import akka.actor.ActorRef

object Games {
  //H, V
  val fieldSize = (16, 16)
}

object Messages {
  //Web
  case class Pair(player1: String, player2: String) extends Serializable
  //Connector
  case class Connect(uid: String) extends Serializable
  case class Connected(connection: ActorRef) extends Serializable
  //Connection
  case class FindGame(uid: String) extends Serializable
  case class GameFound(gameInfo: ((String, String), ActorRef)) extends Serializable
  //Games
  case class PlayerConnected(uid: String) extends Serializable
  case class SnakeCreated(snake: ActorRef) extends Serializable
  case object GetState extends Serializable
  case class State(snake1: List[(Int, Int)], snake2: List[(Int, Int)], food: (Int, Int)) extends Serializable
  //Snake
  trait Direction extends Serializable
  case object Up extends Direction
  case object Down extends Direction
  case object Left extends Direction
  case object Right extends Direction
}
