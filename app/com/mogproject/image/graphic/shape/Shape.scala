package com.mogproject.image.graphic.shape

import com.mogproject.image.graphic.Color

/**
  * basic shapes
  */
sealed trait Shape

case class Rectangle(left: Int,
                     top: Int,
                     width: Int,
                     height: Int,
                     strokeColor: Option[Color] = Some(Color.BLACK),
                     fillColor: Option[Color] = None,
                     stroke: Int = 1,
                     strokeGradation: Option[Color] = None) extends Shape {
  val right: Int = left + width
  val bottom: Int = top + height
}

case class Line(fromX: Int, fromY: Int, toX: Int, toY: Int, strokeColor: Color = Color.BLACK) extends Shape

case class Circle(x: Int, y: Int, r: Int, strokeColor: Option[Color] = Some(Color.BLACK), fillColor: Option[Color] = None) extends Shape {
  val left: Int = x - r
  val top: Int = y - r
  val width: Int = r * 2 + 1
  val height: Int = r * 2 + 1
}

case class Text(text: String,
                fontSize: Int,
                boundary: Rectangle,
                style: Int = Text.PLAIN | Text.ALIGN_CENTER,
                font: Option[String] = None,
                foreColor: Color = Color.BLACK,
                flip: Boolean = false) extends Shape

object Text {
  val PLAIN = 0
  val BOLD = 1

  val ALIGN_CENTER = 0
  val ALIGN_LEFT = 256
  val ALIGN_RIGHT = 512
}

case class Image(data: Array[Byte], boundary: Rectangle, flip: Boolean = false) extends Shape
