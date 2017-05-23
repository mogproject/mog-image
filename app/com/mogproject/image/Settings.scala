package com.mogproject.image

import java.net.URL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object Settings {
  private[this] val conf: Config = ConfigFactory.load()

  val redisURL: URL = new URL(conf.getString("com.mogproject.image.redis_url"))

  val rawImageSize: Int = conf.getInt("com.mogproject.image.raw_image_size")
  val defaultImageSize: Int = conf.getInt("com.mogproject.image.default_image_size")
  val maxImageSize: Int = conf.getInt("com.mogproject.image.max_image_size")
  val minImageSize: Int = conf.getInt("com.mogproject.image.min_image_size")


  object Fetcher {
    val timeoutMillis: FiniteDuration = conf.getInt("com.mogproject.image.fetcher.timeout_millis").millis
  }
}
