package com.mogproject.image

import com.redis.RedisClient
import com.redis.serialization.Parse
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success, Try}

/**
  * Read/write cache using Redis
  */
trait RedisCache {
  private[this] lazy val redisClient = Try(new RedisClient(Settings.redisURL.toURI))

  def withCache[K, V](key: K)(func: => Try[V])(implicit parse: Parse[V]): Try[V] = redisClient match {
    case Success(cl) =>
      cl.get[V](key) match {
        case Some(v) => Success(v)
        case None =>
          val v = func
          v.foreach(value => saveToCache(key, value))
          v
      }
    case e@Failure(_) =>
      Logger.warn(s"Failed to connect to Redis server: ${e}")
      func
  }

  private[this] def saveToCache[K, V](key: K, value: V, expiry: Long = 60L * 60 * 24 * 7): Unit = redisClient.foreach { cl =>
    Future(cl.setex(key, expiry, value)).onComplete {
      case Success(b) if !b => Logger.warn(s"Failed to save cache: key=${key}")
      case Failure(e) => Logger.warn(s"Failed to save cache (key=${key}): ${e}")
      case _ => // do nothing
    }
  }
}
