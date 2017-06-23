package com.mogproject.image.graphic

import com.mogproject.image.Arguments.{Japanese, Language}
import com.mogproject.image.graphic.shape._
import com.mogproject.mogami.core.game.GameStatus.Playing
import com.mogproject.mogami.util.Implicits._
import com.mogproject.mogami.{BoardType, HandType, _}

/**
  * Abstract board drawing
  */
case class BoardGraphicWide(flip: Boolean = false,
                            turn: Player = Player.BLACK,
                            board: BoardType = State.HIRATE.board,
                            hand: HandType = State.HIRATE.hand,
                            lastMove: Seq[Square] = Seq.empty,
                            gameStatus: GameStatus = Playing,
                            indexDisplay: Option[Language] = Some(Japanese),
                            pieceLang: Language = Japanese,
                            blackName: String,
                            whiteName: String,
                            blackPic: Array[Byte],
                            whitePic: Array[Byte]
                           ) extends BoardGraphic {
  override val windowWidth: Int = (windowMargin + boardMargin + indicatorMargin) * 2 + pieceWidth * 13
  override val windowHeight: Int = boardMargin * 2 + pieceHeight * 9

  override protected lazy val boardLeft: Int = windowMargin + pieceWidth * 2 + boardMargin + indicatorMargin
  override protected lazy val boardTop: Int = boardMargin

  override protected lazy val handBlackLeft: Int = boardRect.right + boardMargin + indicatorMargin
  override protected lazy val handBlackTop: Int = boardRect.top + pieceHeight * 5
  override protected lazy val handColumns: Int = 2

  override protected lazy val indicatorBlackTop: Int = handBlackTop - pieceWidth * 2 - indicatorHeight - 6
  override protected lazy val symbolBlackTop: Int =  boardRect.top + pieceHeight / 2

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
      handBlackTop - (pieceWidth * 2 + 4) - 2,
      pieceWidth * 2 + 4,
      pieceWidth * 2 + 4)
    Seq(
      Image(blackPic, flip.when(rotateRect)(base), flip),
      Image(whitePic, (!flip).when(rotateRect)(base), !flip)
    )
  }

  private[this] val playerNames: Seq[Shape] = {
    val base = Rectangle(
      handBlackLeft,
      boardRect.top + pieceHeight * 2,
      pieceWidth * 2,
      indicatorHeight
    )
    Seq(
      Text(blackName, indicatorFontSize, flip.when(rotateRect)(base), Text.BOLD, flip = flip),
      Text(whiteName, indicatorFontSize, (!flip).when(rotateRect)(base), Text.BOLD, flip = !flip)
    )
  }

}
