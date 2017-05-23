package controllers

import javax.inject._

import com.mogproject.image.graphic.{BoardGraphicCompact, BoardGraphicWide}
import com.mogproject.image.{Arguments, ImageFetcher, ImageGenerator}
import play.api.Logger
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(ws: WSClient) extends Controller {
  def image = Action.async { implicit request =>
    val args = Arguments().parseQueryString(request.queryString)

    val result = for {
      (bp, wp) <- ImageFetcher.fetch(args)(ws)
      brd = if (bp.isDefined || wp.isDefined)
        BoardGraphicWide(args.flip, args.state.turn, args.state.board, args.state.hand, args.lastMove, args.gameStatus, args.indexDisplay, args.blackName, args.whiteName, bp, wp)
      else
        BoardGraphicCompact(args.flip, args.state.turn, args.state.board, args.state.hand, args.lastMove, args.gameStatus, args.indexDisplay)
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
