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

package pers.jyb.graph.viz.layout;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import pers.jyb.graph.def.FlatPoint;

public class AWTMeasureLabelSize implements MeasureLabelSize {

  private static final Canvas FONT_METRIC_CANVAS;

  public static final AWTMeasureLabelSize INSTANCE = new AWTMeasureLabelSize();

  static {
    // Warm up Font metrics
    (FONT_METRIC_CANVAS = new Canvas()).getFontMetrics(new Font("Times,serif", Font.PLAIN, 0));
  }

  private AWTMeasureLabelSize() {
  }

  @Override
  public FlatPoint measure(String label, double fontSize, double widthIncr) {
    if (label == null || fontSize <= 0) {
      return FlatPoint.ZERO;
    }

    double width = 0;
    double height = 0;
    String[] lines = label.split("\n");
    Font font = new Font("Times New Roman", Font.PLAIN, (int) fontSize);

    FontMetrics fm = FONT_METRIC_CANVAS.getFontMetrics(font);
    for (String line : lines) {
      width = Math.max(fm.stringWidth(line), width);
      height += fm.getHeight();
    }

    return new FlatPoint(height, width + widthIncr);
  }
}
