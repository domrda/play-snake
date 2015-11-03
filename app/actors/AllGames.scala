package actors

import akka.actor._
import play.libs.Akka

class AllGames extends Actor with ActorLogging {

  var games : Map[(String, String), ActorRef] = Map.empty

  override def receive: Actor.Receive = {
    case Messages.Pair(player1, player2) =>
      val room = context.system.actorOf(Props(classOf[GameRoom], player1, player2))
      games += (player1, player2) -> room
    case Messages.FindGame(player) =>
      println("FindGame from ", sender())
      val gameFound = games.find(complect => complect._1._1 == player || complect._1._2 == player).orNull
      sender ! Messages.GameFound(gameFound)
    case other => println("Something strange come to Games:" + other)
  }
}

object AllGames {
  lazy val g = Akka.system().actorOf(Props[AllGames])
  def apply() = g
}