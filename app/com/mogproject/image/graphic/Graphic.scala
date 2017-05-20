package com.mogproject.image.graphic

import com.mogproject.image.graphic.renderer.Renderer
import com.mogproject.image.graphic.shape.Shape

/**
  *
  */
trait Graphic {

  protected def shapes: Seq[Shape]

  def render[Image](renderer: Renderer[Image])(input: Image): Image = shapes.foldLeft(input) { case (im, sh) => renderer.render(sh)(im) }

}
