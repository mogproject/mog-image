package com.mogproject.image.graphic

import com.mogproject.image.Arguments.{English, Japanese, Language}
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.{BoardType, HandType, _}
import com.mogproject.mogami.util.Implicits._

/**
  *
  */
trait BoardGraphic extends Graphic {
  // variables
  def turn: Player

  def board: BoardType

  def hand: HandType

  def gameStatus: GameStatus

  def lastMove: Seq[Square]

  def flip: Boolean

  def indexDisplay: Option[Language]

  // layouts
  def windowWidth: Int

  def windowHeight: Int

  protected def boardLeft: Int

  protected def boardTop: Int

  protected def handBlackLeft: Int

  protected def handBlackTop: Int

  protected def handColumns: Int

  protected def indicatorBlackTop: Int

  protected def symbolBlackTop: Int

  protected val pieceWidth = 86
  protected val pieceHeight = 90

  protected lazy val boardRect: Rectangle = Rectangle(boardLeft, boardTop, pieceWidth * 9, pieceHeight * 9, stroke = 3)

  protected val handPieceWidth = 70
  protected val handPieceHeight = 67

  protected val handNumberWidth = 50
  protected val handNumberHeight = 45

  protected val boardMargin = 40
  protected val windowMargin = 20
  protected val indicatorMargin = 8

  protected val dotSize = 4

  protected val symbolHeight = 168
  protected val indicatorHeight = 40

  // font sizes
  protected val pieceFontSize = 80
  protected val indexFontSize = 30
  protected val handFontSize = 60
  protected val handNumberSize = 40
  protected val playerIconSize = 90
  protected val indicatorFontSize = 30

  // constants
  protected lazy val fileIndex: String = "９８７６５４３２１"
  protected lazy val rankIndex: String = indexDisplay match {
    case Some(Japanese) => "一二三四五六七八九"
    case Some(English) => "abcdefghi"
    case _ => ""
  }

  //
  // board elements
  //

  protected val lastMoveBackground: Seq[Shape] = lastMove.map(squareToRect(_).copy(strokeColor = None, fillColor = Some(Color.lastMove)))

  protected val lastMoveForeground: Seq[Shape] =
    lastMove.lastOption.toSeq.map(squareToRect(_).copy(strokeColor = Some(Color.cursor), stroke = 10, strokeGradation = Some(Color.cursor.copy(a = 20))))


  protected lazy val background: Seq[Shape] = Seq(Rectangle(0, 0, windowWidth, windowHeight, None, Some(Color.WHITE)))

  protected val boardShapes: Seq[Shape] = Seq(
    boardRect
  ) ++ (1 to 8).map(i =>
    Line(boardRect.left + pieceWidth * i, boardRect.top, boardRect.left + pieceWidth * i, boardRect.bottom) // vertical lines
  ) ++ (1 to 8).map(i =>
    Line(boardRect.left, boardRect.top + pieceHeight * i, boardRect.right, boardRect.top + pieceHeight * i) // horizontal lines
  ) ++ (0 to 3).map(i =>
    Circle(boardRect.left + pieceWidth * 3 * (1 + i / 2), boardRect.top + pieceHeight * 3 * (1 + i % 2), dotSize, None, Some(Color.BLACK)) // dots
  )

  protected val indexShapes: Seq[Shape] = if (indexDisplay.isDefined) {
    flip.when[String](_.reverse)(fileIndex).zipWithIndex.map { case (c, i) =>
      Text(c.toString, indexFontSize, Rectangle(boardRect.left + i * pieceWidth, 0, pieceWidth, boardMargin))
    } ++ flip.when[String](_.reverse)(rankIndex).zipWithIndex.map { case (c, i) =>
      Text(c.toString, indexFontSize, Rectangle(boardRect.right, boardRect.top + i * pieceHeight, boardMargin, pieceHeight))
    }
  } else Seq.empty

  protected val boardPieces: Seq[Shape] = board.map { case (sq, p) =>
    Text(p.ptype.toJapaneseSimpleName, pieceFontSize, squareToRect(sq), Text.BOLD, flip = flip ^ p.owner.isWhite, foreColor = p.isPromoted.fold(Color.red, Color.BLACK))
  }.toSeq

