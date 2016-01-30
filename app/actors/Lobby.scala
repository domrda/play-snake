package actors

import akka.actor.{Actor, ActorRef, Props, Terminated}
import play.libs.Akka

import scala.util.Random

class Lobby extends Actor {
  import Messages._
  var players : Map[ActorRef, String] = Map.empty

  def generatePlayerName = Seq.fill(4)(Random.nextInt(Lobby.words.size)).map(i => Lobby.words(i)).mkString

  def receive = {
    case Started =>
      context.watch(sender)
      players += sender -> generatePlayerName
      sendPlayers()
    case GetList =>
      sendPlayers()
    case Terminated =>
      players -= sender
    case GetCompany(other) =>
      val company = players.filter(p => other.contains(p._2)).keys
      sender ! Company(company)
    case other =>
      println("Something strange come to lobby: " + other)
  }

  def sendPlayers() = {
    val filtered = players filter(p => p._1 != sender)
    sender ! Players(players(sender), filtered.values)
  }
}

object Lobby {
  lazy val lobbyActor = Akka.system().actorOf(Props[Lobby])
  def apply() = lobbyActor

  val words: List[String] = List("Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Lime")
}