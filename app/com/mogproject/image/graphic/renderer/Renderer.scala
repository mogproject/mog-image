package com.mogproject.image.graphic.renderer

import com.mogproject.image.graphic.shape.Shape

/**
  *
  */
trait Renderer[Image] {
  def render(shape: Shape)(input: Image): Image
}
