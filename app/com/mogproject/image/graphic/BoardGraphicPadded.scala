package com.mogproject.image.graphic

import com.mogproject.image.Arguments.{Japanese, Language}
import com.mogproject.mogami.core.game.GameStatus.Playing
import com.mogproject.mogami.{BoardType, HandType, _}

/**
  * Abstract board drawing
  */
case class BoardGraphicPadded(flip: Boolean = false,
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
                             ) extends BoardGraphicSquareLike {
  override lazy val windowWidth: Int = areaWidth * 40 / 21
}
