package com.mogproject.image.graphic

case class Color(r: Int, b: Int, g: Int)

object Color {
  lazy val WHITE = Color(255, 255, 255)
  lazy val BLACK = Color(0, 0, 0)
}