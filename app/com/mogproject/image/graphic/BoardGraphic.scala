package com.mogproject.image.graphic

import com.mogproject.mogami.{BoardType, HandType, _}
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.core.game.GameStatus.Playing

/**
  *
  */
case class BoardGraphic(flip: Boolean = false,
                        turn: Player = Player.BLACK,
                        board: BoardType = State.HIRATE.board,
                        hand: HandType = State.HIRATE.hand,
                        lastMove: Seq[Square] = Seq.empty,
                        gameStatus: GameStatus = Playing,
                        recordLang: String = "ja",
                        pieceLang: String = "ja"
                       ) extends Graphic {
  lazy val windowWidth: Int = (windowMargin + boardMargin) * 2 + pieceWidth * 11
  lazy val windowHeight: Int = windowMargin + boardMargin + pieceHeight * 9

  private[this] val pieceWidth = 86
  private[this] val pieceHeight = 90

  private[this] val boardMargin = 28
  private[this] val windowMargin = 28

  private[this] val dotSize = 4

  protected lazy val shapes: Seq[Shape] = background ++ boardShapes ++ handShapes ++ boardPieces

  //
  // parts
  //
  private[this] val boardRect = Rectangle(windowMargin + pieceWidth + boardMargin, boardMargin, pieceWidth * 9, pieceHeight * 9)

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

  private[this] val handShapes: Seq[Shape] = Seq(
    Rectangle(boardRect.right + boardMargin, boardRect.top + pieceHeight * 2, pieceWidth, pieceHeight * 7),
    Rectangle(windowMargin, boardMargin, pieceWidth, pieceHeight * 7)
  )

  private[this] val boardPieces: Seq[Shape] = board.map { case (sq, p) =>
    Text(p.ptype.toJapaneseSimpleName, "Source Han Serif", 80, squareToRect(sq), flip = flip ^ p.owner.isWhite)
  }.toSeq

  //
  // helper functions
  //
  private[this] def squareToRect(sq: Square): Rectangle = Rectangle(boardRect.right - sq.file * pieceWidth, boardRect.top + (sq.rank - 1) * pieceHeight, pieceWidth, pieceHeight)
}
