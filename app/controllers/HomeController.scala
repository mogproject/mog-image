package controllers

import javax.inject._

import com.mogproject.image.ImageFetcher
import play.api.mvc._

import scala.util.{Failure, Success}

@Singleton
class HomeController @Inject() extends Controller {
  def image = Action { implicit request =>
    ImageFetcher.get(request.queryString) match {
      case Success(bytes) =>
        Ok(bytes).as("image/png")
      case Failure(e) =>
        InternalServerError("Internal Server Error")
    }
  }
}
