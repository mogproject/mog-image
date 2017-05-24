package com.mogproject.image.graphic

import com.mogproject.image.Arguments.{Japanese, Language}
import com.mogproject.mogami.{BoardType, HandType, _}
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.core.game.GameStatus.Playing

/**
  * Abstract board drawing
  */
case class BoardGraphicCompact(flip: Boolean = false,
                               turn: Player = Player.BLACK,
                               board: BoardType = State.HIRATE.board,
                               hand: HandType = State.HIRATE.hand,
                               lastMove: Seq[Square] = Seq.empty,
                               gameStatus: GameStatus = Playing,
                               indexDisplay: Option[Language] = Some(Japanese)
                              ) extends BoardGraphic {
  override val windowWidth: Int = (windowMargin + boardMargin + indicatorMargin) * 2 + pieceWidth * 11
  override val windowHeight: Int = boardMargin * 2 + pieceHeight * 9

  override protected lazy val boardLeft: Int = windowMargin + pieceWidth + boardMargin + indicatorMargin
  override protected lazy val boardTop: Int = boardMargin

  override protected lazy val handBlackLeft: Int = boardRect.right + boardMargin + indicatorMargin
  override protected lazy val handBlackTop: Int = boardRect.top + pieceHeight * 2
  override protected lazy val handColumns: Int = 1

  override protected lazy val indicatorBlackTop: Int = handBlackTop - indicatorHeight - 2

  override protected def symbolBlackTop: Int = boardRect.top + pieceHeight * 2 - symbolHeight

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
    symbolShapes
  ).flatten
}
