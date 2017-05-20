package com.mogproject.image.graphic.shape

import com.mogproject.image.graphic.Color

/**
  * basic shapes
  */
sealed trait Shape

case class Rectangle(left: Int, top: Int, width: Int, height: Int, strokeColor: Option[Color] = Some(Color.BLACK), fillColor: Option[Color] = None) extends Shape {
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
                font: String,
                fontSize: Int,
                boundary: Rectangle,
                foreColor: Color = Color.BLACK,
                flip: Boolean = false) extends Shape
