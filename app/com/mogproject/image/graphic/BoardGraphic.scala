package com.mogproject.image.graphic

import com.mogproject.mogami.{BoardType, HandType, _}
import com.mogproject.mogami.util.Implicits._
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.core.game.GameStatus.Playing

/**
  * Abstract board drawing
  */
case class BoardGraphic(flip: Boolean = false,
                        turn: Player = Player.BLACK,
                        board: BoardType = State.HIRATE.board - Square(7, 7) + (Square(7, 6) -> Piece(Player.BLACK, PAWN)),
                        hand: HandType = State.MATING_BLACK.hand.filterKeys(_.owner.isWhite) ++ State.MATING_WHITE.hand.filterKeys(_.owner.isBlack), //State.HIRATE.hand,
                        lastMove: Seq[Square] = Seq(Square(7, 7), Square(7, 6)), // Seq.empty,
                        gameStatus: GameStatus = Playing,
                        recordLang: String = "ja",
                        pieceLang: String = "ja"
                       ) extends Graphic {
  lazy val windowWidth: Int = (windowMargin + boardMargin) * 2 + pieceWidth * 11
  lazy val windowHeight: Int = boardMargin * 2 + pieceHeight * 9

  private[this] val pieceWidth = 86
  private[this] val pieceHeight = 90

  private[this] val handPieceWidth = 70
  private[this] val handPieceHeight = 67

  private[this] val handNumberWidth = 50
  private[this] val handNumberHeight = 45

  private[this] val boardMargin = 40
  private[this] val windowMargin = 28

  private[this] val dotSize = 4

  private[this] val playerIconHeight = 168
  private[this] val indicatorHeight = 40
  private[this] val indicatorBarWidth = 8

  // font sizes
  private[this] val pieceFontSize = 80
  private[this] val indexFontSize = 30
  private[this] val handFontSize = 60
  private[this] val handNumberSize = 40
  private[this] val playerIconSize = 90
  private[this] val indicatorFontSize = 30

  private[this] val fileIndex = "９８７６５４３２１"
  private[this] val rankIndex = recordLang match {
    case "ja" => "一二三四五六七八九"
    case "en" => "abcdefghi"
  }

  protected lazy val shapes: Seq[Shape] = Seq(
    background,
    lastMoveBackground,
    boardShapes,
    lastMoveForeground,
    indexShapes,
    handShapes,
    boardPieces,
    handPieces,
    indicatorShapes,
    playerIconShapes
  ).flatten

  //
  // parts
  //
  private[this] lazy val lastMoveBackground: Seq[Shape] = lastMove.map(squareToRect(_).copy(strokeColor = None, fillColor = Some(Color.lastMove)))

  private[this] lazy val lastMoveForeground: Seq[Shape] =
    lastMove.lastOption.toSeq.map(squareToRect(_).copy(strokeColor = Some(Color.cursor), stroke = 10, strokeGradation = Some(Color.cursor.copy(a = 20))))

  private[this] val boardRect = Rectangle(windowMargin + pieceWidth + boardMargin, boardMargin, pieceWidth * 9, pieceHeight * 9, stroke = 3)

  private[this] val background: Seq[Shape] = Seq(Rectangle(0, 0, windowWidth, windowHeight, None, Some(Color.WHITE)))

  private[this] val boardShapes: Seq[Shape] = Seq(
    boardRect
  ) ++ (1 to 8).map(i =>
    Line(boardRect.left + pieceWidth * i, boardRect.top, boardRect.left + pieceWidth * i, boardRect.bottom) // vertical lines
  ) ++ (1 to 8).map(i =>
    Line(boardRect.left, boardRect.top + pieceHeight * i, boardRect.right, boardRect.top + pieceHeight * i) // horizontal lines
  ) ++ (0 to 3).map(i =>
    Circle(boardRect.left + pieceWidth * 3 * (1 + i / 2), boardRect.top + pieceHeight * 3 * (1 + i % 2), dotSize, None, Some(Color.BLACK)) // dots
  )

  private[this] lazy val indexShapes: Seq[Shape] = flip.when[String](_.reverse)(fileIndex).zipWithIndex.map { case (c, i) =>
    Text(c.toString, indexFontSize, Rectangle(boardRect.left + i * pieceWidth, 0, pieceWidth, boardMargin))
  } ++ flip.when[String](_.reverse)(rankIndex).zipWithIndex.map { case (c, i) =>
    Text(c.toString, indexFontSize, Rectangle(boardRect.right, boardRect.top + i * pieceHeight, boardMargin, pieceHeight))
  }

  private[this] val boardPieces: Seq[Shape] = board.map { case (sq, p) =>
    Text(p.ptype.toJapaneseSimpleName, pieceFontSize, squareToRect(sq), Text.BOLD, flip = flip ^ p.owner.isWhite)
  }.toSeq

  private[this] val handShapes: Seq[Shape] = Seq(
    Rectangle(boardRect.right + boardMargin, boardRect.top + pieceHeight * 2, pieceWidth, pieceHeight * 7, stroke = 3),
    Rectangle(windowMargin, boardMargin, pieceWidth, pieceHeight * 7, stroke = 3)
  )

  private[this] val handPieces: Seq[Shape] = hand.filter(_._2 > 0).keys.toSeq.map { case Hand(pl, pt) =>
    val isWhiteSide = pl.isWhite ^ flip
    val base = Rectangle(boardRect.right + boardMargin, boardRect.top + pieceHeight * (1 + pt.sortId), handPieceWidth, handPieceHeight)
    Text(pt.toJapaneseSimpleName, handFontSize, isWhiteSide.when(rotateRect)(base), Text.BOLD, flip = isWhiteSide)
  } ++ hand.filter(_._2 > 1).toSeq.map { case (Hand(pl, pt), n) =>
    val isWhiteSide = pl.isWhite ^ flip
    val baseRect = Rectangle(
      boardRect.right + boardMargin + pieceWidth - handNumberWidth,
      boardRect.top + pieceHeight * (2 + pt.sortId) - handNumberHeight, handNumberWidth, handNumberHeight)
    Text(f"${n}%2d", handNumberSize, isWhiteSide.when(rotateRect)(baseRect), Text.PLAIN, flip = isWhiteSide, font = Some("Monospaced"))
  }

  private[this] val indicatorShapes: Seq[Shape] = {
    val base = Rectangle(
      boardRect.right + boardMargin - 2, boardRect.top + pieceHeight * 2 - indicatorHeight - 2,
      pieceWidth + indicatorBarWidth + 5, indicatorHeight
    )
    val bar = Rectangle(
      base.right - indicatorBarWidth, base.top, indicatorBarWidth, indicatorHeight + pieceHeight * 7 + 5
    )

    getIndicatorParams.toSeq.flatMap { case (pl, (s, c)) =>
      val isWhiteSide = pl.isWhite ^ flip
      val xs = Seq(base, bar).map(r => isWhiteSide.when(rotateRect)(r.copy(strokeColor = None, fillColor = Some(c))))
      xs :+ Text(s, indicatorFontSize, xs.head, Text.BOLD, font = Some("SansSerif"), foreColor = Color.WHITE, flip = isWhiteSide)
    }
  }

  private[this] val playerIconShapes: Seq[Shape] = {
    val base = Rectangle(boardRect.right + boardMargin, boardRect.top + pieceHeight * 2 - playerIconHeight, pieceWidth, playerIconHeight)
    Seq(
      Text(Player.BLACK.toSymbolString(), playerIconSize, flip.when(rotateRect)(base), flip = flip),
      Text(Player.WHITE.toSymbolString(), playerIconSize, (!flip).when(rotateRect)(base), flip = !flip)
    )
  }

  //
  // helper functions
  //
  private[this] def squareToRect(sq: Square): Rectangle = Rectangle(
    boardRect.right - flip.fold(9 - sq.file, sq.file) * pieceWidth,
    boardRect.top + flip.fold(10 - sq.rank, sq.rank - 1) * pieceHeight,
    pieceWidth, pieceHeight)

  /**
    * Rotate rectangle 180 degrees centering the center of the board.
    */
  private[this] def rotateRect(rect: Rectangle): Rectangle = rect.copy(
    left = boardRect.left + boardRect.right - rect.right + 1,
    top = boardRect.top + boardRect.bottom - rect.bottom + 1
  )

  private[this] def getIndicatorParams: Map[Player, (String, Color)] = gameStatus match {
    case GameStatus.Playing => Map(turn -> ("千日手", Color.turn))
    case GameStatus.Mated | GameStatus.Resigned | GameStatus.TimedUp | GameStatus.IllegallyMoved => Map(turn -> ("LOSE", Color.lose), !turn -> ("WIN", Color.win))
    case GameStatus.PerpetualCheck | GameStatus.Uchifuzume => Map(!turn -> ("LOSE", Color.lose), turn -> ("WIN", Color.win))
    case GameStatus.Drawn => Map(Player.BLACK -> ("DRAW", Color.draw), Player.WHITE -> ("DRAW", Color.draw))
  }


}
