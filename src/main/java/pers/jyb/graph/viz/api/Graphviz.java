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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.GraphvizUtils;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labeljust;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.Layout;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.api.attributes.Rankdir;
import pers.jyb.graph.viz.api.attributes.Splines;

public class Graphviz extends GraphContainer implements Serializable {

  private static final long serialVersionUID = 7386714074818676956L;

  public static final int PIXLE = 72;

  private static final int MAX_DEPTH = 1000;

  private GraphAttrs graphAttrs;

  private Map<GraphContainer, GraphContainer> fatherRecord;

  private final boolean isDirected;

  private Graphviz(boolean isDirected, GraphAttrs graphAttrs) {
    this.graphAttrs = graphAttrs;
    this.isDirected = isDirected;
  }

  public GraphAttrs graphAttrs() {
    return graphAttrs;
  }

  public boolean isDirected() {
    return isDirected;
  }

  /**
   * 返回指定图容器在当前{@code Graphviz}当中的父容器。
   *
   * @param container 图容器
   * @return 当前图的父图容器
   */
  public GraphContainer father(GraphContainer container) {
    if (fatherRecord == null) {
      return null;
    }
    return fatherRecord.get(container);
  }

  /**
   * 返回指定图容器在当前{@code Graphviz}当中有效的父容器，无效的父容器{@link #isSubgraph()}返回{@code Boolean.FALSE}。
   *
   * @param container 图容器
   * @return 有效的父容器
   */
  public GraphContainer effectiveFather(GraphContainer container) {
    if (fatherRecord == null) {
      return null;
    }

    GraphContainer p = fatherRecord.get(container);
    while (p != null && p.isSubgraph()) {
      p = fatherRecord.get(p);
    }

    return p;
  }

  /*------------------------------------------ static ---------------------------------------*/

  public static GraphvizBuilder graph() {
    return new GraphvizBuilder(false);
  }

  public static GraphvizBuilder digraph() {
    return new GraphvizBuilder(true);
  }

  /*------------------------------------------ builder ---------------------------------------*/

  public static class GraphvizBuilder extends GraphContainerBuilder<Graphviz, GraphvizBuilder> {

    private final GraphAttrs graphAttrs;

    private final boolean isDirected;

    private GraphvizBuilder(boolean isDirected) {
      graphAttrs = new GraphAttrs();
      this.isDirected = isDirected;
    }

    private Graphviz newGraphviz() {
      return new Graphviz(isDirected, graphAttrs);
    }

    public GraphvizBuilder label(String label) {
      graphAttrs.label = label;
      return self();
    }

    public GraphvizBuilder labelloc(Labelloc labelloc) {
      Asserts.nullArgument(labelloc, "labelloc");
      graphAttrs.labelloc = labelloc;
      return self();
    }

    public GraphvizBuilder labeljust(Labeljust labeljust) {
      Asserts.nullArgument(labeljust, "labeljust");
      graphAttrs.labeljust = labeljust;
      return self();
    }

    public GraphvizBuilder splines(Splines splines) {
      Asserts.nullArgument(splines, "splines");
      graphAttrs.splines = splines;
      return self();
    }

    public GraphvizBuilder rankdir(Rankdir rankdir) {
      Asserts.nullArgument(rankdir, "rankdir");
      graphAttrs.rankdir = rankdir;
      return self();
    }

    public GraphvizBuilder center(boolean center) {
      graphAttrs.isCenter = center;
      return self();
    }

    public GraphvizBuilder bgColor(Color bgColor) {
      Asserts.nullArgument(bgColor, "bgColor");
      graphAttrs.bgColor = new FusionColor(bgColor);
      return self();
    }

    public GraphvizBuilder fontColor(Color fontColor) {
      Asserts.nullArgument(fontColor, "fontColor");
      graphAttrs.fontColor = fontColor;
      return self();
    }

    public GraphvizBuilder layout(Layout layout) {
      Asserts.nullArgument(layout, "layout");
      graphAttrs.layout = layout;
      return self();
    }

    public GraphvizBuilder nodeSep(double nodeSep) {
      Asserts.illegalArgument(
          nodeSep < 0,
          "nodeSep (" + nodeSep + ") must be > 0"
      );
      graphAttrs.nodeSep = nodeSep * Graphviz.PIXLE;
      return self();
    }

    public GraphvizBuilder nslimit(int nslimit) {
      Asserts.illegalArgument(
          nslimit < 0,
          "nslimit (" + nslimit + ") must be > 0"
      );
      graphAttrs.nslimit = nslimit;
      return self();
    }

