package com.mogproject.image

import java.net.URL
import java.util.Base64

import com.sksamuel.scrimage.Image
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
import play.api.Logger
import play.api.routing.sird.QueryString

import scala.util.{Failure, Success, Try}

/**
  *
  */
object ImageFetcher {
  val RAW_IMAGE_SIZE = 800

  val DEFAULT_IMAGE_SIZE = 240

  val MIN_IMAGE_SIZE = 120

  val MAX_IMAGE_SIZE = 480

  val playgroundUrl = "file:///proj/mogproject/mog-playground/docs/index.html" // todo: use ApplicationConf

  lazy val config: DesiredCapabilities = {
    val c = new DesiredCapabilities()
    c.setJavascriptEnabled(true)
    c.setCapability("takesScreenshot", false)
    c
  }

  /**
    * @note Creates a driver for each time because it is thread-unsafe.
    */
  private[this] def createDriver(): Try[RemoteWebDriver] = Try(new RemoteWebDriver(new URL("http://localhost:9999"), config)).recoverWith {
    case e: Throwable => Logger.error(s"Failed to create a remote web driver: ${e}"); Failure(e)
  }

  private[this] def accessToUrl(remoteWebDriver: RemoteWebDriver, url: String): Try[Unit] = Try(remoteWebDriver.get(url)).recoverWith {
    case e: Throwable => Logger.error(s"Failed to access to url=${url}: ${e}"); Failure(e)
  }

  private[this] def getEncodedImage(remoteWebDriver: RemoteWebDriver): Try[String] = Try(remoteWebDriver.findElementByTagName("img").getAttribute("src")).recoverWith {
    case e: Throwable => Logger.error(s"Failed to find img tag: ${e}"); Failure(e)
  }

  private[this] def decodeImage(encodedImage: String) = Try(Base64.getDecoder.decode(encodedImage.split(",", 2)(1))).recoverWith {
    case e: Throwable => Logger.error(s"Failed to decode an encoded image: ${e}"); Failure(e)
  }

  private[this] def scaleImage(image: Array[Byte], width: Int): Try[Array[Byte]] = Try(Image(image).scaleToWidth(width).bytes).recoverWith {
    case e: Throwable => Logger.error(s"Failed to scale an image: ${e}"); Failure(e)
  }

  private[this] def convertQueryString(queryString: QueryString): QueryString =
    queryString ++ Map("size" -> Seq(RAW_IMAGE_SIZE.toString), "action" -> Seq("image"))

  private[this] def getImageWidth(s: Option[Seq[String]]): Int = (for {
    xs <- s
    x <- xs.headOption
    n <- Try(x.toInt).toOption
  } yield math.max(MIN_IMAGE_SIZE, math.min(n, MAX_IMAGE_SIZE))).getOrElse(DEFAULT_IMAGE_SIZE)

  def get(queryString: QueryString): Try[Array[Byte]] = {
    // todo: check cache
    val imageWidth = getImageWidth(queryString.get("size"))
    val url = playgroundUrl + "?" + convertQueryString(queryString).map { case (k,v) => k + "=" + v.head}.mkString("&")

    for {
      dr <- createDriver()
      _ <- accessToUrl(dr, url)
      en <- getEncodedImage(dr)
      bs <- decodeImage(en)
      im <- scaleImage(bs, imageWidth)
    } yield {
      dr.close()
      im
    }
    // todo: set to cache
  }
}