package com.mogproject.image.graphic

import com.mogproject.image.Arguments.{Japanese, Language}
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.core.game.GameStatus.Playing
import com.mogproject.mogami.util.Implicits._
import com.mogproject.mogami.{BoardType, HandType, _}

/**
  * Abstract board drawing
  */
case class BoardGraphicSquare(flip: Boolean = false,
                              turn: Player = Player.BLACK,
                              board: BoardType = State.HIRATE.board,
                              hand: HandType = State.HIRATE.hand,
                              lastMove: Seq[Square] = Seq.empty,
                              gameStatus: GameStatus = Playing,
                              indexDisplay: Option[Language] = Some(Japanese),
                              blackName: String,
                              whiteName: String,
                              blackPic: Array[Byte],
                              whitePic: Array[Byte]
                             ) extends BoardGraphic {
  override lazy val windowWidth: Int = (windowMargin + boardMargin + indicatorMargin) * 2 + pieceWidth * 11
  override lazy val windowHeight: Int = windowWidth

  override protected lazy val boardLeft: Int = windowMargin + pieceWidth + boardMargin + indicatorMargin
  override protected lazy val boardTop: Int = (windowHeight - pieceHeight * 9) / 2

  override protected lazy val handBlackLeft: Int = boardRect.right + boardMargin + indicatorMargin
  override protected lazy val handBlackTop: Int = boardRect.top + pieceHeight * 2
  override protected lazy val handColumns: Int = 1

  override protected lazy val indicatorBlackTop: Int = handBlackTop - pieceWidth - indicatorHeight - 6
  override protected lazy val symbolBlackTop: Int = boardRect.top + pieceHeight - symbolHeight

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
    symbolShapes,
    playerImages,
    playerNames
  ).flatten

  //
  // parts
  //
  private[this] val playerImages: Seq[Shape] = {
    val base = Rectangle(
      handBlackLeft - 2,
      handBlackTop - (pieceWidth + 4) - 2,
      pieceWidth + 4,
      pieceWidth + 4)
    Seq(
      Image(blackPic, flip.when(rotateRect)(base), flip),
      Image(whitePic, (!flip).when(rotateRect)(base), !flip)
    )
  }

  private[this] val playerNames: Seq[Shape] = {
    val base = Rectangle(
      0,
      boardRect.top - indicatorHeight * 3,
      windowWidth - windowMargin,
      indicatorHeight * 2
    )
    Seq(
      Text(blackName, indicatorFontSize * 2, flip.when(rotateRect)(base), Text.PLAIN | flip.fold(Text.ALIGN_LEFT, Text.ALIGN_RIGHT)),
      Text(whiteName, indicatorFontSize * 2, (!flip).when(rotateRect)(base), Text.PLAIN | flip.fold(Text.ALIGN_RIGHT, Text.ALIGN_LEFT))
    )
  }

}
