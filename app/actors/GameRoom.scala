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
  var playerToSnake: Map[ActorRef, ActorRef] = Map.empty
  var food : (Int, Int) = genFood()

  def genFood() : (Int, Int) = {
    var generatedFood = nextCoupleInFieldSizes
    while (savedSnakePositions.exists(snakeToPositions => snakeToPositions._2.contains(generatedFood)))
      generatedFood = nextCoupleInFieldSizes
    playerToSnake.values foreach(_ ! Snake.Food(generatedFood))
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
      val playerSnake = playerToSnake.applyOrElse(sender(), (_ : ActorRef) => ActorRef.noSender)
      sender ! Messages.State(savedSnakePositions.applyOrElse(playerSnake, (_ : ActorRef) => List.empty),
        savedSnakePositions.filter(_._1 != playerSnake).values, food)
    case x: Terminated =>
      println("Snake died")
    case other =>
      println("Something strange come to GameRoom: " + other)
  }

  def onPlayerConnection(uid: String) = {
    if (players.contains(uid)) {
      val createdSnake = context.system.actorOf(Props[Snake])
      playerToSnake += sender() -> createdSnake
      context.watch(createdSnake)
      food = genFood()
      sender ! Messages.SnakeCreated(createdSnake)
    }
    if (playerToSnake.values.size == players.size) {
      context.system.scheduler.schedule(initialDelay = 2.seconds, interval = 250.millis) {
        playerToSnake.values foreach (_ ! Snake.Move)
      }
    }
  }

  def onSnakeDeath() = {
    playerToSnake = playerToSnake.filter(_._2 != sender())
    savedSnakePositions -= sender()
    sender() ! PoisonPill
  }

  def saveSnakePosition(pos : List[(Int, Int)]) = {
    savedSnakePositions += sender() -> pos
  }
}