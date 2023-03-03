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
import java.util.Arrays;
import java.util.Objects;
import pers.jyb.graph.viz.api.attributes.ArrowShape;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Dir;
import pers.jyb.graph.viz.api.attributes.LineStyle;
import pers.jyb.graph.viz.api.attributes.Port;

public class LineAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = 1851488933798704614L;

  String id;

  Boolean controlPoints;

  Boolean showboxes;

  ArrowShape arrowHead;

  ArrowShape arrowTail;

  Double arrowSize;

  FusionColor color;

  Dir dir;

  FusionColor fillColor;

  Color fontColor;

  Double fontSize;

  Boolean headclip;

  Boolean tailclip;

  Integer minlen;

  Double weight;

  String label;

  LineStyle style;

  String lhead;

  String ltail;

  Double radian;

  FloatLabel[] floatLabels;

  Port tailPort;

  Port headPort;

  String tailLabelPort;

  String headLabelPort;

  LineAttrs() {
  }

  public String getId() {
    return id;
  }

  public Boolean getControlPoints() {
    return controlPoints;
  }

  public Boolean getShowboxes() {
    return showboxes;
  }

  public Double getWeight() {
    return weight;
  }

  public FusionColor getColor() {
    return color;
  }

  public ArrowShape getArrowHead() {
    return arrowHead != null ? arrowHead : ArrowShape.NORMAL;
  }

  public ArrowShape getArrowTail() {
    return arrowTail != null ? arrowTail : ArrowShape.NORMAL;
  }

  public Double getArrowSize() {
    return arrowSize;
  }

  public String getLabel() {
    return label;
  }

  public Dir getDir() {
    return dir;
  }

  public FusionColor getFillColor() {
    return fillColor;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public Double getFontSize() {
    return fontSize;
  }

  public Boolean getHeadclip() {
    return headclip;
  }

  public Boolean getTailclip() {
    return tailclip;
  }

  public Integer getMinlen() {
    return minlen;
  }

  public LineStyle getStyle() {
    return style;
  }

  public String getLhead() {
    return lhead;
  }

  public String getLtail() {
    return ltail;
  }

  public Double getRadian() {
    return radian;
  }

  public FloatLabel[] getFloatLabels() {
    return floatLabels;
  }

  public Port getTailPort() {
    return tailPort;
  }

  public Port getHeadPort() {
    return headPort;
  }

  public String getTailLabelPort() {
    return tailLabelPort;
  }

  public String getHeadLabelPort() {
    return headLabelPort;
  }

  @Override
  public LineAttrs clone() {
    try {
      return (LineAttrs) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LineAttrs lineAttrs = (LineAttrs) o;
    return Objects.equals(id, lineAttrs.id) &&
        Objects.equals(controlPoints, lineAttrs.controlPoints) &&
        Objects.equals(showboxes, lineAttrs.showboxes) &&
        arrowHead == lineAttrs.arrowHead &&
        arrowTail == lineAttrs.arrowTail &&
        Objects.equals(arrowSize, lineAttrs.arrowSize) &&
        Objects.equals(color, lineAttrs.color) &&
        dir == lineAttrs.dir &&
        Objects.equals(fillColor, lineAttrs.fillColor) &&
        Objects.equals(fontColor, lineAttrs.fontColor) &&
        Objects.equals(fontSize, lineAttrs.fontSize) &&
        Objects.equals(headclip, lineAttrs.headclip) &&
        Objects.equals(tailclip, lineAttrs.tailclip) &&
        Objects.equals(minlen, lineAttrs.minlen) &&
        Objects.equals(weight, lineAttrs.weight) &&
        Objects.equals(label, lineAttrs.label) &&
        Objects.equals(style, lineAttrs.style) &&
        Objects.equals(lhead, lineAttrs.lhead) &&
        Objects.equals(ltail, lineAttrs.ltail) &&
        Objects.equals(radian, lineAttrs.radian) &&
        Arrays.equals(floatLabels, lineAttrs.floatLabels) &&
        tailPort == lineAttrs.tailPort &&
        headPort == lineAttrs.headPort &&
        Objects.equals(tailLabelPort, lineAttrs.tailLabelPort) &&
        Objects.equals(headLabelPort, lineAttrs.headLabelPort);
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(id, controlPoints, showboxes, arrowHead, arrowTail, arrowSize, color, dir, fillColor,
              fontColor, fontSize, headclip, tailclip, minlen, weight, label, style, lhead, ltail,
              radian, tailPort, headPort, tailLabelPort, headLabelPort);
    result = 31 * result + Arrays.hashCode(floatLabels);
    return result;
  }

  @Override
  public String toString() {
    return "LineAttrs{" +
        "id='" + id + '\'' +
        ", controlPoints=" + controlPoints +
        ", showboxes=" + showboxes +
        ", arrowHead=" + arrowHead +
        ", arrowTail=" + arrowTail +
        ", arrowSize=" + arrowSize +
        ", color=" + color +
        ", dir=" + dir +
        ", fillColor=" + fillColor +
        ", fontColor=" + fontColor +
        ", fontSize=" + fontSize +
        ", headclip=" + headclip +
        ", tailclip=" + tailclip +
        ", minlen=" + minlen +
        ", weight=" + weight +
        ", label='" + label + '\'' +
        ", style=" + style +
        ", lhead='" + lhead + '\'' +
        ", ltail='" + ltail + '\'' +
        ", radian=" + radian +
        ", floatLabels=" + Arrays.toString(floatLabels) +
        ", tailPort=" + tailPort +
        ", headPort=" + headPort +
        ", tailLabelPort='" + tailLabelPort + '\'' +
        ", headLabelPort='" + headLabelPort + '\'' +
        '}';
  }
}
