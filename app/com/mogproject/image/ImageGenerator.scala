package com.mogproject.image

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.RenderingHints

import com.mogproject.image.graphic.BoardGraphic
import com.mogproject.image.graphic.renderer.AWTRenderer
import com.redis.serialization.Parse.Implicits._
import com.sksamuel.scrimage.Image
import play.api.Logger
import play.api.routing.sird.QueryString

import scala.util.{Failure, Success, Try}

/**
  *
  */
object ImageGenerator extends RedisCache {

  private[this] def generateImage(): BufferedImage = {
    val brd = BoardGraphic()
    val buff = new BufferedImage(brd.windowWidth, brd.windowHeight, BufferedImage.TYPE_INT_RGB)

    val g = buff.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    brd.render(AWTRenderer)(g)
    g.dispose()

    buff
  }

  private[this] def scaleImage(image: Array[Byte], width: Int): Try[Array[Byte]] = Try(Image(image).scaleToWidth(width).bytes).recoverWith {
    case e: Throwable => Logger.error(s"Failed to scale an image: ${e}"); Failure(e)
  }

  private[this] def getImageWidth(s: Option[Seq[String]]): Int = (for {
    xs <- s
    x <- xs.headOption
    n <- Try(x.toInt).toOption
  } yield math.max(Settings.minImageSize, math.min(n, Settings.maxImageSize))).getOrElse(Settings.defaultImageSize)

  def get(queryString: QueryString): Try[Array[Byte]] = withCache(queryString.hashCode()) {
    val imageWidth = getImageWidth(queryString.get("size"))
//    val url = Settings.playgroundURL + "?" + convertQueryString(queryString).map { case (k, v) => k + "=" + v.head }.mkString("&")

    val baos = new ByteArrayOutputStream()
    ImageIO.write(generateImage(), "png", baos)

    for {
      bs <- Success(baos.toByteArray)
      im <- scaleImage(bs, imageWidth)
    } yield {
      im
    }
  }

}
