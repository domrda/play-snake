package actors

import actors.Messages.GetCompany

import scala.util.Random

import akka.actor.{Props, ActorRef, Actor, Terminated}
import play.libs.Akka

class Lobby extends Actor {
  var players : Map[ActorRef, String] = Map.empty

  def receive = {
    case Messages.Started =>
      context.watch(sender)
      val uid = Seq.fill(4)(Random.nextInt(Lobby.words.size)).map(i => Lobby.words(i)).mkString
      players += sender -> uid
      sendPlayers
    case Messages.GetList =>
      sendPlayers
    case Terminated =>
      players -= sender
    case Messages.GetCompany(other) =>
      val company = players.find(p => p._2 == other).head._1
      sender ! Messages.Company(company)
    case other =>
      println("Something strange come to lobby: " + other)
  }

  def sendPlayers = {
    val filtered = players filter(p => p._2 != sender)
    sender ! Messages.Players(players(sender), players.values)
  }
}

object Lobby {
  lazy val g = Akka.system().actorOf(Props[Lobby])
  def apply() = g

  val words: List[String] = List("Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Lime")
}