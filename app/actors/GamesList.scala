package actors

import akka.actor._
import play.libs.Akka

class GamesList extends Actor with ActorLogging {
  import Messages._
  
  var games : Map[String, ActorRef] = Map.empty

  override def receive: Actor.Receive = {
    case CreateGame(player, players) =>
      startGameForPlayers(player :: players)
    case FindGame(player) =>
      sender ! Room(games(player))
    case StopGame(uid) =>
      games -= uid
    case other => println("Something strange come to Games:" + other)
  }

  def startGameForPlayers(players: List[String]): Unit = {
    val room = context.system.actorOf(Props(classOf[GameRoom], players))
    games = players.map(_ -> room).toMap
    sender ! Room(room)
  }
}

object GamesList {
  lazy val gamesListActor = Akka.system().actorOf(Props[GamesList])
  def apply() = gamesListActor
}