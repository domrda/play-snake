package actors

import akka.actor._
import akka.util.Timeout

import scala.concurrent.duration._

class GameRoom(player1: String, player2: String) extends Actor with ActorLogging {
  import concurrent.ExecutionContext.Implicits.global
  implicit val timeout : Timeout = 1.second

  val random = scala.util.Random
  var snakes: Set[ActorRef] = Set.empty
  var snake1: List[(Int, Int)] = List.empty
  var snake2: List[(Int, Int)] = List.empty
  var snakeActor1 : ActorRef = null
  var snakeActor2 : ActorRef = null
  var food : (Int, Int) = _

  context.system.scheduler.scheduleOnce(15.minutes, self, PoisonPill)

  def genFood() : (Int, Int) = {
    var gen = (random.nextInt(Games.fieldSize._1), random.nextInt(Games.fieldSize._2))
    while (snake1.contains(gen) || snake2.contains(gen))
      gen = (random.nextInt(Games.fieldSize._1), random.nextInt(Games.fieldSize._2))
    snakes foreach(_ ! Snake.Food(gen))
    println("GenFood: " + gen)
    gen
  }

  def onPlayerConnection(uid: String) = {
    println("PlayerConnected from ", sender())
    if (uid == player1 || uid == player2) {
      val snake = context.system.actorOf(Props[Snake])
      if (uid == player1)
        snakeActor1 = snake
      else
        snakeActor2 = snake
      context.watch(snake)
      snakes += snake
      food = genFood()
      sender ! Messages.SnakeCreated(snake)
    }
    if (snakeActor1 != null && snakeActor2 != null) {
      context.system.scheduler.schedule(10.seconds, 250.millis) {
        snakes.foreach(_ ! Snake.Move)
      }
      println("Game room active!")
    }
  }

  def onSnakeDeath() = {
    if (sender == snakeActor1) {
      snake1 = List.empty
      snakeActor1 = null
    } else {
      snake2 = List.empty
      snakeActor2 = null
    }
    println("Snake ate itself")
    snakes -= sender
    sender ! PoisonPill
  }

  override def receive: Receive = {
    case Messages.PlayerConnected(uid) =>
      onPlayerConnection(uid)
    case Snake.AteItself =>
      onSnakeDeath()
    case Snake.SnakeState(pos) =>
      if (sender == snakeActor1)
        snake1 = pos
      else
        snake2 = pos
    case Snake.AteFood =>
      food = genFood()
    case Messages.GetState =>
      sender ! Messages.State(snake1, snake2, food)
    case x: Terminated =>
      println("Snake died")//context.stop(self)
    case other =>
      println("Something strange come to GameRoom: " + other)
  }
}