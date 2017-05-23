package com.mogproject.image

import play.api.libs.ws.WSClient

import scala.concurrent.Future
import com.redis.serialization.Parse.Implicits._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Fetch images from the Internet
  */

class ImageFetchError(msg: String) extends RuntimeException(msg)

object ImageFetcher extends RedisCache {

  override def redisDB: Int = 1 // todo: Settings

  override def redisTTL: Long = 60L * 60 * 24 // one day

  def fetch(url: String)(implicit ws: WSClient): Future[Array[Byte]] = withCacheFuture(url.hashCode()) {
    ws
      .url(url)
      .withRequestTimeout(Settings.Fetcher.timeoutMillis)
      .get()
      .map { response =>
        if (response.status != 200) {
          throw new ImageFetchError(s"Unexpected status: ${response.status}, url=${url}")
        }
        if (!response.header("Content-Type").exists(_.startsWith("image/"))) {
          throw new ImageFetchError(s"Unexpected content type: ${response.header("Content-Type")}, url=${url}")
        }
        response.bodyAsBytes.toByteBuffer.array()
      }
  }

  def fetch(arguments: Arguments)(implicit ws: WSClient): Future[(Option[Array[Byte]], Option[Array[Byte]])] = {
    for {
      bp <- arguments.blackPicURL.map(u => fetch(u).map(Some.apply)).getOrElse(Future[Option[Array[Byte]]](None))
      wp <- arguments.whitePicURL.map(u => fetch(u).map(Some.apply)).getOrElse(Future[Option[Array[Byte]]](None))
    } yield (bp, wp)
  }
}
