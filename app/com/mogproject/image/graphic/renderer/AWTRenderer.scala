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
    g.setColor(new AWTColor(c.r, c.b, c.g, c.a))
    f(g)
  }

  override def render(shape: Shape)(g: Graphics2D): Graphics2D = {
    shape match {
      case rect: Rectangle =>
        rect.strokeColor.foreach { c =>
          (0 until rect.stroke).foreach { i =>
            // make a gradation
            val color = rect.strokeGradation.map(x => c.copy(
              r = c.r + (x.r - c.r) * i / rect.stroke,
              b = c.b + (x.b - c.b) * i / rect.stroke,
              g = c.g + (x.g - c.g) * i / rect.stroke,
              a = c.a + (x.a - c.a) * i / rect.stroke
            )).getOrElse(c)

            // draw
            withColor(color)(g)(_.drawRect(rect.left - i, rect.top - i, rect.width + i * 2, rect.height + i * 2))
          }
        }
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
    val x = -t.boundary.left - t.boundary.width - 1
    val y = -t.boundary.top - t.boundary.height - 1
    renderText(t.copy(boundary = t.boundary.copy(left = x, top = y)), g)
    g.setTransform(orig)
  } else renderText(t, g)

  private[this] lazy val defaultFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass.getResourceAsStream("/fonts/ipaexm.ttf"))

  private[this] lazy val graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment

  private[this] def renderText(t: Text, g: Graphics2D): Unit = {
    // Get the Font and FontMetrics
    val font: Font = t.font.map { ft =>
      // Check if the font is installed
      if (!graphicsEnv.getAvailableFontFamilyNames.contains(ft)) {
        Logger.warn(s"Font is not installed: ${t.font}")
      }
      new Font(ft, t.style, t.fontSize)
    }.getOrElse(defaultFont.deriveFont(t.style, t.fontSize))

    val metrics: FontMetrics = g.getFontMetrics(font)

    // Determine the coordinate for the text
    val x = t.boundary.left + (t.boundary.width - metrics.stringWidth(t.text)) / 2
    val y = t.boundary.top + (t.boundary.height - metrics.getHeight) / 2 + metrics.getAscent

    // Draw the String
    g.setFont(font)
    withColor(t.foreColor)(g)(_.drawString(t.text, x, y))
  }
}
