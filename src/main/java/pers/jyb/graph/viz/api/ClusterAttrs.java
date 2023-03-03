/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pers.jyb.graph.viz.api;

import java.io.Serializable;
import pers.jyb.graph.viz.api.attributes.ClusterStyle;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labeljust;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.api.attributes.Style;

public class ClusterAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = -2390770742172274269L;

  String id;

  String label;

  Labelloc labelloc = Labelloc.TOP;

  Labeljust labeljust = Labeljust.CENTER;

  FusionColor bgColor;

  FusionColor color;

  FusionColor fillColor;

  Color fontColor;

  double lheight;

  double lwidth;

  Point margin = new Point(5, 5);

  double fontSize = 16;

  Style<ClusterStyle> style;

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Labelloc getLabelloc() {
    return labelloc;
  }

  public Labeljust getLabeljust() {
    return labeljust;
  }

  public FusionColor getBgColor() {
    return bgColor;
  }

  public FusionColor getColor() {
    return color;
  }

  public FusionColor getFillColor() {
    return fillColor;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public double getLheight() {
    return lheight;
  }

  public double getLwidth() {
    return lwidth;
  }

  public Point getMargin() {
    return margin;
  }

  public double getFontSize() {
    return fontSize;
  }

  public Style<ClusterStyle> getStyle() {
    return style;
  }

  @Override
  public ClusterAttrs clone() {
    try {
      return (ClusterAttrs) super.clone();
    } catch (CloneNotSupportedException ignore) {
      return null;
    }
  }

  @Override
  public String toString() {
    return "ClusterAttrs{" +
        "id='" + id + '\'' +
        ", label='" + label + '\'' +
        ", labelloc=" + labelloc +
        ", labeljust=" + labeljust +
        ", bgColor=" + bgColor +
        ", color=" + color +
        ", fillColor=" + fillColor +
        ", fontColor=" + fontColor +
        ", lheight=" + lheight +
        ", lwidth=" + lwidth +
        ", margin=" + margin +
        ", fontSize=" + fontSize +
        ", style=" + style +
        '}';
  }
}
