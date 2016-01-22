package actors

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._

class GameRoom(players: List[String]) extends Actor with ActorLogging {
  import concurrent.ExecutionContext.Implicits.global
  implicit val timeout : Timeout = 1.second

  context.system.scheduler.scheduleOnce(15.minutes, self, PoisonPill)

  val random = scala.util.Random
  var savedSnakePositions: Map[ActorRef, List[(Int, Int)]] = Map.empty
  var snakeToPlayer: Map[ActorRef, ActorRef] = Map.empty
  var food : (Int, Int) = genFood()

  def genFood() : (Int, Int) = {
    var generatedFood = nextCoupleInFieldSizes
    while (savedSnakePositions.exists(snakeToPositions => snakeToPositions._2.contains(generatedFood)))
      generatedFood = nextCoupleInFieldSizes
    snakeToPlayer.keys foreach(_ ! Snake.Food(generatedFood))
    generatedFood
  }

  def nextCoupleInFieldSizes: (Int, Int) =
    ( random.nextInt(Games.fieldSize._1),
      random.nextInt(Games.fieldSize._2) )

  override def receive: Receive = {
    case Messages.PlayerConnected(uid) =>
      onPlayerConnection(uid)
    case Snake.AteItself =>
      onSnakeDeath()
    case Snake.SnakeState(pos) =>
      saveSnakePosition(pos)
    case Snake.AteFood =>
      println("GameRoom: Ate food")
      food = genFood()
    case Messages.GetState =>
      sender ! Messages.State(savedSnakePositions.values, food)
    case x: Terminated =>
      println("Snake died")
    case other =>
      println("Something strange come to GameRoom: " + other)
  }

  def onPlayerConnection(uid: String) = {
    if (players.contains(uid)) {
      val createdSnake = context.system.actorOf(Props[Snake])
      snakeToPlayer += createdSnake -> sender()
      context.watch(createdSnake)
      food = genFood()
      sender ! Messages.SnakeCreated(createdSnake)
    }
    if (snakeToPlayer.keys.size == players.size) {
      context.system.scheduler.schedule(initialDelay = 2.seconds, interval = 250.millis) {
        snakeToPlayer.keys foreach (_ ! Snake.Move)
      }
    }
  }

  def onSnakeDeath() = {
    snakeToPlayer -= sender()
    savedSnakePositions -= sender()
    sender() ! PoisonPill
  }

  def saveSnakePosition(pos : List[(Int, Int)]) = {
    savedSnakePositions += sender() -> pos
  }
}