  //
  // hand elements
  //
  protected lazy val handShapes: Seq[Shape] = {
    val base = Rectangle(handBlackLeft, handBlackTop, pieceWidth * handColumns, pieceHeight * (6 / handColumns + 1), stroke = 3)
    Seq(base, rotateRect(base))
  }

  protected lazy val handPieces: Seq[Shape] = hand.toSeq.flatMap { case (Hand(pl, pt), n) => getHandPieceText(pl, pt, n) }

  private[this] def getHandPieceText(player: Player, ptype: Ptype, n: Int): Seq[Text] = {
    val isWhiteSide = player.isWhite ^ flip
    val base = Rectangle(
      handBlackLeft + pieceWidth * ((ptype.sortId - 1) % handColumns),
      handBlackTop + pieceHeight * ((ptype.sortId - 1) / handColumns),
      handPieceWidth,
      handPieceHeight
    )
    (n > 0).option(
      Text(ptype.toJapaneseSimpleName, handFontSize, isWhiteSide.when(rotateRect)(base), Text.BOLD, flip = isWhiteSide)
    ).toSeq ++ (n > 1).option {
      val numberRect = Rectangle(base.left + pieceWidth - handNumberWidth, base.top + pieceHeight - handNumberHeight, handNumberWidth, handNumberHeight)
      Text(n.toString, handNumberSize, isWhiteSide.when(rotateRect)(numberRect), Text.PLAIN | Text.ALIGN_RIGHT, flip = isWhiteSide, font = Some("SansSerif"))
    }
  }

  //
  // indicator elements
  //
  protected lazy val indicatorShapes: Seq[Shape] = {
    val base = Rectangle(handBlackLeft - 2, indicatorBlackTop, pieceWidth * handColumns + indicatorMargin + 4, indicatorHeight)
    val bar = Rectangle(base.right - indicatorMargin, base.top, indicatorMargin, boardRect.bottom - base.top + 3)

    getIndicatorParams.toSeq.flatMap { case (pl, (s, c)) =>
      val isWhiteSide = pl.isWhite ^ flip
      val xs = Seq(base, bar).map(r => isWhiteSide.when(rotateRect)(r.copy(strokeColor = None, fillColor = Some(c))))
      xs :+ Text(s, indicatorFontSize, xs.head, Text.BOLD, font = Some("SansSerif"), foreColor = Color.WHITE, flip = isWhiteSide)
    }
  }

  //
  // symbol elements
  //
  protected val symbolShapes: Seq[Shape] = {
    val base = Rectangle(handBlackLeft, symbolBlackTop, pieceWidth * handColumns, symbolHeight)
    Seq(
      Text(Player.BLACK.toSymbolString(), playerIconSize, flip.when(rotateRect)(base), flip = flip),
      Text(Player.WHITE.toSymbolString(), playerIconSize, (!flip).when(rotateRect)(base), flip = !flip)
    )
  }

  //
  // helper functions
  //
  protected def squareToRect(sq: Square): Rectangle = Rectangle(
    boardRect.right - flip.fold(10 - sq.file, sq.file) * pieceWidth,
    boardRect.top + flip.fold(9 - sq.rank, sq.rank - 1) * pieceHeight,
    pieceWidth, pieceHeight)

  /**
    * Rotate rectangle 180 degrees centering the center of the board.
    */
  protected def rotateRect(rect: Rectangle): Rectangle = rect.copy(
    left = boardRect.left + boardRect.right - rect.right + 1,
    top = boardRect.top + boardRect.bottom - rect.bottom + 1
  )

  protected def getIndicatorParams: Map[Player, (String, Color)] = gameStatus match {
    case GameStatus.Playing => Map(turn -> ("TURN", Color.turn))
    case GameStatus.Mated | GameStatus.Resigned | GameStatus.TimedUp | GameStatus.IllegallyMoved => Map(turn -> ("LOSE", Color.lose), !turn -> ("WIN", Color.win))
    case GameStatus.PerpetualCheck | GameStatus.Uchifuzume => Map(!turn -> ("LOSE", Color.lose), turn -> ("WIN", Color.win))
    case GameStatus.Drawn => Map(Player.BLACK -> ("DRAW", Color.draw), Player.WHITE -> ("DRAW", Color.draw))
  }

}