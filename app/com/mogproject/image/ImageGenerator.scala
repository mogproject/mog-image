package com.mogproject.image

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.RenderingHints

import com.mogproject.image.graphic.BoardGraphic
import com.mogproject.image.graphic.renderer.AWTRenderer
import com.redis.serialization.Parse.Implicits._
import com.sksamuel.scrimage.{Image, ScaleMethod}
import play.api.Logger

import scala.util.{Failure, Success, Try}

/**
  * main object
  */
object ImageGenerator extends RedisCache {

  override def redisDB: Int = 0

  override def redisTTL: Long = 60L * 60 * 24 * 7 // a week

  private[this] def generateImage(boardGraphic: BoardGraphic): BufferedImage = {
    val buff = new BufferedImage(boardGraphic.windowWidth, boardGraphic.windowHeight, BufferedImage.TYPE_INT_RGB)

    val g = buff.createGraphics()

    // set rendering hints
    //    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    //    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
    //    g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)
    //    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    //    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    //    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)

    boardGraphic.render(AWTRenderer)(g)
    g.dispose()

    buff
  }

  // todo: move to another class
  private[this] def scaleImage(image: Array[Byte], width: Int): Try[Array[Byte]] = Try(Image(image).scaleToWidth(width, ScaleMethod.Bicubic).bytes).recoverWith {
    case e: Throwable => Logger.error(s"Failed to scale an image: ${e}"); Failure(e)
  }

  private[this] def convertImageToByteArray(bi: BufferedImage): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    ImageIO.write(bi, "png", baos)
    baos.toByteArray
  }

  def generate(board: BoardGraphic, hashCode: Int, size: Int): Try[Array[Byte]] = withCache(hashCode) {
    val bs = convertImageToByteArray(generateImage(board))
    scaleImage(bs, size)
  }

}