    public GraphvizBuilder nslimit1(int nslimit1) {
      Asserts.illegalArgument(
          nslimit1 < 0,
          "nslimit (" + nslimit1 + ") must be > 0"
      );
      graphAttrs.nslimit1 = nslimit1;
      return self();
    }

    public GraphvizBuilder rankSep(double rankSep) {
      graphAttrs.rankSep = Math.max(rankSep, 0.1) * Graphviz.PIXLE;
      return self();
    }

    public GraphvizBuilder scale(double scale) {
      return scale(scale, scale);
    }

    public GraphvizBuilder scale(double xScale, double yScale) {
      Asserts.illegalArgument(
          xScale < 0,
          "Horizontal scale (" + xScale + ") must be > 0"
      );
      Asserts.illegalArgument(
          yScale < 0,
          "Vertical scale (" + yScale + ") must be > 0"
      );
      graphAttrs.scale = new FlatPoint(xScale, yScale);
      return self();
    }

    public GraphvizBuilder margin(double margin) {
      return margin(margin, margin);
    }

    public GraphvizBuilder margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(
          horMargin < 0,
          "Horizontal margin (" + horMargin + ") must be > 0"
      );
      Asserts.illegalArgument(
          verMargin < 0,
          "Vertical margin (" + verMargin + ") must be > 0"
      );
      graphAttrs.margin = new Point(horMargin, verMargin);
      return self();
    }

    public GraphvizBuilder searchSize(int searchSize) {
      Asserts.illegalArgument(
          searchSize < 0,
          "searchSize (" + searchSize + ") must be > 0"
      );
      graphAttrs.searchSize = searchSize;
      return self();
    }

    public GraphvizBuilder mclimit(double mclimit) {
      Asserts.illegalArgument(
          mclimit < 0,
          "mclimit (" + mclimit + ") must be > 0"
      );
      graphAttrs.mclimit = mclimit;
      return self();
    }

    public GraphvizBuilder lheight(double lheight) {
      Asserts.illegalArgument(
          lheight < 0,
          "lheight (" + lheight + ") must be > 0"
      );
      graphAttrs.lheight = lheight * Graphviz.PIXLE;
      return self();
    }

    public GraphvizBuilder lwidth(double lwidth) {
      Asserts.illegalArgument(
          lwidth < 0,
          "lwidth (" + lwidth + ") must be > 0"
      );
      graphAttrs.lwidth = lwidth * Graphviz.PIXLE;
      return self();
    }

    public GraphvizBuilder fontSize(double fontSize) {
      Asserts.illegalArgument(
          fontSize < 0,
          "fontSize (" + fontSize + ") must be > 0"
      );
      graphAttrs.fontSize = fontSize;
      return self();
    }

    public GraphvizBuilder compound(boolean compound) {
      graphAttrs.compound = compound;
      return self();
    }

    public GraphvizBuilder showGrid(boolean showGrid) {
      graphAttrs.showGrid = showGrid;
      return self();
    }

    @Override
    protected GraphvizBuilder self() {
      return this;
    }

    @Override
    protected Graphviz newContainer() {
      Graphviz graphviz = newGraphviz();
      graphviz.graphAttrs = this.graphAttrs;
      return graphviz;
    }

    @Override
    protected Graphviz copy() {
      Graphviz graphviz = newGraphviz();
      graphviz.graphAttrs = graphAttrs.clone();
      return graphviz;
    }

    @Override
    public synchronized Graphviz build() {
      Graphviz graphviz = super.build();

      // Make sure acyclic,limit container depency depth
      Set<GraphContainer> path = new HashSet<>();
      Set<GraphContainer> accessStack = new HashSet<>();

      GraphvizUtils.dfs(
          MAX_DEPTH,
          Boolean.TRUE,
          path,
          accessStack,
          graphviz,
          (s, f) -> setFather(graphviz, f, s),
          (c, f) -> setFather(graphviz, f, c),
          null
      );

      return graphviz;
    }

    private void setFather(Graphviz graphviz, GraphContainer father, GraphContainer container) {
      if (graphviz.fatherRecord == null) {
        graphviz.fatherRecord = new HashMap<>();
      }
      Asserts.illegalArgument(container.absoluteEmpty(), "Graphviz have empty sub graph!");
      Asserts.illegalArgument(graphviz.fatherRecord.get(container) != null,
                              "Graph Container is repeatedly set in Graphviz!");
      graphviz.fatherRecord.put(container, father);
    }
  }
}
