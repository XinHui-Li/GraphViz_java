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
import pers.jyb.graph.viz.api.attributes.ClusterStyle;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Labeljust;
import pers.jyb.graph.viz.api.attributes.Labelloc;
import pers.jyb.graph.viz.api.attributes.Point;
import pers.jyb.graph.viz.api.attributes.Style;

public class Cluster extends GraphContainer implements Serializable {

  private static final long serialVersionUID = 5027532737058187995L;

  private Cluster(ClusterAttrs clusterAttrs) {
    this.clusterAttrs = clusterAttrs;
  }

  private final ClusterAttrs clusterAttrs;

  public ClusterAttrs clusterAttrs() {
    return clusterAttrs;
  }

  /*------------------------------------------ static ---------------------------------------*/

  public static ClusterBuilder builder() {
    return new ClusterBuilder();
  }

  public abstract static class AbstractClusterBuilder<B extends GraphContainerBuilder<Cluster, B>>
      extends GraphContainerBuilder<Cluster, B> {

    private final ClusterAttrs clusterAttrs;

    protected AbstractClusterBuilder() {
      this.clusterAttrs = new ClusterAttrs();
    }

    public B label(String label) {
      clusterAttrs.label = label;
      return self();
    }

    public B labelloc(Labelloc labelloc) {
      Asserts.nullArgument(labelloc, "labelloc");
      clusterAttrs.labelloc = labelloc;
      return self();
    }

    public B labeljust(Labeljust labeljust) {
      Asserts.nullArgument(labeljust, "labeljust");
      clusterAttrs.labeljust = labeljust;
      return self();
    }

    public B bgColor(Color bgColor) {
      Asserts.nullArgument(bgColor, "bgColor");
      clusterAttrs.bgColor = new FusionColor(bgColor);
      return self();
    }

    public B color(Color color) {
      Asserts.nullArgument(color, "color");
      clusterAttrs.color = new FusionColor(color);
      return self();
    }

    public B fillColor(Color fillColor) {
      Asserts.nullArgument(fillColor, "fillColor");
      clusterAttrs.fillColor = new FusionColor(fillColor);
      return self();
    }

    public B fontColor(Color fontColor) {
      Asserts.nullArgument(fontColor, "fontColor");
      clusterAttrs.fontColor = fontColor;
      return self();
    }

    public B lheight(double lheight) {
      Asserts.illegalArgument(
          lheight < 0,
          "lheight (" + lheight + ") must be > 0"
      );
      clusterAttrs.lheight = lheight * Graphviz.PIXLE;
      return self();
    }

    public B lwidth(double lwidth) {
      Asserts.illegalArgument(
          lwidth < 0,
          "lwidth (" + lwidth + ") must be > 0"
      );
      clusterAttrs.lwidth = lwidth * Graphviz.PIXLE;
      return self();
    }

    public B margin(double margin) {
      return margin(margin, margin);
    }

    public B margin(double horMargin, double verMargin) {
      Asserts.illegalArgument(
          horMargin < 0,
          "Horizontal margin (" + horMargin + ") must be > 0"
      );
      Asserts.illegalArgument(
          verMargin < 0,
          "Vertical margin (" + verMargin + ") must be > 0"
      );
      double[] margins = {horMargin, verMargin};
      return margin(margins);
    }

    public B margin(double... margin) {
      Asserts.illegalArgument(margin == null || margin.length == 0, "margin can not be empty");
      double[] ms = new double[margin.length];
      for (int i = 0; i < margin.length; i++) {
        ms[i] = margin[i];
      }
      clusterAttrs.margin = new Point(ms);
      return self();
    }

    public B fontSize(double fontSize) {
      Asserts.illegalArgument(
          fontSize < 0,
          "fontSize (" + fontSize + ") must be > 0"
      );
      clusterAttrs.fontSize = fontSize;
      return self();
    }

    public B style(ClusterStyle... clusterStyles) {
      Asserts.nullArgument(clusterStyles, "clusterStyles");
      clusterAttrs.style = new Style<>(clusterStyles);
      return self();
    }

    @Override
    protected Cluster newContainer() {
      return new Cluster(clusterAttrs);
    }

    @Override
    protected Cluster copy() {
      return new Cluster(clusterAttrs.clone());
    }
  }

  public static class ClusterBuilder extends
      AbstractClusterBuilder<ClusterBuilder> {

    private ClusterBuilder() {
      super();
    }

    @Override
    protected ClusterBuilder self() {
      return this;
    }
  }

  public static class IntegrationClusterBuilder<
      G extends GraphContainer,
      B extends GraphContainerBuilder<G, B>>
      extends AbstractClusterBuilder<IntegrationClusterBuilder<G, B>> {

    private final B container;

    IntegrationClusterBuilder(B container) {
      Asserts.nullArgument(container, "container");
      this.container = container;
    }

    public B endClus() {
      Cluster cluster = build();
      container.cluster(cluster);
      return container;
    }

    @Override
    protected IntegrationClusterBuilder<G, B> self() {
      return this;
    }
  }
}
