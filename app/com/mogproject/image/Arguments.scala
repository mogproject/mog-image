package com.mogproject.image

import java.net.URLDecoder

import com.mogproject.image.Arguments.GraphicLayout
import com.mogproject.mogami._
import com.mogproject.mogami.core.move.{MoveBuilderSfenBoard, MoveBuilderSfenHand}
import play.api.Logger
import play.api.routing.sird.QueryString

import scala.util.{Failure, Success, Try}

case class Arguments(state: State = State.HIRATE,
                     size: Int = Settings.defaultImageSize,
                     lastMove: Seq[Square] = Seq.empty,
                     gameStatus: GameStatus = GameStatus.Playing,
                     flip: Boolean = false,
                     indexDisplay: Option[Arguments.Language] = Some(Arguments.Japanese),
                     pieceLang: Arguments.Language = Arguments.Japanese,
                     blackName: String = "Black",
                     whiteName: String = "White",
                     blackPicURL: Option[String] = None,
                     whitePicURL: Option[String] = None,
                     layout: GraphicLayout.GraphicLayout = GraphicLayout.Square
                    ) {

  def parseQueryString(q: QueryString): Arguments = this
    .parseState(q)
    .parseSize(q)
    .parseLastMove(q)
    .parseGameStatus(q)
    .parseFlip(q)
    .parseIndexDisplay(q)
    .parsePieceLang(q)
    .parsePlayerNames(q)
    .parsePicURLs(q)
    .parseGraphicLayout(q)

  private[this] def getFirstValue(q: QueryString, key: String): Option[String] = q.get(key).flatMap(_.headOption)

  private[this] def decodeString(s: String): String = URLDecoder.decode(s, "UTF8")

  protected[image] def parseState(q: QueryString): Arguments = {
    getFirstValue(q, "u").flatMap(s => Try(State.parseUsenString(s)) match {
      case Success(st) => Some(st)
      case Failure(e) =>
        Logger.debug(s"Failed to parse USEN string: ${e.toString}")
        None
    }).map(st => this.copy(state = st)).getOrElse {
      getFirstValue(q, "sfen").flatMap(s => Try(State.parseSfenString(s)) match {
        case Success(st) => Some(st)
        case Failure(e) =>
          Logger.debug(s"Failed to parse SFEN string: ${e.toString}")
          None
      }).map(st => this.copy(state = st)).getOrElse(this)
    }
  }

  protected[image] def parseSize(q: QueryString): Arguments = (for {
    s <- getFirstValue(q, "size")
    x <- Try(s.toInt).toOption
    y = math.max(Settings.minImageSize, math.min(x, Settings.maxImageSize))
  } yield this.copy(size = y)).getOrElse(this)

  protected[image] def parseLastMove(q: QueryString): Arguments = getFirstValue(q, "last").flatMap(s => Try(MoveBuilderSfen.parseSfenString(s)) match {
    case Success(MoveBuilderSfenBoard(from, to, _)) => Some(Seq(from, to))
    case Success(MoveBuilderSfenHand(_, to)) => Some(Seq(to))
    case Failure(e) =>
      Logger.debug(s"Failed to parse last move: ${e.toString}")
      None
  }).map(mv => this.copy(lastMove = mv)).getOrElse(this)

  protected[image] def parseGameStatus(q: QueryString): Arguments = getFirstValue(q, "status").map {
    case "illegallymoved" => GameStatus.IllegallyMoved
    case "drawn" => GameStatus.Drawn
    case "mated" => GameStatus.Mated
    case "perpetualcheck" => GameStatus.PerpetualCheck
    case "resigned" => GameStatus.Resigned
    case "timeup" => GameStatus.TimedUp
    case "uchifuzume" => GameStatus.Uchifuzume
    case s =>
      Logger.debug(s"Invalid parameter: status=${s}")
      GameStatus.Playing
  }.map(st => this.copy(gameStatus = st)).getOrElse(this)

  protected[image] def parseFlip(q: QueryString): Arguments = {
    getFirstValue(q, "flip") match {
      case Some("true") => this.copy(flip = true)
      case Some("false") => this.copy(flip = false)
      case Some(s) =>
        Logger.debug(s"Invalid parameter: flip=${s}")
        this
      case None => this
    }
  }

  protected[image] def parseIndexDisplay(q: QueryString): Arguments = getFirstValue(q, "index") match {
    case Some("ja") => this.copy(indexDisplay = Some(Arguments.Japanese))
    case Some("en") => this.copy(indexDisplay = Some(Arguments.English))
    case Some("none") => this.copy(indexDisplay = None)
    case Some(s) =>
      Logger.debug(s"Invalid parameter: index=${s}")
      this
    case _ => this
  }

  protected[image] def parsePieceLang(q: QueryString): Arguments = getFirstValue(q, "plang") match {
    case Some("ja") => this.copy(pieceLang = Arguments.Japanese)
    case Some("en") => this.copy(pieceLang = Arguments.English)
    case _ => this
  }

  protected[image] def parsePlayerNames(q: QueryString): Arguments = this.copy(
    blackName = getFirstValue(q, "bn").map(decodeString).getOrElse("Black"), // todo default -> Settings or lang?
    whiteName = getFirstValue(q, "wn").map(decodeString).getOrElse("White")
  )

  protected[image] def parsePicURLs(q: QueryString): Arguments = this.copy(
    blackPicURL = getFirstValue(q, "bp").map(decodeString),
    whitePicURL = getFirstValue(q, "wp").map(decodeString)
  )

  protected[image] def parseGraphicLayout(q: QueryString): Arguments = getFirstValue(q, "layout") match {
    case Some("compact") => this.copy(layout = GraphicLayout.Compact)
    case Some("wide") => this.copy(layout = GraphicLayout.Wide)
    case Some("square") => this.copy(layout = GraphicLayout.Square)
    case Some("padded") => this.copy(layout = GraphicLayout.Padded)
    case Some(s) =>
      Logger.debug(s"Invalid parameter: layout=${s}")
      this
    case _ => this
  }
}

object Arguments {

  sealed trait Language

  case object Japanese extends Language

  case object English extends Language

  object GraphicLayout {

    sealed trait GraphicLayout

    case object Compact extends GraphicLayout

    case object Wide extends GraphicLayout

    case object Square extends GraphicLayout

    case object Padded extends GraphicLayout // for Facebook's 1.91x1 layout

  }

}