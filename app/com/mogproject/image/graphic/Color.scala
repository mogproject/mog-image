package com.mogproject.image.graphic

case class Color(r: Int, b: Int, g: Int, a: Int = 255)

object Color {
  lazy val WHITE = Color(255, 255, 255)
  lazy val BLACK = Color(0, 0, 0)

  lazy val cursor = Color(0xE1, 0xB2, 0x65, 240)
  lazy val lastMove = Color(0xf0, 0xf0, 0xf0)

  // for indicators
  lazy val turn = Color(0x2b, 0x5f, 0x91)
  lazy val win = Color(0x5c, 0xb8, 0x5c)
  lazy val lose = Color(0xd9, 0x53, 0x4f)
  lazy val draw = Color(0x99, 0x87, 0x7a)
}