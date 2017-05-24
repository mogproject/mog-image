package controllers

import javax.inject._

import com.mogproject.image.Arguments.GraphicLayout
import com.mogproject.image.graphic.{BoardGraphicCompact, BoardGraphicSquare, BoardGraphicWide}
import com.mogproject.image.{Arguments, ImageFetcher, ImageGenerator, Settings}
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(ws: WSClient) extends Controller {
  def image: Action[AnyContent] = Action.async { implicit request =>
    val args = Arguments().parseQueryString(request.queryString)

    val result = for {
      (bp, wp) <- ImageFetcher.fetch(args)(ws)
      brd = args.layout match {
        case GraphicLayout.Square =>
          BoardGraphicSquare(
            args.flip, args.state.turn, args.state.board, args.state.hand, args.lastMove, args.gameStatus, args.indexDisplay,
            args.blackName, args.whiteName, bp.getOrElse(Settings.defaultProfileImage), wp.getOrElse(Settings.defaultProfileImage)
          )
        case GraphicLayout.Wide =>
          BoardGraphicWide(
            args.flip, args.state.turn, args.state.board, args.state.hand, args.lastMove, args.gameStatus, args.indexDisplay,
            args.blackName, args.whiteName, bp.getOrElse(Settings.defaultProfileImage), wp.getOrElse(Settings.defaultProfileImage)
          )
        case GraphicLayout.Compact =>
          BoardGraphicCompact(
            args.flip, args.state.turn, args.state.board, args.state.hand, args.lastMove, args.gameStatus, args.indexDisplay
          )
      }
      bytes <- Future.fromTry(ImageGenerator.generate(brd, args.hashCode(), args.size))
    } yield {
      Logger.debug(s"Success: args=${args}")
      Ok(bytes).as("image/png")
    }

    result.recover { case e =>
      Logger.warn(e.toString)
      InternalServerError("Internal Server Error")
    }
  }
}
