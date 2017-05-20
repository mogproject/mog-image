package com.mogproject.image.graphic.renderer

import java.awt.{Font, FontMetrics, Graphics2D, GraphicsEnvironment, Color => AWTColor}

import com.mogproject.image.graphic.Color
import com.mogproject.image.graphic.shape._
import play.api.Logger

/**
  *
  */
object AWTRenderer extends Renderer[Graphics2D] {
  private[this] def withColor(c: Color)(g: Graphics2D)(f: Graphics2D => Unit) = {
    g.setColor(new AWTColor(c.r, c.b, c.g))
    f(g)
  }

  override def render(shape: Shape)(g: Graphics2D): Graphics2D = {
    shape match {
      case rect: Rectangle =>
        rect.strokeColor.foreach(withColor(_)(g)(_.drawRect(rect.left, rect.top, rect.width, rect.height)))
        rect.fillColor.foreach(withColor(_)(g)(_.fillRect(rect.left, rect.top, rect.width, rect.height)))
      case line: Line =>
        withColor(line.strokeColor)(g)(_.drawLine(line.fromX, line.fromY, line.toX, line.toY))
      case circle: Circle =>
        circle.strokeColor.foreach(withColor(_)(g)(_.drawOval(circle.left, circle.top, circle.width, circle.height)))
        circle.fillColor.foreach(withColor(_)(g)(_.fillOval(circle.left, circle.top, circle.width, circle.height)))
      case t: Text =>
        renderFlippedText(t.flip)(t, g)
    }
    g
  }

  private[this] def renderFlippedText(flip: Boolean)(t: Text, g: Graphics2D) = if (flip) {
    val orig = g.getTransform
    g.rotate(Math.PI)
    val x = - t.boundary.left - t.boundary.width - 1
    val y = - t.boundary.top - t.boundary.height - 1
    renderText(t.copy(boundary = t.boundary.copy(left = x, top = y)), g)
    g.setTransform(orig)
  } else renderText(t, g)

  private[this] def renderText(t: Text, g: Graphics2D): Unit = {
    // Check if the font is installed
    if (!GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames.contains(t.font)) {
      Logger.warn(s"Font is not installed: ${t.font}")
    }

    // Get the Font and FontMetrics
    val font = new Font(t.font, Font.PLAIN, t.fontSize)
    val metrics: FontMetrics = g.getFontMetrics(font)

    // Determine the coordinate for the text
    val x = t.boundary.left + (t.boundary.width - metrics.stringWidth(t.text)) / 2
    val y = t.boundary.bottom + (t.boundary.height - metrics.getHeight) / 2 - 1

    // Draw the String
    g.setFont(font)
    withColor(t.foreColor)(g)(_.drawString(t.text, x, y))
  }
}
