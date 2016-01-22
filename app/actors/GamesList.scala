package actors

import akka.actor._
import play.libs.Akka

class GamesList extends Actor with ActorLogging {

  var games : Map[String, ActorRef] = Map.empty

  override def receive: Actor.Receive = {
    case Messages.Pair(player1, player2) =>
      startGameForPlayers(List(player1, player2))
    case Messages.FindGame(player) =>
      sender ! Messages.Room(games(player))
    case Messages.StopGame(uid) =>
      games -= uid
    case other => println("Something strange come to Games:" + other)
  }

  def startGameForPlayers(players: List[String]): Unit = {
    val room = context.system.actorOf(Props(classOf[GameRoom], players))
    games = players.map(_ -> room).toMap
    sender ! Messages.Room(room)
  }
}

object GamesList {
  lazy val gamesListActor = Akka.system().actorOf(Props[GamesList])
  def apply() = gamesListActor
}