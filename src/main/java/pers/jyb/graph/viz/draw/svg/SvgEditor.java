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

package pers.jyb.graph.viz.draw.svg;

import java.util.List;
import java.util.function.Consumer;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.util.StringUtils;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.draw.Brush;
import pers.jyb.graph.viz.draw.Editor;

public abstract class SvgEditor<T, B extends Brush> implements SvgConstants, Editor<T, B> {

  protected void text(TextAttribute textAttribute) {
    Asserts.nullArgument(textAttribute, "textAttribute");

    if (textAttribute.lineAttributeConsumer == null) {
      return;
    }

    double halfHeight = textAttribute.fontsize / 2;
    String[] lines = textAttribute.label.split("\n");
    int midIndex = (lines.length - 1) / 2;
    boolean oddLen = (lines.length & 1) == 1;
    double xc = textAttribute.centerPoint.getX() + halfHeight / 8;
    double yc;
    double t = halfHeight / 3;

    for (int i = 0; i < lines.length; i++) {
      yc = textAttribute.centerPoint.getY() - t;

      yc -= (midIndex - i) * textAttribute.fontsize;
      if (oddLen) {
        yc += halfHeight;
      }

      textAttribute.lineAttributeConsumer.accept(
          new TextLineAttribute(xc, yc, i, lines[i], textAttribute.fontColor)
      );
    }
  }

  protected void setText(Element text, double fontSize, TextLineAttribute textLineAttribute) {
    text.setAttribute(SvgConstants.X, String.valueOf(textLineAttribute.getX()));
    text.setAttribute(SvgConstants.Y, String.valueOf(textLineAttribute.getY()));
    text.setAttribute(SvgConstants.TEXT_ANCHOR, "middle");
    text.setAttribute(SvgConstants.FONT_FAMILY, "Times,serif");
    text.setAttribute(SvgConstants.FONT_SIZE, String.valueOf(fontSize));

    Color fontColor = textLineAttribute.getFontColor();
    if (fontColor != null) {
      text.setAttribute(FILL, fontColor.value());
    }
  }


  public String getPathPintStr(FlatPoint point) {
    return SvgEditors.getPathPintStr(point);
  }

  public String getPathPintStr(FlatPoint point, boolean needSpace) {
    return SvgEditors.getPathPintStr(point, needSpace);
  }

  protected String pointsToSvgLine(FlatPoint start, List<FlatPoint> points, boolean isCurve) {
    if (CollectionUtils.isEmpty(points)) {
      return null;
    }

    start = start == null ? points.get(0) : start;
    StringBuilder path = new StringBuilder(PATH_START_M)
        .append(start.getX())
        .append(SvgConstants.COMMA)
        .append(start.getY());

    if (isCurve) {
      path.append(CURVE_PATH_MARK);
    } else {
      path.append(SvgConstants.SPACE);
    }

    for (int i = 1; i < points.size(); i++) {
      FlatPoint flatPoint = points.get(i);
      path.append(flatPoint.getX())
          .append(SvgConstants.COMMA)
          .append(flatPoint.getY())
          .append(SvgConstants.SPACE);
    }
    return path.toString();
  }

  protected String generateBox(Box box) {
    return generatePolylinePoints(box.getLeftBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getUpBorder(),
                                  box.getRightBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getDownBorder(),
                                  box.getLeftBorder(), box.getUpBorder());
  }

  public static String generatePolylinePoints(double... positions) {
    return SvgEditors.generatePolylinePoints(positions);
  }

  // -------------------------------------------- subclass --------------------------------------------

  public static class TextAttribute {

    private final FlatPoint centerPoint;

    private final double fontsize;

    private final String label;

    private final Color fontColor;

    private final Consumer<TextLineAttribute> lineAttributeConsumer;

    public TextAttribute(FlatPoint centerPoint, double fontsize, String label, Color fontColor,
                         Consumer<TextLineAttribute> lineAttributeConsumer) {
      Asserts.nullArgument(centerPoint, "centerPoint");
      Asserts.illegalArgument(StringUtils.isEmpty(label), "label can not be empty");
      this.centerPoint = centerPoint;
      this.fontsize = fontsize;
      this.label = label;
      this.fontColor = fontColor;
      this.lineAttributeConsumer = lineAttributeConsumer;
    }
  }

  public static class TextLineAttribute {

    private final double x;

    private final double y;

    private final int lineNo;

    private final String line;

    private final Color fontColor;

    public TextLineAttribute(double x, double y, int lineNo, String line, Color fontColor) {
      this.x = x;
      this.y = y;
      this.lineNo = lineNo;
      this.line = line;
      this.fontColor = fontColor;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public int getLineNo() {
      return lineNo;
    }

    public String getLine() {
      return line;
    }

    public Color getFontColor() {
      return fontColor;
    }
  }
}
