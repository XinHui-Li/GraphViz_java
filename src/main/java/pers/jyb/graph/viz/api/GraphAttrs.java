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
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labeljust;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.Layout;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.api.attributes.Rankdir;
import pers.jyb.graph.viz.api.attributes.Splines;

public class GraphAttrs implements Serializable, Cloneable {

  private static final long serialVersionUID = -1741433881093744063L;

  FusionColor bgColor;

  Splines splines = Splines.ROUNDED;

  Color fontColor = Color.BLACK;

  boolean isCenter = true;

  Rankdir rankdir = Rankdir.rankdir(System.getProperty("rankdir"));

  Layout layout = Layout.DOT;

  double nodeSep = 0.5 * Graphviz.PIXLE;

  String label;

  Labelloc labelloc = Labelloc.BOTTOM;

  Labeljust labeljust = Labeljust.CENTER;

  int nslimit = 100000;

  int nslimit1 = Integer.MAX_VALUE;

  double rankSep = (double) Graphviz.PIXLE / 2;

  FlatPoint scale = new FlatPoint(1, 1);

  Point margin = new Point(20, 20);

  int searchSize;

  double mclimit;

  double lheight;

  double lwidth;

  double fontSize = 36;

  boolean compound = false;

  boolean showGrid = false;

  public Splines getSplines() {
    return splines;
  }

  public Color getFontColor() {
    return fontColor;
  }

  public FusionColor getBgColor() {
    return bgColor;
  }

  public boolean isCenter() {
    return isCenter;
  }

  public Rankdir getRankdir() {
    return rankdir;
  }

  public Layout getLayout() {
    return layout;
  }

  public double getNodeSep() {
    return nodeSep;
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

  public int getNslimit() {
    return nslimit;
  }

  public int getNslimit1() {
    return nslimit1;
  }

  public double getRankSep() {
    return rankSep;
  }

  public FlatPoint getScale() {
    return scale;
  }

  public int getSearchSize() {
    return searchSize;
  }

  public double getMclimit() {
    return mclimit;
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

  public boolean isCompound() {
    return compound;
  }

  public boolean isShowGrid() {
    return showGrid;
  }

  @Override
  public GraphAttrs clone() {
    try {
      return (GraphAttrs) super.clone();
    } catch (CloneNotSupportedException ignore) {
      return null;
    }
  }

  @Override
  public String toString() {
    return "GraphAttrs{" +
        "bgColor=" + bgColor +
        ", splines=" + splines +
        ", fontColor=" + fontColor +
        ", isCenter=" + isCenter +
        ", rankdir=" + rankdir +
        ", layout=" + layout +
        ", nodeSep=" + nodeSep +
        ", label='" + label + '\'' +
        ", labelloc=" + labelloc +
        ", labeljust=" + labeljust +
        ", nslimit=" + nslimit +
        ", nslimit1=" + nslimit1 +
        ", rankSep=" + rankSep +
        ", scale=" + scale +
        ", margin=" + margin +
        ", searchSize=" + searchSize +
        ", mclimit=" + mclimit +
        ", lheight=" + lheight +
        ", lwidth=" + lwidth +
        ", fontSize=" + fontSize +
        ", compound=" + compound +
        ", showGrid=" + showGrid +
        '}';
  }
}
