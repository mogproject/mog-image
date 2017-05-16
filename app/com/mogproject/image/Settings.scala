package com.mogproject.image

import java.net.URL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
  *
  */
object Settings {

  val conf: Config = ConfigFactory.load()

  object ImageFetcher {
    val playgroundURL: URL = new URL(conf.getString("com.mogproject.image.ImageFetcher.playground_url"))
    val ghostDriverURL: URL = new URL(conf.getString("com.mogproject.image.ImageFetcher.ghost_driver_url"))
    val redisURL: URL = new URL(conf.getString("com.mogproject.image.ImageFetcher.redis_url"))

    val rawImageSize: Int = conf.getInt("com.mogproject.image.ImageFetcher.raw_image_size")
    val defaultImageSize: Int = conf.getInt("com.mogproject.image.ImageFetcher.default_image_size")
    val maxImageSize: Int = conf.getInt("com.mogproject.image.ImageFetcher.max_image_size")
    val minImageSize: Int = conf.getInt("com.mogproject.image.ImageFetcher.min_image_size")
  }

}
