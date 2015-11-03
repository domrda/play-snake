package actors

import akka.actor.Actor

object Snake {
  case class SnakeState(snake: List[(Int, Int)])
  case class Food(food: (Int, Int))
  case object AteFood
  case object Move
  case object AteItself
  case class DropTail(index: Int)
}

class Snake extends Actor {
  println("Snake started")

  import Snake._
  import Messages._

  val random = scala.util.Random
  var currentDirection : Messages.Direction = Messages.Up
  var buffDirection = currentDirection
  var currentBlocks = genStartPositionOfSnake()
  var currentFood : (Int, Int) = (-1, -1)

  def genStartPositionOfSnake() = {
    val p = (random.nextInt(Games.fieldSize._1), random.nextInt(Games.fieldSize._2))
    List((p._1, p._2), (p._1, p._2-1 % Games.fieldSize._2), (p._1, p._2-2 % Games.fieldSize._2))
  }

  override def receive: Receive = {
    case Down => if (currentDirection != Up) buffDirection = Down
    case Left => if (currentDirection != Right) buffDirection = Left
    case Right => if (currentDirection != Left) buffDirection = Right
    case Up => if (currentDirection != Down) buffDirection = Up

    case Food(pos) => currentFood = pos
    case Move => move()
    case DropTail(index) =>
      println("DropTail "+index)
      currentBlocks = currentBlocks.dropRight(index)
  }

  def move() = {
    val head = currentBlocks.head
    if (currentDirection != buffDirection) currentDirection = buffDirection
    currentDirection match {
      case Down =>
        currentBlocks = (head._1, (Games.fieldSize._2 + head._2 - 1) % Games.fieldSize._2) :: currentBlocks
      case Left =>
        currentBlocks = ((Games.fieldSize._1 + head._1 - 1) % Games.fieldSize._1, head._2) :: currentBlocks
      case Right =>
        currentBlocks = ((head._1 + 1) % Games.fieldSize._1, head._2) :: currentBlocks
      case Up =>
        currentBlocks = (head._1, (head._2 + 1) % Games.fieldSize._2) :: currentBlocks
    }
    if (head == currentFood) {
      sender ! AteFood
    } else {
      currentBlocks = currentBlocks.dropRight(1)
    }

    if (currentBlocks.distinct.size == currentBlocks.size) sender ! SnakeState(currentBlocks)
    else sender ! AteItself
  }
}
