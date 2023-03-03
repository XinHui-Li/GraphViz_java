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
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.ArrowShape;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Dir;
import pers.jyb.graph.viz.api.attributes.LineStyle;
import pers.jyb.graph.viz.api.attributes.Port;

public class Line implements Comparable<Line>, Serializable {

  private static final long serialVersionUID = 7867944912063456255L;

  private static final Node EMPTY = Node.builder().build();

  private final Node head;

  private final Node tail;

  private final LineAttrs lineAttrs;

  private Line(Node head, Node tail, LineAttrs lineAttrs) {
    Asserts.nullArgument(head, "head");
    Asserts.nullArgument(tail, "tail");
    Asserts.nullArgument(lineAttrs, "lineAttrs");

    this.head = head;
    this.tail = tail;
    this.lineAttrs = lineAttrs;
  }

  public Node head() {
    return head;
  }

  public Node tail() {
    return tail;
  }

  public Node either() {
    return head;
  }

  public Node other(Node node) {
    if (node == head) {
      return tail;
    }
    if (node == tail) {
      return head;
    }
    return null;
  }

  public double weight() {
    return lineAttrs.weight != null ? lineAttrs.weight : 1D;
  }

  public LineAttrs lineAttrs() {
    return lineAttrs;
  }

  @Override
  public int compareTo(Line o) {
    if (o == null) {
      return 1;
    }

    int c = tail.compareTo(o.tail);

    if (c != 0) {
      return c;
    }

    c = head.compareTo(o.head);

    if (c != 0) {
      return c;
    }

    c = Integer.compare(lineAttrs.hashCode(), o.lineAttrs.hashCode());

    if (c != 0) {
      return c;
    }

    return this.hashCode() - o.hashCode();
  }

  public static LineBuilder builder(Node left, Node right) {
    return new LineBuilder(left, right);
  }

  public static LineBuilder tempLine() {
    return new LineBuilder(EMPTY, EMPTY);
  }

  /*------------------------------------------ buidler ---------------------------------------*/

  public static class LineBuilder {

    protected Node from;

    protected Node to;

    protected LineAttrs lineAttrs;

    private LineBuilder(Node from, Node to) {
      if (from == null || to == null) {
        throw new NullPointerException("node can not be null");
      }

      this.from = from;
      this.to = to;
      this.lineAttrs = new LineAttrs();
    }

    public LineBuilder id(String id) {
      Asserts.nullArgument(id, "id");
      lineAttrs.id = id;
      return this;
    }

    public LineBuilder controlPoints(boolean controlPoints) {
      lineAttrs.controlPoints = controlPoints;
      return this;
    }

    public LineBuilder showboxes(boolean showboxes) {
      lineAttrs.showboxes = showboxes;
      return this;
    }

    public LineBuilder radian(double radian) {
      Asserts.illegalArgument(
          radian < 0,
          "radian (" + radian + ") must be > 0"
      );
      lineAttrs.radian = radian;
      return this;
    }

    public LineBuilder label(String label) {
      lineAttrs.label = label;
      return this;
    }

    public LineBuilder color(Color color) {
      Asserts.nullArgument(color, "color");
      lineAttrs.color = new FusionColor(color);
      return this;
    }

    public LineBuilder weight(double weight) {
      lineAttrs.weight = weight;
      return this;
    }

    public LineBuilder fillColor(Color color) {
      Asserts.nullArgument(color, "color");
      lineAttrs.fillColor = new FusionColor(color);
      return this;
    }

    public LineBuilder fontColor(Color color) {
      Asserts.nullArgument(color, "color");
      lineAttrs.fontColor = color;
      return this;
    }

    public LineBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(
          fontSize < 0,
          "fontSize (" + fontSize + ") must be > 0"
      );
      lineAttrs.fontSize = fontSize;
      return this;
    }

    public LineBuilder headclip(boolean headclip) {
      lineAttrs.headclip = headclip;
      return this;
    }

    public LineBuilder tailclip(boolean tailclip) {
      lineAttrs.tailclip = tailclip;
      return this;
    }

    public LineBuilder minlen(int minlen) {
      Asserts.illegalArgument(
          minlen < 0,
          "minlen (" + minlen + ") must be > 0"
      );
      lineAttrs.minlen = minlen;
      return this;
    }

    public LineBuilder style(LineStyle lineStyle) {
      Asserts.nullArgument(lineStyle, "lineStyle");
      lineAttrs.style = lineStyle;
      return this;
    }

    public LineBuilder arrowHead(ArrowShape arrowHead) {
      Asserts.nullArgument(arrowHead, "arrowHead");
      lineAttrs.arrowHead = arrowHead;
      return this;
    }

    public LineBuilder arrowTail(ArrowShape arrowTail) {
      Asserts.nullArgument(arrowTail, "arrowTail");
      lineAttrs.arrowTail = arrowTail;
      return this;
    }

    public LineBuilder arrowSize(double arrowSize) {
      Asserts.illegalArgument(
          arrowSize < 0,
          "arrowSize (" + arrowSize + ") must be > 0"
      );
      lineAttrs.arrowSize = arrowSize * Graphviz.PIXLE;
      return this;
    }

    public LineBuilder dir(Dir dir) {
      Asserts.nullArgument(dir, "dir");
      lineAttrs.dir = dir;
      return this;
    }

    public LineBuilder lhead(String lhead) {
      lineAttrs.lhead = lhead;
      return this;
    }

    public LineBuilder ltail(String ltail) {
      lineAttrs.ltail = ltail;
      return this;
    }

    public LineBuilder floatLabels(FloatLabel... floatLabels) {
      Asserts.illegalArgument(floatLabels == null || floatLabels.length == 0,
                              "floatLabels is empty");
      lineAttrs.floatLabels = floatLabels;
      return this;
    }

    public LineBuilder tailPort(Port tailPort) {
      Asserts.nullArgument(tailPort, "tailPort");
      lineAttrs.tailPort = tailPort;
      return this;
    }

    public LineBuilder headPort(Port headPort) {
      Asserts.nullArgument(headPort, "tailPort");
      lineAttrs.headPort = headPort;
      return this;
    }

    public LineBuilder tailLabelPort(String tailLabelPort) {
      lineAttrs.tailLabelPort = tailLabelPort;
      return this;
    }

    public LineBuilder headLabelPort(String headLabelPort) {
      lineAttrs.headLabelPort = headLabelPort;
      return this;
    }

    public Line build() {
      return new Line(to, from, lineAttrs);
    }
  }
}
